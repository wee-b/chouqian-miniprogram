package com.quickstart.draw.module.prize.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.domain.prize.Prize;
import com.quickstart.common.domain.prize.dto.PrizeDTO;
import com.quickstart.draw.module.prize.mapper.PrizeMapper;
import com.quickstart.draw.module.prize.service.PrizeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class PrizeServiceImpl implements PrizeService {

    @Autowired
    private PrizeMapper prizeMapper;

    @Override
    public List<Prize> getPrizesByDrawId(Long drawId) {

        LambdaQueryWrapper<Prize> queryWrapper = new LambdaQueryWrapper<Prize>();
        queryWrapper.eq(Prize::getDrawId, drawId);
        queryWrapper.orderByAsc(Prize::getPrizeType); // 一等奖到九等奖

        List<Prize> prizes = prizeMapper.selectList(queryWrapper);

        return prizes;
    }

    @Override
    public void batchAddPrize(List<PrizeDTO> prizes) {

    }

    @Override
    public void updatePrize(PrizeDTO dto) {

    }

    @Override
    public void batchDeletePrize(List<Long> prizeIds) {

    }
}
