# Spring Cloud + Spring Boot 3 集成 Knife4j 网关聚合方案

## 版本要求

| 组件 | 版本 | 说明 |
|---|---|---|
| Spring Boot | 3.5.4 | 内置 Spring Framework 6.2 |
| Spring Cloud | 2023.0.3 | 内置 Spring Cloud Gateway 4.1 |
| Spring Cloud Alibaba | 2023.0.1.0 | Nacos 服务发现 |
| springdoc-openapi | 2.8.6 | WebFlux 版，聚合核心 |
| Knife4j | 4.6.0 | 仅用 UI 皮肤，不用 Gateway 聚合模块 |

---

## 一、后端服务配置（qs-client、qs-draw）

每个 MVC 服务独立接入 Knife4j，与普通 Spring Boot 3 项目完全一样。

### 1.1 pom.xml

```xml
<!-- Knife4j 文档（MVC 版） -->
<dependency>
    <groupId>com.github.xingfudeshi</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.6.0</version>
</dependency>
```

该依赖已包含 springdoc-openapi + Swagger 注解 + Knife4j UI，无需额外添加。

> **Knife4j groupId 变更说明**：Knife4j 4.5.0 起 groupId 从 `com.github.xiaoymin` 变为 `com.github.xingfudeshi`。如果看到两个不同的 groupId，它们是同一个项目的不同版本，不是两个不同的包。

#### 版本兼容注意事项

Knife4j 4.4.0 及更早版本自带的 `springdoc-openapi-starter-webmvc-ui` 版本较旧，与 Spring Framework 6.2+（Spring Boot 3.3+）不兼容。表现为启动时报 `NoSuchMethodError` 或 OpenAPI 规范生成异常。

**如果你必须使用 Knife4j 4.4.0（或更早版本），需要排除自带的 springdoc，手动指定兼容版本：**

```xml
<!-- Knife4j 4.4.0（旧版，groupId 为 xiaoymin） -->
<dependency>
    <groupId>com.github.xiaoymin</groupId>
    <artifactId>knife4j-openapi3-jakarta-spring-boot-starter</artifactId>
    <version>4.4.0</version>
    <exclusions>
        <exclusion>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </exclusion>
    </exclusions>
</dependency>

<!-- 手动指定 springdoc 兼容版本 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.7.0</version>
</dependency>
```

**推荐做法**：直接使用 Knife4j 4.6.0+（groupId `com.github.xingfudeshi`），自带的 springdoc 版本已兼容 Spring 6.2+，无需额外配置。

### 1.2 分组配置（Java Config）

每个服务创建一个 `GroupedOpenApi` Bean，设置唯一 group 名称：

```java
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openApi() {
        return new OpenAPI()
                .info(new Info().title("服务名称").version("1.0"));
    }

    @Bean
    public GroupedOpenApi serviceGroup() {
        return GroupedOpenApi.builder()
                .group("service-group-name")    // 唯一标识，网关聚合时引用
                .displayName("服务显示名称")
                .packagesToScan("com.xxx.yyy")   // 扫描 Controller 包
                .build();
    }
}
```

**关键点**：`group` 值在整个系统中唯一，网关通过 `/v3/api-docs/{group}` 获取该服务的文档。

### 1.3 拦截器放行

```java
@Override
public void addInterceptors(InterceptorRegistry registry) {
    registry.addInterceptor(authInterceptor)
            .addPathPatterns("/**")
            .excludePathPatterns(
                    "/doc.html",
                    "/webjars/**",
                    "/v3/api-docs/**",
                    "/swagger-resources/**",
                    "/error");
}
```

### 1.4 application.yml

```yaml
springdoc:
  swagger-ui:
    enabled: true
  api-docs:
    enabled: true

knife4j:
  enable: true
```

---

## 二、网关配置（qs-gateway）

网关是关键。不要用 `knife4j-gateway-spring-boot-starter`，它的聚合逻辑有 bug 且会干扰 springdoc 的端点注册。

架构思路：**springdoc 负责聚合端点，Knife4j 只管 UI 皮肤，职责分离**。

### 2.1 pom.xml

```xml
<!-- SpringDoc WebFlux 版 — 核心：在网关注册 /v3/api-docs 聚合端点 -->
<dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webflux-ui</artifactId>
    <version>2.8.6</version>
</dependency>

<!-- Knife4j UI 皮肤 — 仅替换默认 Swagger UI，不带聚合逻辑 -->
<dependency>
    <groupId>com.github.xingfudeshi</groupId>
    <artifactId>knife4j-openapi3-ui</artifactId>
    <version>4.6.0</version>
</dependency>
```

**为什么不用 `knife4j-gateway-spring-boot-starter`：**

| 问题 | 说明 |
|---|---|
| group 匹配 bug | 手动模式下按 group 名查找资源失败，报 `No OpenAPI resource found` |
| 干扰 springdoc | 覆盖 springdoc 的 `RouterFunction` 注册，导致 `/v3/api-docs/swagger-config` 被网关路由劫持 |

### 2.2 application.yml

```yaml
spring:
  cloud:
    gateway:
      routes:
        # ═══ API 文档代理路由 ═══
        # 位置：必须在 catch-all 兜底路由之前
        # StripPrefix=1：去掉第一段服务前缀（/client 或 /draw）
        - id: service-a-api-docs
          uri: lb://service-a
          predicates:
            - Path=/service-a/v3/api-docs/**
          filters:
            - StripPrefix=1

        - id: service-b-api-docs
          uri: lb://service-b
          predicates:
            - Path=/service-b/v3/api-docs/**
          filters:
            - StripPrefix=1

        # ═══ 业务路由 ═══
        - id: service-a-business
          uri: lb://service-a
          predicates:
            - Path=/client/draw/**, /client/prize/**

        # 兜底路由（排在最后）
        - id: fallback
          uri: lb://service-default
          predicates:
            - Path=/**
      default-filters:
        - DedupeResponseHeader=Access-Control-Allow-Origin Access-Control-Allow-Credentials

# SpringDoc 聚合配置
springdoc:
  swagger-ui:
    urls:
      - name: 服务A显示名称
        url: /service-a/v3/api-docs/group-a
      - name: 服务B显示名称
        url: /service-b/v3/api-docs/group-b
```

**几个容易踩的坑：**

#### 坑1：RewritePath 的 `${}` 被 Spring 占位符吞掉

```
# 错误写法 — ${remaining} 被 Spring 当作属性占位符，解析失败
- RewritePath=/service-a(?<remaining>/?.*), $\{remaining}

# 正确写法 — StripPrefix 不涉及正则占位符，不受影响
- StripPrefix=1
```

#### 坑2：API 文档路由必须在兜底路由之前

Spring Cloud Gateway 按配置顺序匹配路由，第一条匹配的生效。如果把 `/**` 兜底路由放在前面，文档路径会被错误转发。

#### 坑3：网关自身的 /v3/api-docs 被转发

这是最隐蔽的问题。springdoc 通过 `RouterFunction` 在网关注册 `/v3/api-docs/swagger-config` 端点，`RouterFunction` 优先级（ORDER=-1）高于 Gateway 路由（ORDER=1）。但如果用了错误的依赖（如 `springdoc-openapi-starter-webmvc-ui` 代替 `webflux-ui`），`RouterFunction` 根本没注册，请求就落到兜底路由被转发走了。

验证方法：访问 `http://gateway:port/v3/api-docs/swagger-config`，返回的 JSON 中如果出现后端服务的端口号（如 `oauth2RedirectUrl: "http://xxx:8085"`），说明被转发了。正常情况应当只返回 `urls` 列表。

### 2.3 认证白名单

网关中的认证过滤器需要放行文档路径：

```java
private static final List<String> EXCLUDE_PATHS = List.of(
        // 业务免认证路径 ...
        "/doc.html",
        "/webjars/**",
        "/v3/api-docs/**",
        "/swagger-resources/**",
        "/favicon.ico",
        // ⚠️ 带服务前缀的文档代理路径也要加！
        "/service-a/v3/api-docs/**",
        "/service-b/v3/api-docs/**"
);
```

**坑4：忘记加带服务前缀的路径**。`/v3/api-docs/**` 只匹配网关自身的文档端点，`/service-a/v3/api-docs/**` 是 Knife4j UI 通过网关代理请求后端文档时用的路径，前缀不同，必须单独加。

---

## 三、请求链路

```
浏览器 :8080/doc.html
  │
  ├─① /v3/api-docs/swagger-config
  │      → 网关 springdoc RouterFunction 自己处理
  │      → 返回 JSON: [{name:"服务A", url:"/service-a/v3/api-docs/group-a"}, ...]
  │
  ├─② /service-a/v3/api-docs/group-a
  │      → Gateway 路由 StripPrefix=1 去掉 /service-a
  │      → lb://service-a/v3/api-docs/group-a
  │      → 返回 OpenAPI JSON
  │
  ├─③ /service-b/v3/api-docs/group-b
  │      → Gateway 路由 StripPrefix=1 去掉 /service-b
  │      → lb://service-b/v3/api-docs/group-b
  │      → 返回 OpenAPI JSON
  │
  └─④ /webjars/**, /doc.html
         → Knife4j UI 皮肤（knife4j-openapi3-ui jar 内静态资源）
```

**六层路径处理，层层对照：**

| 层 | 请求路径 | 处理后 |
|---|---|---|
| 浏览器 | `/service-a/v3/api-docs/group-a` | — |
| Gateway Route 匹配 | `/service-a/v3/api-docs/**` | 命中 |
| StripPrefix=1 | 去掉 `/service-a` | `/v3/api-docs/group-a` |
| lb://转发 | → `lb://service-a/v3/api-docs/group-a` | — |
| service-a 接收 | `/v3/api-docs/group-a` | — |
| springdoc 返回 | group="group-a" 的 OpenAPI JSON | — |
| 浏览器拿到 | 服务A 的所有 API 定义 | 渲染到 Knife4j UI |

---

## 四、依赖关系总结

```
网关 (WebFlux)
├── springdoc-openapi-starter-webflux-ui  ← /v3/api-docs 聚合端点
└── knife4j-openapi3-ui                   ← UI 皮肤（/doc.html）
    （不做聚合逻辑）

后端服务 (MVC)
└── knife4j-openapi3-jakarta-spring-boot-starter  ← 完整 Knife4j
    ├── springdoc-openapi (传递)
    ├── swagger-annotations (传递)
    └── knife4j UI (传递)
```

**一句话**：`knife4j-gateway-spring-boot-starter` 设计上想做"网关聚合+UI"一体化，实际用起来和 springdoc 冲突。正确的做法是依赖分离——springdoc 管聚合，knife4j-ui 管皮肤。
