package com.quickstart.client.module;

import com.quickstart.common.annotation.NoNeedLogin;
import com.quickstart.common.security.SecurityUserContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "测试接口")
@RestController
public class TestController {

    @NoNeedLogin
    @GetMapping("/test/testConnection")
    @Operation(summary = "测试后端连接")
    public String testConnection(){
        return "hello world";
    }

    @GetMapping("/test/testLogin")
    @Operation(summary = "测试登录拦截器")
    public String testLogin(){
        String memberCode = SecurityUserContext.getCurrentMemberCode();
        return "you are logged in, memberCode: " + memberCode;
    }
}
