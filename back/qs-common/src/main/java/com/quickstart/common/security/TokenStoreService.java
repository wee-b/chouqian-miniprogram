package com.quickstart.common.security;

import com.quickstart.common.config.JwtProperties;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.time.Duration;

@Component
public class TokenStoreService {
    private static final String TOKEN_KEY_PREFIX = "qs:auth:token:";
    private final StringRedisTemplate stringRedisTemplate;
    private final JwtProperties jwtProperties;

    public TokenStoreService(StringRedisTemplate stringRedisTemplate, JwtProperties jwtProperties) {
        this.stringRedisTemplate = stringRedisTemplate;
        this.jwtProperties = jwtProperties;
    }

    public void save(String tokenId, String memberCode) {
        stringRedisTemplate.opsForValue()
                .set(TOKEN_KEY_PREFIX + tokenId, memberCode, Duration.ofSeconds(jwtProperties.getExpireSeconds()));
    }

    public boolean isValid(String tokenId, String memberCode) {
        if (!StringUtils.hasText(tokenId) || !StringUtils.hasText(memberCode)) {
            return false;
        }
        String value = stringRedisTemplate.opsForValue().get(TOKEN_KEY_PREFIX + tokenId);
        return memberCode.equals(value);
    }

    /**
     * 注销token：从Redis中删除对应的token记录
     * @param tokenId 要注销的token唯一标识
     * @return 注销是否成功
     */
    public boolean logout(String tokenId) {
        if (!StringUtils.hasText(tokenId)) {
            return false;
        }
        // 拼接Redis真实key
        String key = TOKEN_KEY_PREFIX + tokenId;
        // 删除key并返回删除结果
        return Boolean.TRUE.equals(stringRedisTemplate.delete(key));
    }
}
