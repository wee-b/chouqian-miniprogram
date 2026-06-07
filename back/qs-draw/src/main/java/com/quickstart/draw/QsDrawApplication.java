package com.quickstart.draw;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.quickstart")
@MapperScan({"com.quickstart.draw.**.mapper", "com.quickstart.draw.mapper"})
public class QsDrawApplication {

    public static void main(String[] args) {
        SpringApplication.run(QsDrawApplication.class, args);
    }

}
