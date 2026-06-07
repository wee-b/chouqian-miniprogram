package com.quickstart.draw.module.temporaryCode.service.impl;

import com.quickstart.common.domain.draw.Draw;
import com.quickstart.common.domain.temporaryCode.dto.GenerateCodeDTO;
import com.quickstart.common.domain.draw.vo.DrawSmallVO;
import com.quickstart.common.domain.temporaryCode.vo.PassCodeVO;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.draw.constant.RedisConstant;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import com.quickstart.draw.module.temporaryCode.service.TemporaryCode;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Random;
import java.util.concurrent.TimeUnit;

@Service
public class TemporaryCodeImpl implements TemporaryCode {

    @Resource
    private DrawMapper drawMapper;

    @Autowired
    private StringRedisTemplate redisTemplate;


    @Override
    public void generatePassCode(Long userId, GenerateCodeDTO dto) {

        Long drawId = dto.getDrawId();
        Draw draw = drawMapper.selectById(drawId);
        if(draw == null){
            throw new IllegalArgumentException("抽奖不存在");
        }
        if(!draw.getPublisherUserId().equals(userId)){
            throw new IllegalArgumentException("不可发布他人抽奖的口令");
        }

        String prefix = RedisConstant.PassCodePrefix;
        String passcode = "";
        boolean generateSuccess = false;
        for(int i = 0;i<3;i++){     // 尝试3次
            passcode = String.valueOf(generateSixDigitCode());
            String query = redisTemplate.opsForValue().get(prefix+passcode);

            if(query == null){
                generateSuccess = true;
                break;
            }
        }
        if(!generateSuccess){
            throw new BusinessException("请稍后再试");
        }

        Integer expireHours = dto.getExpireHours();
        redisTemplate.opsForValue().set(prefix+passcode, String.valueOf(drawId),expireHours, TimeUnit.HOURS);
        redisTemplate.opsForValue().set(prefix+drawId, passcode,expireHours,TimeUnit.HOURS);
    }

    @Override
    public void banPassCode(Long userId, String passCode) {
        String prefix = RedisConstant.PassCodePrefix;

        // 1. 根据口令查询对应的抽奖ID（你之前存的：prefix+passCode -> drawId）
        String passCodeKey = prefix + passCode;
        String drawIdStr = redisTemplate.opsForValue().get(passCodeKey);
        if (drawIdStr == null) {
            throw new BusinessException("口令不存在或已失效");
        }
        Long drawId = Long.parseLong(drawIdStr);

        // 2. 校验抽奖是否存在 & 只能禁用自己发布的抽奖
        Draw draw = drawMapper.selectById(drawId);
        if (draw == null) {
            throw new BusinessException("抽奖不存在");
        }
        if (!draw.getPublisherUserId().equals(userId)) {
            throw new BusinessException("不能禁用他人的抽奖口令");
        }

        // 3. 核心：删除Redis中的两个key，立即失效口令
        String drawIdKey = prefix + drawId;
        redisTemplate.delete(passCodeKey);  // 删除 口令->抽奖ID
        redisTemplate.delete(drawIdKey);    // 删除 抽奖ID->口令
    }

    @Override
    public PassCodeVO queryPassCode(Long userId, Long drawId) {

        Draw draw = drawMapper.selectById(drawId);
        if(draw == null){
            throw new IllegalArgumentException("抽奖不存在");
        }
        if(!draw.getPublisherUserId().equals(userId)){
            throw new IllegalArgumentException("不可查询他人抽奖的口令");
        }

        // 从redis中读取
        String prefix = RedisConstant.PassCodePrefix;
        String redisKey = prefix + drawId;
        String passcode = redisTemplate.opsForValue().get(redisKey);

        if(passcode == null){
            throw new BusinessException("口令不存在");
        }

        // ========== 核心：计算剩余时间和过期时间戳 ==========
        // 获取剩余过期时间（秒）
        Long remainValidSecond = redisTemplate.getExpire(redisKey);

        // 计算过期时间戳（当前时间 + 剩余秒数 = 毫秒级时间戳）
        long expireTime = System.currentTimeMillis() + remainValidSecond * 1000;

        // 封装VO
        PassCodeVO vo = new PassCodeVO();
        vo.setPassCode(passcode);
        vo.setExpireTime(expireTime);       // 过期时间戳（毫秒）
        vo.setRemainValidSecond(remainValidSecond); // 剩余秒数
        return vo;
    }

    @Override
    public DrawSmallVO queryDrawByPC(Long userId, String passCode) {

        String prefix = RedisConstant.PassCodePrefix;
        String drawString = redisTemplate.opsForValue().get(prefix + passCode);
        if(drawString == null){
            throw new BusinessException("口令已失效");
        }
        Long drawId = Long.valueOf(drawString);

        Draw draw = drawMapper.selectById(drawId);
        if(draw == null){
            throw new IllegalArgumentException("抽奖不存在");
        }
        DrawSmallVO vo = new DrawSmallVO();
        vo.setTitle(draw.getTitle());
        vo.setDrawId(draw.getDrawId());
        vo.setDrawCover(draw.getDrawCover());

        return vo;
    }


    private static int generateSixDigitCode() {
        Random random = new Random();
        // 生成 0~899999，再加 100000，保证6位
        return 100000 + random.nextInt(900000);
    }


}
