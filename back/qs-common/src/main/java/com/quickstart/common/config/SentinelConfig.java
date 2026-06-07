package com.quickstart.common.config;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SentinelConfig {

    @PostConstruct
    public void init() {
        // 让 Sentinel 知道规则从哪里加载。
        // 当前阶段先用注解方式定义规则，后续可从 Nacos 配置中心动态拉取。
        System.setProperty("csp.sentinel.app.type", "1"); // 1 = 私有应用
    }
}
