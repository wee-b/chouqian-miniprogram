package com.quickstart.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@EnableDiscoveryClient
@ComponentScan(basePackages = {
        "com.quickstart.gateway",
        "com.quickstart.common"  // 关键：扫描公共模块
})
public class QsGatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(QsGatewayApplication.class, args);
    }

}