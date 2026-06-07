package com.quickstart.draw.module.prize.service;

import com.quickstart.common.domain.prize.Prize;
import com.quickstart.common.domain.prize.dto.PrizeDTO;

import java.util.List;

public interface PrizeService {

    List<Prize> getPrizesByDrawId(Long drawId);

    void batchAddPrize(List<PrizeDTO> prizes);

    void updatePrize(PrizeDTO dto);

    void batchDeletePrize(List<Long> prizeIds);
}
