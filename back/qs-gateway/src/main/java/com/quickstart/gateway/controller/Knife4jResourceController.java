package com.quickstart.gateway.controller;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaTypeFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.concurrent.TimeUnit;

@RestController
public class Knife4jResourceController {

    private static final String RESOURCE_ROOT = "META-INF/resources/";

    @GetMapping({"/doc.html", "/favicon.ico"})
    public Mono<ResponseEntity<Resource>> rootResource(org.springframework.web.server.ServerWebExchange exchange) {
        String path = exchange.getRequest().getPath().pathWithinApplication().value().substring(1);
        return resource(path, false);
    }

    @GetMapping({"/webjars/{*path}", "/img/{*path}"})
    public Mono<ResponseEntity<Resource>> nestedResource(@PathVariable("path") String path,
                                                        org.springframework.web.server.ServerWebExchange exchange) {
        String prefix = exchange.getRequest().getPath().pathWithinApplication().value().startsWith("/img/")
                ? "img/"
                : "webjars/";
        return resource(prefix + stripLeadingSlash(path), true);
    }

    private Mono<ResponseEntity<Resource>> resource(String path, boolean cache) {
        String cleanPath = StringUtils.cleanPath(path);
        if (cleanPath.contains("..")) {
            return Mono.just(ResponseEntity.badRequest().build());
        }

        ClassPathResource resource = new ClassPathResource(RESOURCE_ROOT + cleanPath);
        if (!resource.exists()) {
            return Mono.just(ResponseEntity.notFound().build());
        }

        ResponseEntity.BodyBuilder builder = ResponseEntity.ok();
        MediaTypeFactory.getMediaType(resource).ifPresent(builder::contentType);
        if (cache) {
            builder.cacheControl(CacheControl.maxAge(7, TimeUnit.DAYS).cachePublic());
        }
        return Mono.just(builder.body(resource));
    }

    private String stripLeadingSlash(String path) {
        return path.startsWith("/") ? path.substring(1) : path;
    }
}
