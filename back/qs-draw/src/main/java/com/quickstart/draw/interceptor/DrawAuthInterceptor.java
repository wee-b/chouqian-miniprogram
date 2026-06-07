package com.quickstart.draw.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickstart.common.annotation.NoNeedLogin;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.user.User;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.draw.mapper.UserReadMapper;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;
import java.util.Collections;

@Component
public class DrawAuthInterceptor implements HandlerInterceptor {

    @Resource
    private UserReadMapper userReadMapper;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) throws Exception {

        if (!(handler instanceof HandlerMethod hm)) {
            return true;
        }

        Method method = hm.getMethod();
        if (method.isAnnotationPresent(NoNeedLogin.class)
                || hm.getBeanType().isAnnotationPresent(NoNeedLogin.class)) {
            return true;
        }

        String memberCode = request.getHeader("X-User-Code");
        if (!StringUtils.hasText(memberCode)) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        User user = userReadMapper.selectByMemberCode(memberCode);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            writeUnauthorized(response, "用户不存在或已禁用");
            return false;
        }

        LoginUser loginUser = new LoginUser(user, Collections.emptyList());
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
        SecurityUserContext.setToken(request.getHeader("X-Token-Id"));
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response,
                                Object handler, Exception ex) {
        SecurityUserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String msg) throws Exception {
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        response.setCharacterEncoding("UTF-8");
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ResponseDTO<Void> body = new ResponseDTO<>(ErrorCode.UNAUTHORIZED, false, msg, null);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}