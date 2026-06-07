# 抽签小程序

微信小程序 + Spring Cloud 微服务后端，支持抽签活动创建、参与、开奖、中奖展示。

## 项目结构

```
chouqian-miniprogram/
├── front/                          # 微信小程序前端
│   ├── app.js / app.json / app.wxss
│   ├── pages/
│   │   ├── index/                  # 首页：轮播 + 官方抽签列表
│   │   ├── drawDetail/             # 抽签详情：查看 / 编辑 / 发布 / 开奖 / 中奖名单
│   │   ├── draw/                   # 口令参与
│   │   ├── drawList/               # 抽签列表
│   │   ├── publish/                # 创建抽签
│   │   ├── login/                  # 登录
│   │   ├── register/               # 注册
│   │   ├── mine/                   # 我的
│   │   └── userInfo/               # 用户信息
│   ├── utils/
│   │   ├── apis/draw.js            # 抽签 API 封装
│   │   ├── request.js              # 请求封装
│   │   └── auth.js                 # 认证工具
│   └── images/                     # 静态资源
│
└── back/                           # Spring Cloud 微服务后端
    ├── qs-common/                  # 公共模块：实体类、DTO/VO、JWT、安全工具
    ├── qs-gateway/                 # 网关 :8080
    ├── qs-client/                  # 用户服务 :8085
    └── qs-draw/                    # 抽签服务 :8086
```

## 功能清单

### 小程序端

| 页面 | 功能 |
|---|---|
| 首页 | 轮播图（官方大奖）、快捷入口（口令参与/签到）、官方抽签列表、下拉刷新 |
| 抽签详情 | 封面、统计条、规则信息、奖品一览、我的签码、参与/发布/开奖/编辑 |
| 详情页编辑 | 页面内直接编辑抽签（标题/封面/描述/规则/奖品），无需跳转 |
| 中奖名单 | 开奖后展示中奖者头像、昵称、签码、获奖等级 |
| 创建抽签 | 三步表单：基本设置 → 抽签规则 → 奖品设置 |
| 口令参与 | 通过口令码参与抽签 |
| 用户系统 | 登录、注册、个人中心 |

### 后端服务

| 模块 | 说明 |
|---|---|
| qs-gateway | 网关：JWT 验签 + Redis token 校验，请求转发 |
| qs-client | 用户服务：登录注册、用户管理、角色权限 |
| qs-draw | 抽签服务：抽签 CRUD、参与、开奖、奖品管理、中奖记录、口令 |

## 技术栈

### 前端

| 框架/工具 | 版本 |
|---|---|
| 微信小程序原生 | — |

### 后端

| 依赖 | 版本 | 说明 |
|---|---|---|
| JDK | 17 | |
| Spring Boot | 3.5.4 | |
| Spring Cloud | 2023.0.3 | |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos 服务发现 |
| MyBatis-Plus | 3.5.16 | ORM + 分页 |
| MySQL Connector | 8.0.33 | |
| jjwt | 0.12.6 | JWT 签发与验签 |
| Redisson | 3.44.0 | 分布式锁 |
| Knife4j | 4.6.0 | API 文档 |
| RabbitMQ | 3.13 | 抽签异步处理 |
| Druid | 1.2.25 | 连接池 |

## 本地运行

### 后端

环境依赖：JDK 17+、MySQL 8.0+、Redis、Nacos、RabbitMQ

```bash
# 启动 Nacos
docker run -d --name nacos -p 8848:8848 -p 9848:9848 \
  -e MODE=standalone nacos/nacos-server:v2.3.2

# 启动 RabbitMQ
docker run -d --name rabbitmq -p 5672:5672 -p 15672:15672 \
  -e RABBITMQ_DEFAULT_USER=admin -e RABBITMQ_DEFAULT_PASS=123456 \
  rabbitmq:3.13-management

# 依次启动：qs-gateway → qs-client → qs-draw
```

配置文件位于 `back/qs-common/src/main/resources/dev/base.yaml`。

### 前端

用微信开发者工具打开 `front/` 目录，配置 AppID 后即可预览。

## API 文档

网关聚合文档：`http://localhost:8080/doc.html`
