package com.quickstart.client.module.user.service.impl;

import com.quickstart.common.domain.user.dto.AdminLoginRequest;
import com.quickstart.common.domain.user.dto.ClientLoginDTO;
import com.quickstart.common.domain.user.vo.LoginResponse;
import com.quickstart.common.domain.user.vo.UserInfoVO;
import com.quickstart.common.domain.user.User;
import com.quickstart.common.security.JwtTokenService;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.common.security.TokenStoreService;
import com.quickstart.client.module.user.service.AuthService;
import com.quickstart.client.module.user.service.UserService;
import io.jsonwebtoken.Claims;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {
    private final UserService userService;
    private final JwtTokenService jwtTokenService;
    private final TokenStoreService tokenStoreService;

    public AuthServiceImpl(UserService userService, JwtTokenService jwtTokenService, TokenStoreService tokenStoreService) {
        this.userService = userService;
        this.jwtTokenService = jwtTokenService;
        this.tokenStoreService = tokenStoreService;
    }

    @Override
    public LoginResponse adminLogin(AdminLoginRequest request) {
        User user = userService.adminLogin(request);
        String token = jwtTokenService.createToken(user.getUserCode());
        Claims claims = jwtTokenService.parseClaims(token);
        tokenStoreService.save(claims.getId(), user.getUserCode());
        return new LoginResponse(token, UserInfoVO.fromEntity(user));
    }

    @Override
    public LoginResponse clientLogin(ClientLoginDTO request) {
        User user = userService.clientLogin(request);
        String token = jwtTokenService.createToken(user.getUserCode());
        Claims claims = jwtTokenService.parseClaims(token);
        tokenStoreService.save(claims.getId(), user.getUserCode());
        return new LoginResponse(token, UserInfoVO.fromEntity(user));
    }

    @Override
    public UserInfoVO currentUser(String memberCode) {

        User user = userService.findByMemberCode(memberCode);
        if (user == null) {
            throw new IllegalArgumentException("用户不存在或登录已失效");
        }
        return UserInfoVO.fromEntity(user);
    }

    /**
     * 用户登出：从Redis中删除token，使当前登录失效
     * 优先从上下文获取token，无则根据userCode处理
     */
    @Override
    public void logout(String userCode) {
        // 1. 从安全上下文获取当前请求的token（推荐：精准注销当前登录的token）
        String token = SecurityUserContext.getToken();

        // 2. 如果能获取到token，解析出tokenId并注销
        if (token != null && !token.isEmpty()) {
            Claims claims = jwtTokenService.parseClaims(token);
            String tokenId = claims.getId();
            // 调用我们之前写的注销方法，删除Redis中的token
            tokenStoreService.logout(tokenId);
        }

        // 3. 清空当前线程的用户上下文信息
        SecurityUserContext.clear();
    }
}
