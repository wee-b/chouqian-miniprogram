package com.quickstart.draw.module.draw.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.draw.Draw;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface DrawMapper extends BaseMapper<Draw> {
    @Select("SELECT * FROM qs_draw WHERE share_token = #{shareToken} AND deleted_flag = 0 LIMIT 1")
    Draw selectByShareToken(@Param("shareToken") String shareToken);

    @Select("SELECT * FROM qs_draw WHERE draw_code = #{drawCode} AND deleted_flag = 0 LIMIT 1")
    Draw selectByDrawCode(@Param("drawCode") String drawCode);

    @Select("SELECT * FROM qs_draw WHERE status = 1 AND join_deadline <= NOW() AND deleted_flag = 0")
    List<Draw> selectExpiredRunningDraws();
}
