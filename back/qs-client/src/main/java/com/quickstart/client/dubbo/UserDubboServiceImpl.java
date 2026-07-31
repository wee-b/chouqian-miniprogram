package com.quickstart.client.dubbo;

import com.quickstart.api.dto.UserDTO;
import com.quickstart.api.dubbo.UserDubboService;
import com.quickstart.common.domain.user.User;
import com.quickstart.client.module.user.service.UserService;
import org.apache.dubbo.config.annotation.DubboService;

/**
 * 用户查询 Dubbo Provider 实现。
 *
 * 由 qs-client 暴露，供 qs-draw / qs-ops 在参与抽奖、开奖通知等链路查询用户信息。
 * 参与抽奖热路径上会调用 checkUserStatus 做快速校验。
 *
 * 注意：仅返回查询所需字段，不暴露 password/openid 等敏感信息。
 */
@DubboService
public class UserDubboServiceImpl implements UserDubboService {

    private final UserService userService;

    public UserDubboServiceImpl(UserService userService) {
        this.userService = userService;
    }

    @Override
    public UserDTO getUserByCode(String userCode) {
        User user = userService.findByMemberCode(userCode);
        return toDTO(user);
    }

    @Override
    public UserDTO getUserById(Long userId) {
        User user = userService.findById(userId);
        return toDTO(user);
    }

    @Override
    public boolean checkUserStatus(Long userId) {
        User user = userService.findById(userId);
        return user != null && Integer.valueOf(1).equals(user.getStatus());
    }

    private UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        UserDTO dto = new UserDTO();
        dto.setUserId(user.getUserId());
        dto.setUserCode(user.getUserCode());
        dto.setPhone(user.getPhone());
        // User 实体用 userName，DTO 统一用 nickName 对外
        dto.setNickName(user.getUserName());
        dto.setAvatar(user.getAvatar());
        dto.setStatus(user.getStatus());
        return dto;
    }
}
