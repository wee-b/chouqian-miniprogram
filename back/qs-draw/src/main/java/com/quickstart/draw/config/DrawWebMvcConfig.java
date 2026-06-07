package com.quickstart.draw.config;

import com.quickstart.draw.interceptor.DrawAuthInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class DrawWebMvcConfig implements WebMvcConfigurer {

    private final DrawAuthInterceptor drawAuthInterceptor;

    public DrawWebMvcConfig(DrawAuthInterceptor drawAuthInterceptor) {
        this.drawAuthInterceptor = drawAuthInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(drawAuthInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/error",
                        "/doc.html",
                        "/webjars/**",
                        "/v3/api-docs/**",
                        "/swagger-resources/**");
    }
}