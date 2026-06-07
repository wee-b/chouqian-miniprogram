package com.quickstart.client.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiClientConfig {

    @Bean
    public OpenAPI quickStartOpenApi() {
        return new OpenAPI()
                .info(new Info().title("QuickStart API").version("1.0"));
    }

    @Bean
    public GroupedOpenApi userGroup() {
        return GroupedOpenApi.builder()
                .group("user")
                .displayName("用户与系统")
                .pathsToMatch("/**")
                .packagesToScan("com.quickstart.client")
                .build();
    }

}
