package com.quickstart.draw.module.draw.service.impl;

import cn.hutool.core.lang.Snowflake;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.PageResult;
import com.quickstart.common.domain.draw.Draw;

import com.quickstart.common.domain.draw.dto.DrawCreateRequest;
import com.quickstart.common.domain.draw.dto.DrawPageQueryDTO;
import com.quickstart.common.domain.draw.vo.*;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.user.User;
import com.quickstart.common.enumeration.DeletedFlagEnum;
import com.quickstart.common.exception.BusinessException;

import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.mapper.UserReadMapper;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.draw.service.DrawService;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.cache.OfficialDrawCacheService;
import com.quickstart.draw.cache.DrawRedisService;
import com.quickstart.draw.job.DrawOpenScheduleService;
import com.quickstart.draw.module.drawVerify.service.DrawVerifyService;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class DrawServiceImpl implements DrawService {


    @Resource
    private DrawMapper drawMapper;
    @Resource
    private DrawCodeMapper drawCodeMapper;
    @Resource
    private UserReadMapper userReadMapper;
    @Autowired
    private Snowflake snowflake;
    @Resource
    private DrawRedisService drawRedisService;
    @Resource
    private OfficialDrawCacheService officialDrawCacheService;
    @Resource
    private DrawVerifyService drawVerifyService;
    @Autowired
    private ObjectProvider<DrawOpenScheduleService> drawOpenScheduleServiceProvider;


    /**
     * 获取官方抽奖（三级缓存编排委托给 OfficialDrawCacheService）
     */
    @Override
    public List<DrawSmallVO> getOfficialDraw() {
        return officialDrawCacheService.get();
    }

    private void evictOfficialDrawCache() {
        officialDrawCacheService.evict();
    }

    /**
     * 创建抽签
     * @param request
     * @param publisherUserId
     * @return
     */
    @Override
    @Transactional
    public DrawVO createDraw(DrawCreateRequest request, Long publisherUserId) {
        validateDeadline(request.getJoinDeadline());

        Draw draw = new Draw();
        draw.setPublisherUserId(publisherUserId);
        draw.setTitle(request.getTitle());
        draw.setDrawCover(request.getDrawCover());
        draw.setDescription(request.getDescription());
        draw.setHasPrize(request.getHasPrize());
        draw.setDrawingWay(request.getDrawingWay());
        draw.setJoinDeadline(request.getJoinDeadline());
        draw.setMinPerson(request.getMinPerson() == null ? 0 : request.getMinPerson());
        draw.setPerCodeNum(request.getPerCodeNum() == null ? 5 : request.getPerCodeNum());
        draw.setPartLimit(request.getPartLimit() == null ? 1 : request.getPartLimit());

        // 系统自动生成
        String drawNo = String.valueOf(snowflake.nextId());
        draw.setDrawNo(drawNo);
        draw.setDrawTime(null);
        // status: 1=保存并发布 其他=草稿
        draw.setStatus(request.getStatus() != null && request.getStatus() == 1
                ? DrawConstants.DRAW_STATUS_RUNNING
                : DrawConstants.DRAW_STATUS_DRAFT);
        draw.setParticipantCount(0);
        draw.setCodeCount(0);
        draw.setDeletedFlag(0);
        draw.setCreateTime(LocalDateTime.now());
        draw.setUpdateTime(LocalDateTime.now());
        drawVerifyService.initCommitment(draw);

        drawMapper.insert(draw);

        // 如果是直接发布，同步参与次数限制到 Redis
        if (draw.getStatus() == DrawConstants.DRAW_STATUS_RUNNING) {
            drawRedisService.setPartLimit(draw.getDrawId(), draw.getPartLimit(),  draw.getJoinDeadline());
            scheduleAutoOpenIfNeeded(draw);
        }

        // 清缓存
        evictOfficialDrawCache();

        // 组装返回VO（完全按你要求的字段）
        DrawVO vo = new DrawVO();
        vo.setDrawId(draw.getDrawId());
        vo.setTitle(draw.getTitle());
        vo.setDrawCover(draw.getDrawCover());
        vo.setDescription(draw.getDescription());
        vo.setHasPrize(draw.getHasPrize());
        vo.setDrawingWay(draw.getDrawingWay());
        vo.setJoinDeadline(draw.getJoinDeadline());
        vo.setMinPerson(draw.getMinPerson());
        vo.setPerCodeNum(draw.getPerCodeNum());
        vo.setPartLimit(draw.getPartLimit());
        vo.setDrawNo(draw.getDrawNo());
        vo.setCreateTime(draw.getCreateTime());
        vo.setSeedHash(draw.getSeedHash());

        return vo;
    }
    private void validateDeadline(LocalDateTime joinDeadline) {
        if (joinDeadline == null) {
            throw new IllegalArgumentException("参与截止时间不能为空");
        }
        if (joinDeadline.isAfter(LocalDateTime.now().plusDays(DrawConstants.MAX_DRAW_EXPIRE_DAYS))) {
            throw new IllegalArgumentException("参与截止时间不能超过最大有效期");
        }
    }


    @Override
    public DrawVO getDetailDraw(Long drawId, Long currentUserId) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }
        DrawVO vo = new DrawVO();
        BeanUtils.copyProperties(draw, vo);
        vo.setIsOwner(currentUserId != null && currentUserId.equals(draw.getPublisherUserId()));
        return vo;
    }

    @Override
    @Transactional
    public DrawVO updateDraw(DrawCreateRequest request, Long userId) {
        Draw draw = drawMapper.selectById(request.getDrawId());
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }
        if (!draw.getPublisherUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能修改自己发布的抽签");
        }
        if (draw.getStatus() != DrawConstants.DRAW_STATUS_DRAFT) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "草稿状态的抽签才可修改");
        }

        validateDeadline(request.getJoinDeadline());

        draw.setTitle(request.getTitle());
        draw.setDrawCover(request.getDrawCover());
        draw.setDescription(request.getDescription());
        draw.setHasPrize(request.getHasPrize());
        draw.setDrawingWay(request.getDrawingWay());
        draw.setJoinDeadline(request.getJoinDeadline());
        draw.setMinPerson(request.getMinPerson() == null ? 0 : request.getMinPerson());
        draw.setPerCodeNum(request.getPerCodeNum() == null ? 5 : request.getPerCodeNum());
        draw.setPartLimit(request.getPartLimit() == null ? 1 : request.getPartLimit());
        draw.setUpdateTime(LocalDateTime.now());

        drawMapper.updateById(draw);

        evictOfficialDrawCache();

        DrawVO vo = new DrawVO();
        BeanUtils.copyProperties(draw, vo);
        return vo;
    }

    @Override
    @Transactional
    public void deleteDraw(Long drawId, Long userId) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }
        if (!draw.getPublisherUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能删除自己发布的抽签");
        }
        if (draw.getStatus() != DrawConstants.DRAW_STATUS_DRAFT) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "草稿状态的抽签才可删除");
        }

        draw.setDeletedFlag(1);
        draw.setUpdateTime(LocalDateTime.now());
        drawMapper.updateById(draw);

        evictOfficialDrawCache();
    }

    @Override
    @Transactional
    public void publishDraw(Long drawId, Long userId) {
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }
        if (!draw.getPublisherUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能发布自己创建的抽签");
        }
        if (draw.getStatus() != DrawConstants.DRAW_STATUS_DRAFT) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只有草稿状态的抽签才能发布");
        }

        draw.setStatus(DrawConstants.DRAW_STATUS_RUNNING);
        draw.setUpdateTime(LocalDateTime.now());
        drawVerifyService.initCommitment(draw);
        drawMapper.updateById(draw);

        // 同步参与次数限制到 Redis
        drawRedisService.setPartLimit(drawId, draw.getPartLimit(),  draw.getJoinDeadline());
        scheduleAutoOpenIfNeeded(draw);

        evictOfficialDrawCache();
    }

    private void scheduleAutoOpenIfNeeded(Draw draw) {
        DrawOpenScheduleService scheduleService = drawOpenScheduleServiceProvider.getIfAvailable();
        if (scheduleService != null) {
            scheduleService.scheduleAfterCommit(draw);
        }
    }


    @Override
    public List<Draw> listExpiredRunningDraws() {
        return drawMapper.selectExpiredRunningDraws();
    }


    @Override
    public PageResult<DrawSmallVO> queryList(DrawPageQueryDTO dto, Long userId) {

        Integer queryType = dto.getQueryType();

        LambdaQueryWrapper<Draw> drawWrapper = new LambdaQueryWrapper<>();
        drawWrapper.eq(Draw::getDeletedFlag, DeletedFlagEnum.NORMAL_STATUS.getValue());

        if (queryType == 2) {
            // 我发布的
            drawWrapper.eq(Draw::getPublisherUserId, userId);
        } else {
            // 我参与的 或 我中奖的
            // 先去参与码表中查询
            LambdaQueryWrapper<DrawCode> dcWrapper = new LambdaQueryWrapper<>();
            dcWrapper.eq(DrawCode::getUserId, userId);
            if (queryType == 3) {
                dcWrapper.isNotNull(DrawCode::getPrizeId);
            }
            dcWrapper.select(DrawCode::getDrawId);
            List<DrawCode> drawCodes = drawCodeMapper.selectList(dcWrapper);

            List<Long> drawIds = drawCodes.stream()
                    .map(DrawCode::getDrawId)
                    .distinct()
                    .toList();

            if (drawIds.isEmpty()) {
                PageResult<DrawSmallVO> emptyResult = new PageResult<>();
                emptyResult.setCurPage(dto.getPage());
                emptyResult.setTotal(0L);
                emptyResult.setData(List.of());
                return emptyResult;
            }
            drawWrapper.in(Draw::getDrawId, drawIds);
        }

        drawWrapper.orderByDesc(Draw::getCreateTime);

        Page<Draw> page = new Page<>(dto.getPage(), dto.getPageSize());
        Page<Draw> result = drawMapper.selectPage(page, drawWrapper);

        List<DrawSmallVO> voList = result.getRecords().stream().map(draw -> {
            DrawSmallVO vo = new DrawSmallVO();
            vo.setDrawId(draw.getDrawId());
            vo.setTitle(draw.getTitle());
            vo.setDrawCover(draw.getDrawCover());
            vo.setStatus(draw.getStatus());
            // 发布人不填充
//            if (draw.getPublisherUserId() != null && draw.getPublisherUserId() != 0) {
//                User publisher = userReadMapper.selectById(draw.getPublisherUserId());
//                if (publisher != null) {
//                    vo.setPublisherMCode(publisher.getUserCode());
//                    vo.setPublisherAvatar(publisher.getAvatar());
//                }
//            }
            return vo;
        }).toList();

        PageResult<DrawSmallVO> pageResult = new PageResult<>();
        pageResult.setCurPage(dto.getPage());
        pageResult.setTotal(result.getTotal());
        pageResult.setData(voList);
        return pageResult;
    }

    @Override
    public DrawStatisticsVO queryStatistics(String currentMemberCode) {

        User user = userReadMapper.selectByMemberCode(currentMemberCode);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        Long userId = user.getUserId();

        DrawStatisticsVO vo = new DrawStatisticsVO();

        // 我发布的
        LambdaQueryWrapper<Draw> publishedWrapper = new LambdaQueryWrapper<>();
        publishedWrapper.eq(Draw::getPublisherUserId, userId);
        publishedWrapper.eq(Draw::getDeletedFlag, DeletedFlagEnum.NORMAL_STATUS.getValue());
        Long l = drawMapper.selectCount(publishedWrapper);
        vo.setPublishedCount(l);

        // 我参与的
        LambdaQueryWrapper<DrawCode> joinedWrapper = new LambdaQueryWrapper<>();
        joinedWrapper.eq(DrawCode::getUserId, userId);
        joinedWrapper.select(DrawCode::getDrawId);
        Long joinedCount = drawCodeMapper.selectList(joinedWrapper).stream()
                .map(DrawCode::getDrawId)
                .distinct()
                .count();
        vo.setJoinedCount(joinedCount);

        // 我中奖的
        LambdaQueryWrapper<DrawCode> rewardedWrapper = new LambdaQueryWrapper<>();
        rewardedWrapper.eq(DrawCode::getUserId, userId);
        rewardedWrapper.isNotNull(DrawCode::getPrizeId);
        rewardedWrapper.select(DrawCode::getDrawId);
        Long rewardedCount = drawCodeMapper.selectList(rewardedWrapper).stream()
                .map(DrawCode::getDrawId)
                .distinct()
                .count();
        vo.setRewardCount(rewardedCount);

        return vo;
    }


}
