package com.quickstart.draw.module.draw.service;

import com.quickstart.common.domain.PageResult;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.draw.dto.DrawCreateRequest;
import com.quickstart.common.domain.draw.dto.DrawPageQueryDTO;
import com.quickstart.common.domain.draw.vo.*;

import java.util.List;

public interface DrawService {

    List<DrawSmallVO> getOfficialDraw();

    DrawVO createDraw(DrawCreateRequest request, Long publisherUserId);

    DrawVO getDetailDraw(Long drawId, Long currentUserId);

    DrawVO updateDraw(DrawCreateRequest request, Long userId);

    void deleteDraw(Long drawId, Long userId);

    void publishDraw(Long drawId, Long userId);


    List<Draw> listExpiredRunningDraws();


    PageResult<DrawSmallVO> queryList(DrawPageQueryDTO dto, Long userid);

    DrawStatisticsVO queryStatistics(String currentMemberCode);
}
