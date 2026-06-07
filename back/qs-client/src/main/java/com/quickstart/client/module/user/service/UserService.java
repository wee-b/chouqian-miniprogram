package com.quickstart.client.module.user.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.quickstart.common.domain.user.dto.*;
import com.quickstart.common.domain.user.User;

public interface UserService {
    IPage<User> pageAdminUsers(long pageNo, long pageSize, String userName, String phone, Integer status);

    User findById(Long userId);

    User findByMemberCode(String memberCode);

    User findByPhone(String phone);

    User createByAdmin(AdminUserCreateRequest request);

    User registerClient(ClientRegisterDTO request);

    User adminLogin(AdminLoginRequest request);

    User clientLogin(ClientLoginDTO request);

    boolean updateStatusByMemberCode(String memberCode, Integer status);

    void updateInfo(String userCode, UpdateInfoDTO updateInfoDTO);
}
