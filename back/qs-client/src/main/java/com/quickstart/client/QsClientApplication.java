package com.quickstart.client;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.Environment;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication(scanBasePackages = "com.quickstart")
@MapperScan("com.quickstart.client.module.**.mapper")
@EnableScheduling
public class QsClientApplication {

    public static void main(String[] args) {
        ConfigurableApplicationContext context = SpringApplication.run(QsClientApplication.class, args);
        Environment env = context.getEnvironment();
//        System.out.println("Final datasource URL: " + env.getProperty("spring.datasource.url"));
    }

}
