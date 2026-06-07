package com.quickstart.draw.module.drawCode.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.quickstart.common.domain.winner.Winner;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface WinnerMapper extends BaseMapper<Winner> {

    void batchInsert(List<Winner> winnerList);

    List<WinnerVO> selectWinnersByDrawId(Long drawId);
}
