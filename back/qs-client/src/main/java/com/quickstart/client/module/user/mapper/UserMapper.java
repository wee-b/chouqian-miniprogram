package com.quickstart.client.module.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.user.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("SELECT COALESCE(MAX(CAST(SUBSTRING(user_code, 3) AS UNSIGNED)), -1) " +
            "FROM qs_user WHERE user_code LIKE 'QS%'")
    Long selectMaxMemberCodeNumber();
}
