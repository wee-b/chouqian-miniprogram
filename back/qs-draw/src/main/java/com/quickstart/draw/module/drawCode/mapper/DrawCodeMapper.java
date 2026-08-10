package com.quickstart.draw.module.drawCode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.drawCode.vo.DrawJoinRecordVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface DrawCodeMapper extends BaseMapper<DrawCode> {

    void batchInsert(List<DrawCode> drawCodeList);

    List<String> selectCodesByBatch(@Param("codeList") List<String> codeList);

    Page<DrawJoinRecordVO> selectJoinRecordPage(Page<DrawJoinRecordVO> page, @Param("drawId") Long drawId);

}
