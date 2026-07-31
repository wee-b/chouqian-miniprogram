package com.quickstart.draw;

import org.apache.dubbo.config.spring.context.annotation.EnableDubbo;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.quickstart")
@MapperScan({"com.quickstart.draw.**.mapper", "com.quickstart.draw.mapper"})
@EnableDubbo
public class QsDrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(QsDrawApplication.class, args);
    }

}
