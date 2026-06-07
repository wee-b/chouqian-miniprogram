package com.quickstart.client.interceptor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quickstart.common.annotation.NoNeedLogin;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.client.module.menu.service.PermissionService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

@Component
@Slf4j
public class GatewayAuthInterceptor implements HandlerInterceptor {

    @Resource
    private PermissionService permissionService;
    @Resource
    private ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;
        Method method = handlerMethod.getMethod();

        // @NoNeedLogin 放行
        boolean isNoNeedLogin = method.isAnnotationPresent(NoNeedLogin.class)
                || handlerMethod.getBeanType().isAnnotationPresent(NoNeedLogin.class);
        if (isNoNeedLogin) {
            return true;
        }

        // 从 gateway 注入的 header 中读取 userCode
        String memberCode = request.getHeader("X-User-Code");
        if (!StringUtils.hasText(memberCode)) {
            writeUnauthorized(response, "未登录");
            return false;
        }

        try {
            LoginUser loginUser = permissionService.buildLoginUser(memberCode);
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(loginUser, null, loginUser.getAuthorities());
            SecurityContextHolder.getContext().setAuthentication(authentication);
            SecurityUserContext.setToken(request.getHeader("X-Token-Id"));
            return true;
        } catch (Exception e) {
            SecurityContextHolder.clearContext();
            writeUnauthorized(response, "用户信息获取失败");
            return false;
        }
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