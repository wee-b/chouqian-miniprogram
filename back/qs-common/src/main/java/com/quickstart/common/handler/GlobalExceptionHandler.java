package com.quickstart.common.handler;

import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.exception.BusinessException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
// 加上这一行：只有在 Servlet 环境（Web MVC）下才加载
@ConditionalOnWebApplication(type = ConditionalOnWebApplication.Type.SERVLET)
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseDTO<Void> handleValidException(MethodArgumentNotValidException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        String message = fieldError == null ? "参数校验失败" : fieldError.getDefaultMessage();
        log.warn("Validation failed: {}", message);
        return ResponseDTO.userErrorParam(message);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseDTO<Void> handleConstraintException(ConstraintViolationException ex) {
        log.warn("Constraint violation: {}", ex.getMessage());
        return ResponseDTO.userErrorParam(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseDTO<Void> handleIllegalArgument(IllegalArgumentException ex) {
        log.warn("Illegal argument: {}", ex.getMessage());
        return ResponseDTO.userErrorParam(ex.getMessage());
    }

    @ExceptionHandler(AuthenticationCredentialsNotFoundException.class)
    public ResponseDTO<Void> handleAuthenticationException(AuthenticationCredentialsNotFoundException ex) {
        log.warn("Authentication failed: {}", ex.getMessage());
        return ResponseDTO.error(com.quickstart.common.domain.ErrorCode.UNAUTHORIZED, ex.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseDTO<Void> handleAccessDeniedException(AccessDeniedException ex) {
        log.warn("Access denied: {}", ex.getMessage());
        return ResponseDTO.error(com.quickstart.common.domain.ErrorCode.FORBIDDEN, "没有访问权限");
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseDTO<Void> handleBusinessException(BusinessException ex) {
        log.warn("Business exception: {}", ex.getMessage());
        return new ResponseDTO<>(ex.getCode(), ex.getLevel(), false, ex.getMsg());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public void handleNoResourceFoundException(NoResourceFoundException e) {
        // 静态资源不存在，不处理，不打印日志
        log.warn("静态资源不存在: {}", e);
    }

    @ExceptionHandler(Exception.class)
    public ResponseDTO<Void> handleException(Exception ex) {
        log.error("System exception: {}", ex.getMessage(), ex);
        return ResponseDTO.error(com.quickstart.common.domain.ErrorCode.INTERNAL_SERVER_ERROR, ex.getMessage());
    }
}
