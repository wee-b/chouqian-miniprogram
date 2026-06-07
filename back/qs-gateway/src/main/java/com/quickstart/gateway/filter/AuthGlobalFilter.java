package com.quickstart.gateway.filter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickstart.common.config.JwtProperties;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.ResponseDTO;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 *
 *
 * 请求 → Gateway（JWT 验签 + Redis 校验）→ 转发 header
 *         ↓
 * qs-client → 新拦截器（读 X-User-Code header → 查 DB 权限 → 设 SecurityContext）
 *
 */
@Slf4j
@Component
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    /** 仅拦截静态资源/文档路径，业务接口无论有无 token 都放行 */
    private static final List<String> STATIC_PATHS = List.of(
            "/doc.html",
            "/webjars/**",
            "/v3/api-docs/**",
            "/swagger-resources/**",
            "/client/v3/api-docs/**",
            "/draw/v3/api-docs/**",
            "/favicon.ico"
    );

    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final JwtProperties jwtProperties;
    private final ReactiveStringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    private static final String TOKEN_KEY_PREFIX = "qs:auth:token:";

    public AuthGlobalFilter(JwtProperties jwtProperties, ReactiveStringRedisTemplate redisTemplate,
                            ObjectMapper objectMapper) {
        this.jwtProperties = jwtProperties;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getPath().value();

        // 1. OPTIONS（CORS 预检）直接放行
        if (request.getMethod() == HttpMethod.OPTIONS) {
            return chain.filter(exchange);
        }

        // 2. 静态资源/文档直接放行
        if (isStaticResource(path)) {
            return chain.filter(exchange);
        }

        // 3. 提取 token —— 没有 token 就透传，交给下游 @NoNeedLogin 决定
        String token = extractToken(request);
        if (token == null) {
            return chain.filter(exchange);
        }

        // 4. 解析 JWT（有 token 但无效 → 401，让调用方知道 token 有问题）
        Claims claims;
        try {
            claims = Jwts.parser()
                    .verifyWith(signingKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (Exception e) {
            return writeUnauthorized(exchange, "token 无效或已过期");
        }

        String memberCode = claims.getSubject();
        String tokenId = claims.getId();

        // 5. Redis 校验 token 有效性
        return redisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + tokenId)
                .flatMap(redisMemberCode -> {
                    if (!memberCode.equals(redisMemberCode)) {
                        return writeUnauthorized(exchange, "登录已失效");
                    }
                    // 6. token 有效，注入 header 给下游
                    ServerHttpRequest modifiedRequest = request.mutate()
                            .header("X-User-Code", memberCode)
                            .header("X-Token-Id", tokenId)
                            .build();
                    return chain.filter(exchange.mutate().request(modifiedRequest).build());
                })
                .switchIfEmpty(writeUnauthorized(exchange, "登录已失效"));
    }

    @Override
    public int getOrder() {
        return -100;
    }

    // ===== 辅助方法 ======

    private boolean isStaticResource(String path) {
        return STATIC_PATHS.stream().anyMatch(p -> pathMatcher.match(p, path));
    }

    private String extractToken(ServerHttpRequest request) {
        String authHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (StringUtils.hasText(authHeader) && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        return null;
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(jwtProperties.getSecret().getBytes(StandardCharsets.UTF_8));
    }

    private Mono<Void> writeUnauthorized(ServerWebExchange exchange, String msg) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ResponseDTO<Void> body = new ResponseDTO<>(ErrorCode.UNAUTHORIZED.getCode(),
                ErrorCode.UNAUTHORIZED.getLevel(), false, msg, null);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (JsonProcessingException e) {
            return response.setComplete();
        }
    }
}
