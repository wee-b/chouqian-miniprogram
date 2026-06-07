package com.quickstart.client.module.menu.service;

import com.quickstart.common.domain.LoginUser;

public interface PermissionService {

    LoginUser buildLoginUser(String memberCode);
}
