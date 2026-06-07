package com.quickstart.common.domain.user;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("qs_user")
public class User {
    @TableId(value = "user_id", type = IdType.AUTO)
    private Long userId;
    private String userCode;
    private Integer userType;
    private String userName;
    private String phone;
    private String email;
    private String password;
    private String avatar;
    private Integer gender;
    private LocalDateTime birthday;
    private LocalDateTime lastLoginTime;
    private String lastLoginIp;
    private Integer registerSource;
    private String openid;
    private String unionid;
    private Integer loginCount;
    private BigDecimal memberLevelScore;
    private Integer creditScore;
    private Integer status;
    private Integer deletedFlag;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
