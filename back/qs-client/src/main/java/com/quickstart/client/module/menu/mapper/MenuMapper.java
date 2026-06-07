package com.quickstart.client.module.menu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.system.menu.Menu;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface MenuMapper extends BaseMapper<Menu> {

    @Select("""
            SELECT DISTINCT m.perms
            FROM qs_menu m
            INNER JOIN qs_role_menu rm ON rm.menu_id = m.menu_id
            INNER JOIN qs_user_role ur ON ur.role_id = rm.role_id
            INNER JOIN qs_user u ON u.user_id = ur.user_id
            WHERE u.user_code = #{userCode}
              AND u.deleted_flag = 0
              AND u.status = 1
              AND m.status = 1
              AND m.perms IS NOT NULL
              AND m.perms <> ''
            """)
    List<String> selectPermissionsByUserCode(@Param("userCode") String userCode);
}
