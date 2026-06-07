package com.quickstart.client.module.menu.service.impl;

import com.quickstart.client.module.menu.service.PermissionService;
import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.user.User;
import com.quickstart.client.module.user.service.UserService;
import com.quickstart.client.module.menu.mapper.MenuMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class PermissionServiceImpl implements PermissionService {

    @Resource
    private UserService userService;
    @Resource
    private MenuMapper menuMapper;

    @Override
    public LoginUser buildLoginUser(String memberCode) {
        User user = userService.findByMemberCode(memberCode);
        if (user == null || user.getStatus() == null || user.getStatus() != 1) {
            throw new IllegalArgumentException("用户不存在或已禁用");
        }
        List<String> permissions = menuMapper.selectPermissionsByUserCode(memberCode);
        if (permissions == null) {
            permissions = Collections.emptyList();
        }
        return new LoginUser(user, permissions);
    }
}
