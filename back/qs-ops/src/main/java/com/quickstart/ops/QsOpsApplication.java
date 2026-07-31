package com.quickstart.ops;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 运营服务启动类。
 *
 * Dubbo Provider 角色，暴露 OpLog/RiskRule/Id/Stat 四组高频内部调用接口。
 * - @EnableDubbo 开启 Dubbo 扫描，配合 application.yml 的 dubbo.scan.base-packages
 * - @MapperScan 扫描操作日志等落库 Mapper
 */
@SpringBootApplication
@EnableDubbo
@MapperScan("com.quickstart.ops.**.mapper")
public class QsOpsApplication {

    public static void main(String[] args) {
        SpringApplication.run(QsOpsApplication.class, args);
    }
}
