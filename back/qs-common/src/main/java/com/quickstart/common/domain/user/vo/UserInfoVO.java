package com.quickstart.common.domain.user.vo;

import com.quickstart.common.domain.user.User;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserInfoVO {
    private String memberCode;
    private Integer userType;
    private String userName;
    private String phone;
    private String email;
    private Integer status;
    private Integer loginCount;
    private LocalDateTime createTime;

    public static UserInfoVO fromEntity(User user) {
        UserInfoVO view = new UserInfoVO();
        view.setMemberCode(user.getUserCode());
        view.setUserType(user.getUserType());
        view.setUserName(user.getUserName());
        view.setPhone(user.getPhone());
        view.setEmail(user.getEmail());
        view.setStatus(user.getStatus());
        view.setLoginCount(user.getLoginCount());
        view.setCreateTime(user.getCreateTime());
        return view;
    }
}
