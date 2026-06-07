package com.quickstart.draw.module.drawCode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.drawCode.DrawTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DrawTaskMapper extends BaseMapper<DrawTask> {
    @Select("SELECT * FROM qs_draw_task WHERE task_type = #{taskType} AND draw_participant_id = #{drawParticipantId} LIMIT 1")
    DrawTask selectByTypeAndParticipantId(@Param("taskType") String taskType,
                                          @Param("drawParticipantId") Long drawParticipantId);
}
