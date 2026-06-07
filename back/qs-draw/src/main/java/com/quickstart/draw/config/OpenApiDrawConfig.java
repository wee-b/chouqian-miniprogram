package com.quickstart.draw.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiDrawConfig {

    @Bean
    public OpenAPI drawOpenApi() {
        return new OpenAPI()
                .info(new Info().title("QuickStart API").version("1.0"));
    }

    @Bean
    public GroupedOpenApi drawGroup() {
        return GroupedOpenApi.builder()
                .group("draw")
                .displayName("抽奖")
                .pathsToMatch("/**")
                .packagesToScan("com.quickstart.draw")
                .build();
    }
}