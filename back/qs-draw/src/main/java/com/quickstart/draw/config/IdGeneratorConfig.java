package com.quickstart.draw.config;

import cn.hutool.core.lang.Snowflake;
import cn.hutool.core.util.IdUtil;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IdGeneratorConfig {

    /**
     * 全局单例雪花算法ID生成器
     * 数据中心ID(0-31) + 机器ID(0-31)，分布式环境必须不一样
     */
    @Bean
    public Snowflake snowflake() {
        // 这里的 1 和 1 要根据你的服务器配置修改
        return IdUtil.getSnowflake(1, 1);
    }
}
