package com.quickstart.gateway.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.boot.web.context.WebServerApplicationContext;
import org.springframework.context.event.EventListener;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class StartupUrlLogger {

    private static final Logger log = LoggerFactory.getLogger(StartupUrlLogger.class);

    @EventListener(ApplicationReadyEvent.class)
    public void logUrls(ApplicationReadyEvent event) {
        if (!(event.getApplicationContext() instanceof WebServerApplicationContext webCtx)) {
            return;
        }
        Environment env = webCtx.getEnvironment();
        int port = webCtx.getWebServer().getPort();
        String ctxPath = normalizeContextPath(env.getProperty("server.servlet.context-path", "/"));
        String host = resolveHost(env);

        String base = "http://" + host + ":" + port + ctxPath;

        log.info("============================================");
        log.info("  网关服务:     {}", base);
        log.info("  接口文档:     {}/doc.html", base);
        log.info("  Nacos 控制台: http://{}:8848/nacos", resolveNacosHost(env));
        log.info("  Nacos 账号:   nacos / nacos");
        log.info("============================================");
    }

    private static String normalizeContextPath(String raw) {
        if (raw == null || raw.isBlank() || "/".equals(raw.trim())) {
            return "";
        }
        String cp = raw.trim();
        if (!cp.startsWith("/")) cp = "/" + cp;
        if (cp.endsWith("/")) cp = cp.substring(0, cp.length() - 1);
        return cp;
    }

    private static String resolveHost(Environment env) {
        String addr = env.getProperty("server.address");
        if (addr == null || addr.isBlank() || "0.0.0.0".equals(addr) || "::".equals(addr)) {
            return "localhost";
        }
        return addr;
    }

    private static String resolveNacosHost(Environment env) {
        String addr = env.getProperty("spring.cloud.nacos.discovery.server-addr", "localhost:8848");
        return addr.contains(":") ? addr.substring(0, addr.indexOf(':')) : addr;
    }
}
