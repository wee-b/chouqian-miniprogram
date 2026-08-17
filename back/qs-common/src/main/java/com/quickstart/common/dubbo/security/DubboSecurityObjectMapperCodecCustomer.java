package com.quickstart.common.dubbo.security;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.user.User;
import org.apache.dubbo.spring.security.jackson.ObjectMapperCodec;
import org.apache.dubbo.spring.security.jackson.ObjectMapperCodecCustomer;

public class DubboSecurityObjectMapperCodecCustomer implements ObjectMapperCodecCustomer {

    @Override
    public void customize(ObjectMapperCodec objectMapperCodec) {
        objectMapperCodec.configureMapper(objectMapper -> {
            objectMapper.addMixIn(LoginUser.class, LoginUserMixin.class);
            objectMapper.addMixIn(User.class, UserMixin.class);
        });
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class LoginUserMixin {
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    private abstract static class UserMixin {
    }
}
