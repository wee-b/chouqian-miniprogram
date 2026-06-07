package com.quickstart.client.module.role.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.system.role.Role;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface RoleMapper extends BaseMapper<Role> {
}
