package com.quickstart.draw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.user.User;
import org.apache.ibatis.annotations.Select;

public interface UserReadMapper extends BaseMapper<User> {

    @Select("SELECT * FROM qs_user WHERE user_code = #{memberCode} AND deleted_flag = 0")
    User selectByMemberCode(String memberCode);
}
