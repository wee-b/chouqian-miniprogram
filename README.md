# 抽签小程序

微信小程序 + Spring Cloud 微服务后端的抽签活动系统，支持活动创建、口令参与、抽签码生成、手动/自动开奖、可验证随机开奖、开奖结果展示、消息中心和实时通知。

项目经过一次架构优化后，后端从单一抽签服务扩展为 Gateway + 用户服务 + 抽签服务 + 运营支撑服务，并引入 Dubbo、RabbitMQ、XXL-JOB、Sentinel、WebSocket 等能力，重点补强高并发参与、自动开奖、开奖后通知和流量治理链路。

## 项目结构

```text
chouqian-miniprogram/
├── front/                         # 微信小程序前端
│   ├── app.js / app.json / app.wxss
│   ├── pages/
│   │   ├── index/                 # 首页、官方抽签列表
│   │   ├── drawDetail/            # 抽签详情、参与、开奖、中奖名单
│   │   ├── draw/                  # 口令参与
│   │   ├── drawList/              # 抽签列表
│   │   ├── publish/               # 创建抽签
│   │   ├── message/               # 消息中心
│   │   ├── login/ register/       # 登录注册
│   │   └── mine/ userInfo/        # 我的、用户信息
│   └── utils/                     # 请求封装、鉴权、API 封装
│
├── back/                          # Spring Cloud 微服务后端
│   ├── qs-common/                 # 公共实体、DTO/VO、JWT、安全、限流、Sentinel 基础配置
│   ├── qs-api/                    # Dubbo 接口契约与 DTO
│   ├── qs-gateway/                # 网关服务 :8080
│   ├── qs-client/                 # 用户/权限服务 :8085，Dubbo Provider
│   ├── qs-draw/                   # 抽签核心服务 :8086，Dubbo Consumer
│   └── qs-ops/                    # 运营支撑服务 :8087，Dubbo Provider
│
└── sql/                           # 建表与初始化数据
```

## 后端模块

| 模块 | 端口 | 职责 |
|---|---:|---|
| `qs-gateway` | 8080 | 统一入口、JWT/Redis token 校验、路由转发、Swagger 聚合、WebSocket 代理 |
| `qs-client` | 8085 | 用户登录注册、用户管理、角色权限、用户状态 Dubbo 服务 |
| `qs-draw` | 8086 | 抽签创建/发布/参与/开奖、抽签码、奖品、中奖名单、消息中心、WebSocket 推送 |
| `qs-ops` | 8087 | 操作日志、风控规则、分布式 ID、实时统计等 Dubbo 支撑能力 |
| `qs-api` | - | Dubbo 接口契约，供 Provider/Consumer 共享 |
| `qs-common` | - | 公共领域对象、工具类、异常处理、JWT、Redisson 限流、Sentinel 配置 |

## 核心能力

### 1. 抽签业务闭环

- 创建、编辑、发布抽签活动。
- 支持口令参与和详情页参与。
- Redis 前置参与次数控制，避免超过活动参与上限。
- 抽签码批量生成，结合本地去重、数据库查重和唯一约束保证唯一性。
- 手动开奖和自动开奖复用同一套开奖执行器。

### 2. Dubbo 服务拆分

`qs-draw` 通过 Dubbo 调用 `qs-client` 和 `qs-ops`：

| Dubbo 能力 | Provider | 调用场景 |
|---|---|---|
| `UserDubboService` | `qs-client` | 参与抽签前校验用户状态 |
| `RiskRuleDubboService` | `qs-ops` | 参与抽签时执行黑名单/IP 频控风控 |
| `IdDubboService` | `qs-ops` | 生成参与流水号、操作 traceId |
| `OpLogDubboService` | `qs-ops` | 异步记录参与、开奖等关键操作日志 |
| `StatDubboService` | `qs-ops` | 统计参与人数、在线人数等运营数据 |

### 3. 参与抽签高并发处理

参与链路核心流程：

```text
用户请求
  -> Gateway 鉴权
  -> qs-draw Sentinel / Redisson 限流
  -> Dubbo 校验用户状态
  -> Dubbo 风控校验
  -> Redis 原子占用参与次数
  -> 生成抽签码并批量落库
  -> 更新活动统计
  -> Dubbo 异步操作日志
  -> afterCommit 异步通知用户
```

参与实现支持两种路径：

| 路径 | 开关 | 说明 |
|---|---|---|
| 本地同步路径 | `qs.rabbitmq.enabled=false` | Controller 直接调用本地参与执行器，适合默认开发和演示 |
| RabbitMQ 异步路径 | `qs.rabbitmq.enabled=true` | Controller 投递参与消息，消费者生成抽签码，支持削峰和手动 ACK |

### 4. XXL-JOB 定时开奖

- 发布按时间开奖的活动后，通过 XXL-JOB Admin 动态创建单次开奖任务。
- 任务参数绑定 `drawId`，到点后触发 `triggerOpenDrawHandler`。
- 自动开奖通过 Redis 分布式锁和活动状态校验保证幂等。
- 手动提前开奖成功后，在事务提交后删除对应 XXL-JOB 任务。
- 任务注册/删除使用 `afterCommit`，避免业务事务回滚后产生脏任务。

### 5. 可验证随机开奖

开奖采用轻量级 commit-reveal 思路：

- 活动发布时生成 `serverSeed`，提前公开 `seedHash = SHA256(serverSeed)`。
- 开奖时按 `SHA256(serverSeed + drawId + codeValue)` 对抽签码排序。
- 根据排序结果和奖品等级/数量分配中奖码。
- 开奖后保存并公开 `serverSeed`、`codesHash`，用户可以复算开奖结果。

这样可以解释“开奖前无法预测，开奖后可以验证”。

### 6. 消息中心与实时通知

- `qs_notify_message` 持久化站内通知，支持分页、未读数、单条已读、全部已读。
- 参与成功、开奖成功、中奖结果等事件在事务提交后异步写通知。
- 自定义 `drawNotifyExecutor` 处理通知任务，避免阻塞参与和开奖主流程。
- WebSocket 地址：`/ws/notify`，用于在线用户实时弹窗、红点刷新和详情页自动刷新。
- 离线用户下次进入消息中心仍可查看通知。

### 7. Sentinel 流量治理

当前已在 `qs-draw` 服务内接入 Sentinel：

- 对参与抽签、开奖、中奖名单、详情页、我的抽签码等接口配置 `@SentinelResource`。
- `draw:join` 配置 QPS 限流、线程数限流和基于 `drawId` 的热点参数限流。
- `draw:winners`、`draw:detail`、`draw:statistics` 配置慢调用比例熔断。
- 配置系统自适应保护规则：RT、线程数、入口 QPS、CPU 使用率。
- `blockHandler` 处理限流/熔断/系统保护，`fallback` 处理非业务异常。

重点保护场景：

```text
热门活动集中参与
  -> draw:join QPS/线程数限流
  -> drawId 热点参数限流

开奖后大量用户刷新结果
  -> draw:winners / draw:detail QPS 限流
  -> 慢调用熔断降级
```

### 8. 操作日志与风控

- `qs-ops` 提供操作日志 Dubbo 接口，参与、开奖等关键动作异步上报。
- 操作日志记录 traceId、用户、模块、动作、参数、IP、成功失败状态。
- 风控服务支持黑名单和 IP 滑动窗口频控。
- 风控失败会在参与链路中直接拦截，避免恶意请求继续进入抽签码生成和数据库写入。

## 技术栈

| 类型 | 技术 |
|---|---|
| 前端 | 微信小程序原生 |
| 后端基础 | JDK 17、Spring Boot 3.5.4、Spring Cloud 2023.0.3、Spring Cloud Alibaba 2023.0.1.0 |
| 网关/注册 | Spring Cloud Gateway、Nacos |
| RPC | Apache Dubbo 3.3.4 Triple |
| 数据访问 | MySQL、MyBatis-Plus 3.5.16、Druid、P6Spy |
| 缓存/锁/限流 | Redis、Redisson 3.44.0 |
| 消息/调度 | RabbitMQ、XXL-JOB |
| 稳定性 | Sentinel 1.8.8 |
| 实时通知 | WebSocket |
| 文档 | SpringDoc、Knife4j |
| 安全 | Spring Security、JWT |

## 本地运行

### 1. 基础环境

需要准备：

- JDK 17+
- Maven 3.8+
- MySQL 8.0+
- Redis
- Nacos 2.x
- RabbitMQ
- XXL-JOB Admin

### 2. 启动基础组件

示例：

```bash
# Nacos 2.x，需要同时暴露 8848 和 gRPC 9848
docker run -d --name nacos \
  -p 8848:8848 -p 9848:9848 \
  -e MODE=standalone \
  nacos/nacos-server:v2.3.2

# RabbitMQ 管理版
docker run -d --name rabbitmq \
  -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin \
  -e RABBITMQ_DEFAULT_PASS=123456 \
  rabbitmq:3.13-management
```

XXL-JOB Admin 默认配置：

```text
地址：http://127.0.0.1:8888/xxl-job-admin
账号：admin
密码：123456
执行器 appname：qs-draw
执行器端口：9999
JobHandler：triggerOpenDrawHandler
```

### 3. 初始化数据库

执行：

```text
sql/tables.sql
sql/insertData.sql
```

### 4. 配置本地环境

配置文件入口：

```text
back/qs-common/src/main/resources/dev/base.yaml
```

本地私有配置通常放在各服务的 `application-dev.yml` 中，至少需要提供：

```yaml
qs:
  datasource:
    host: localhost
    port: 3306
    database: your_database
    username: root
    password: your_password
  redis:
    host: localhost
    port: 6379
    database: 0
    password:
  rabbitmq:
    host: localhost
    port: 5672
    username: admin
    password: 123456
    virtual-host: /
    enabled: false
```

需要启用 MQ 异步参与链路时，将 `qs.rabbitmq.enabled` 改为 `true`。

### 5. 启动后端服务

建议顺序：

```text
1. qs-ops       # Dubbo Provider: 日志/风控/ID/统计
2. qs-client    # Dubbo Provider: 用户服务
3. qs-draw      # 抽签核心服务，依赖 qs-client/qs-ops
4. qs-gateway   # 统一入口
```

也可以先编译：

```bash
cd back
mvn -q -pl qs-draw -am compile
```

### 6. 启动前端

使用微信开发者工具打开：

```text
front/
```

根据实际 AppID 和后端网关地址调整小程序配置。

## 访问地址

| 服务 | 地址 |
|---|---|
| 网关 | `http://localhost:8080` |
| 用户服务 | `http://localhost:8085` |
| 抽签服务 | `http://localhost:8086` |
| 运营服务 | `http://localhost:8087` |
| 网关聚合 API 文档 | `http://localhost:8080/swagger-ui.html` |
| RabbitMQ 控制台 | `http://localhost:15672` |
| XXL-JOB Admin | `http://127.0.0.1:8888/xxl-job-admin` |
| WebSocket 通知 | `ws://localhost:8080/ws/notify` |


## 当前验证情况

已验证：

```bash
cd back
mvn -q -pl qs-draw -am compile
```

单测 `mvn -q -pl qs-draw -am test` 需要本地 Nacos 正常启动，否则 Spring 上下文会在连接 `localhost:9848` 时失败。
