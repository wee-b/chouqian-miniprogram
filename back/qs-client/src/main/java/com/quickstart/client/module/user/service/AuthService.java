package com.quickstart.client.module.user.service;

import com.quickstart.common.domain.user.dto.AdminLoginRequest;
import com.quickstart.common.domain.user.dto.ClientLoginDTO;
import com.quickstart.common.domain.user.vo.LoginResponse;
import com.quickstart.common.domain.user.vo.UserInfoVO;

public interface AuthService {
    LoginResponse adminLogin(AdminLoginRequest request);

    LoginResponse clientLogin(ClientLoginDTO request);

    UserInfoVO currentUser(String memberCode);

    void logout(String userCode);
}
