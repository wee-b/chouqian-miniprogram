package com.quickstart.draw.module.drawCode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.PageResult;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.drawCode.dto.DrawJoinRecordPageDTO;
import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.domain.drawCode.vo.DrawJoinRecordVO;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.module.drawCode.mapper.WinnerMapper;
import com.quickstart.draw.module.drawCode.service.DrawQueryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 查询域实现
 */
@Service
public class DrawQueryServiceImpl implements DrawQueryService {

    @Autowired
    private DrawMapper drawMapper;
    @Autowired
    private DrawCodeMapper drawCodeMapper;
    @Autowired
    private WinnerMapper winnerMapper;

    @Override
    public List<DrawCodeVO> getMyCodes(Long drawId, Long userId) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }

        LambdaQueryWrapper<DrawCode> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DrawCode::getDrawId, draw.getDrawId());
        queryWrapper.eq(DrawCode::getUserId, userId);

        List<DrawCode> codes = drawCodeMapper.selectList(queryWrapper);
        if (codes.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "您还没有参加过该抽签");
        }

        LocalDateTime now = LocalDateTime.now();
        boolean isOpen = Optional.ofNullable(draw.getDrawTime())
                .map(time -> time.isBefore(now))
                .orElse(false);

        return codes.stream().map(one -> {
            DrawCodeVO vo = new DrawCodeVO();
            vo.setCodeValue(one.getCodeValue());
            String desc;
            if (one.getPrizeId() != null) {
                vo.setPrizeId(one.getPrizeId());
                desc = "已中奖";
            } else if (isOpen) {
                desc = "未中奖";
            } else {
                desc = "未开奖";
            }
            vo.setDesc(desc);
            return vo;
        }).collect(Collectors.toList());
    }

    @Override
    public List<WinnerVO> getWinners(Long drawId) {
        return winnerMapper.selectWinnersByDrawId(drawId);
    }

    @Override
    public PageResult<DrawJoinRecordVO> queryJoinRecords(DrawJoinRecordPageDTO dto) {
        Draw draw = drawMapper.selectById(dto.getDrawId());
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }

        Page<DrawJoinRecordVO> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<DrawJoinRecordVO> result = drawCodeMapper.selectJoinRecordPage(page, dto.getDrawId());

        PageResult<DrawJoinRecordVO> pageResult = new PageResult<>();
        pageResult.setCurPage(dto.getPage());
        pageResult.setTotal(result.getTotal());
        pageResult.setData(result.getRecords());
        return pageResult;
    }
}
