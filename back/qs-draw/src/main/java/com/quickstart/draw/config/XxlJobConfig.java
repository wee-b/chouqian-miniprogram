package com.quickstart.draw.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Data
@Configuration
@ConfigurationProperties(prefix = "xxl.job")
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
public class XxlJobConfig {

    private Admin admin = new Admin();
    private Executor executor = new Executor();
    private String accessToken;

    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info("初始化 XXL-JOB Executor, appname={}, admin={}",
                executor.getAppname(), admin.getAddresses());

        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        xxlJobSpringExecutor.setAdminAddresses(admin.getAddresses());
        xxlJobSpringExecutor.setAppname(executor.getAppname());
        xxlJobSpringExecutor.setAddress(executor.getAddress());
        xxlJobSpringExecutor.setIp(executor.getIp());
        xxlJobSpringExecutor.setPort(executor.getPort());
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(executor.getLogpath());
        xxlJobSpringExecutor.setLogRetentionDays(executor.getLogretentiondays());
        return xxlJobSpringExecutor;
    }

    @Bean
    public RestTemplate xxlJobRestTemplate(RestTemplateBuilder builder) {
        return builder.build();
    }

    @Data
    public static class Admin {
        private String addresses;
        private String username;
        private String password;
        private Integer jobGroupId;
    }

    @Data
    public static class Executor {
        private String appname;
        private String jobHandler;
        private String address;
        private String ip;
        private int port;
        private String logpath;
        private int logretentiondays;
    }
}
