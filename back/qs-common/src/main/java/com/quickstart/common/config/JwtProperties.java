package com.quickstart.common.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "qs.jwt")
public class JwtProperties {
    private String secret = "CHANGE_ME_TO_A_LONG_RANDOM_SECRET_KEY";
    private long expireSeconds = 7200;
}
