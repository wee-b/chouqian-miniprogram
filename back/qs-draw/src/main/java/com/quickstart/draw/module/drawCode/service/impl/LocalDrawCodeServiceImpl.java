package com.quickstart.draw.module.drawCode.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.prize.Prize;
import com.quickstart.common.domain.user.User;
import com.quickstart.common.domain.winner.Winner;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.constant.RedisConstant;
import com.quickstart.common.domain.drawCode.DrawCode;
import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.mapper.UserReadMapper;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import com.quickstart.draw.module.drawCode.mapper.WinnerMapper;
import com.quickstart.draw.module.drawCode.service.DrawCodeService;
import com.quickstart.draw.module.prize.mapper.PrizeMapper;
import com.quickstart.draw.util.DrawCodeGenerator;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@ConditionalOnProperty(value = "qs.rabbitmq.enabled", havingValue = "false")
public class LocalDrawCodeServiceImpl implements DrawCodeService {


    // 注入 线程安全单例 唯一码生成器
    @Resource
    private DrawCodeGenerator drawCodeGenerator;

    @Autowired
    private DrawCodeMapper drawCodeMapper;
    @Autowired
    private DrawMapper drawMapper;
    @Autowired
    private PrizeMapper prizeMapper;
    @Autowired
    private WinnerMapper winnerMapper;
    @Autowired
    private UserReadMapper userReadMapper;
    @Autowired
    private StringRedisTemplate redisTemplate;


    /**
     * 参与抽奖
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public List<String> joinDraw(Long drawId, Long userId) {
        // 1. 查询抽签活动并校验是否可参与
        Draw draw = drawMapper.selectById(drawId);
        ensureJoinable(draw);

        // 2. 检查参与次数上限（Redis 原子计数）
        checkAndIncrementPartCount(drawId, userId);

        // 3. 获取每人可生成的码数量
        int perCodeNum = draw.getPerCodeNum();
        if (perCodeNum <= 0) {
            throw new IllegalArgumentException("每人参与码数量配置错误");
        }

        // ========== 核心：调用线程安全的单例生成器 ==========
        List<String> codeValues = drawCodeGenerator.batchGenerate(perCodeNum);

        // 4. 组装批量插入数据
        List<DrawCode> drawCodeList = new ArrayList<>(codeValues.size());
        LocalDateTime now = LocalDateTime.now();
        for (String codeValue : codeValues) {
            DrawCode code = new DrawCode();
            code.setDrawId(drawId);
            code.setUserId(userId);
            code.setCodeValue(codeValue);
            code.setCreateTime(now);
            drawCodeList.add(code);
        }

        // 5. 批量插入
        drawCodeMapper.batchInsert(drawCodeList);

        // 6. 更新抽签统计
        draw.setParticipantCount(draw.getParticipantCount() + 1);
        draw.setCodeCount(draw.getCodeCount() + perCodeNum);
        drawMapper.updateById(draw);

        return codeValues;
    }

    /**
     * 检查并递增参与次数（Redis 原子操作），超过上限则拒绝
     */
    private void checkAndIncrementPartCount(Long drawId, Long userId) {
        String limitKey = RedisConstant.PART_LIMIT_PREFIX + ":" + drawId;
        String limitStr = redisTemplate.opsForValue().get(limitKey);
        if (limitStr == null) {
            return; // 未设置上限，跳过检查
        }
        int limit = Integer.parseInt(limitStr);
        String countKey = RedisConstant.PART_COUNT_PREFIX + ":" + drawId;
        String userIdStr = String.valueOf(userId);

        Long newCount = redisTemplate.opsForHash().increment(countKey, userIdStr, 1);
        if (newCount != null && newCount > limit) {
            redisTemplate.opsForHash().increment(countKey, userIdStr, -1);
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "您已达到参与次数上限");
        }
    }

    private void ensureJoinable(Draw draw) {
        if(draw == null){
            throw new IllegalArgumentException("抽奖不存在");
        }
        // 校验抽奖状态：必须是进行中
        if (draw.getStatus() == null || draw.getStatus() != DrawConstants.DRAW_STATUS_RUNNING) {
            throw new IllegalArgumentException("当前抽奖已结束，无法参与");
        }
        // 校验参与截止时间：未超时才能参与
        if (draw.getJoinDeadline() != null && draw.getJoinDeadline().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("抽奖已截止，无法参与");
        }
    }

    /**
     * 查询我的抽奖码
     */
    @Override
    public List<DrawCodeVO> getMyCodes(Long drawId, Long userId) {
        Draw draw = drawMapper.selectById(drawId);

        LambdaQueryWrapper<DrawCode> dcLambdaQueryWrapper = new LambdaQueryWrapper<>();
        dcLambdaQueryWrapper.eq(DrawCode::getDrawId, draw.getDrawId());
        dcLambdaQueryWrapper.eq(DrawCode::getUserId, userId);

        List<DrawCode> codes = drawCodeMapper.selectList(dcLambdaQueryWrapper);
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
    @Transactional(rollbackFor = Exception.class)
    public void openDraw(Long drawId, Long userId) {
        // 1. 查询抽签并校验
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "抽签不存在");
        }
        if (!draw.getPublisherUserId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能开奖自己发布的抽签");
        }
        if (draw.getStatus() != DrawConstants.DRAW_STATUS_RUNNING) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "只有进行中的抽签才能开奖");
        }

        // 2. 查询所有抽签码（未中奖的）
        LambdaQueryWrapper<DrawCode> codeWrapper = new LambdaQueryWrapper<>();
        codeWrapper.eq(DrawCode::getDrawId, drawId);
        codeWrapper.isNull(DrawCode::getPrizeId);
        List<DrawCode> codes = drawCodeMapper.selectList(codeWrapper);

        if (codes.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "没有可开奖的抽签码，参与者不足");
        }

        // 3. 查询奖品（按奖品等级升序）
        LambdaQueryWrapper<Prize> prizeWrapper = new LambdaQueryWrapper<>();
        prizeWrapper.eq(Prize::getDrawId, drawId);
        prizeWrapper.orderByAsc(Prize::getPrizeType);
        List<Prize> prizes = prizeMapper.selectList(prizeWrapper);

        // 4. 随机开奖
        Collections.shuffle(codes);
        List<Winner> winners = new ArrayList<>();
        int codeIndex = 0;

        if (prizes.isEmpty()) {
            // 没有奖品：随机选1个人
            DrawCode selected = codes.get(0);
            selected.setPrizeId(0L);
            drawCodeMapper.updateById(selected);
            winners.add(buildWinner(selected, drawId, 0L));
        } else {
            // 有奖品：按奖品等级和数量依次分配
            for (Prize prize : prizes) {
                int amount = prize.getAmount();
                for (int i = 0; i < amount && codeIndex < codes.size(); i++) {
                    DrawCode selected = codes.get(codeIndex);
                    selected.setPrizeId(prize.getPrizeId());
                    drawCodeMapper.updateById(selected);
                    winners.add(buildWinner(selected, drawId, prize.getPrizeId()));
                    codeIndex++;
                }
            }
        }

        if (winners.isEmpty()) {
            throw new BusinessException(ErrorCode.BUSINESS_ERROR, "参与人数不足或奖品配置异常，开奖失败");
        }

        // 5. 批量写入中奖记录
        winnerMapper.batchInsert(winners);

        // 6. 更新抽签状态为已开奖
        draw.setStatus(DrawConstants.DRAW_STATUS_OPENED);
        draw.setDrawTime(LocalDateTime.now());
        draw.setUpdateTime(LocalDateTime.now());
        drawMapper.updateById(draw);
    }

    @Override
    public List<WinnerVO> getWinners(Long drawId) {
        return winnerMapper.selectWinnersByDrawId(drawId);
    }

    private Winner buildWinner(DrawCode selectedCode, Long drawId, Long prizeId) {
        User winnerUser = userReadMapper.selectById(selectedCode.getUserId());
        Winner winner = new Winner();
        winner.setUserId(selectedCode.getUserId());
        winner.setDrawId(drawId);
        winner.setPrizeId(prizeId);
        winner.setWinnerCodeId(selectedCode.getDrawCodeId());
        winner.setUserName(winnerUser != null ? winnerUser.getUserName() : "");
        winner.setAvatar(winnerUser != null ? winnerUser.getAvatar() : null);
        return winner;
    }




}