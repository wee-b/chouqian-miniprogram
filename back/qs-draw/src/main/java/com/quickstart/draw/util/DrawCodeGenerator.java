package com.quickstart.draw.util;

import com.quickstart.draw.constant.RedisConstant;
import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import jakarta.annotation.Resource;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

@Component
public class DrawCodeGenerator {

    // 安全字符池
    private static final String SAFE_CHARS =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final int CODE_LENGTH = 8;
    private static final int CHAR_POOL_LEN = SAFE_CHARS.length();

    @Resource
    private DrawCodeMapper drawCodeMapper;

    @Resource
    private RedissonClient redissonClient;

    public DrawCodeGenerator() {}

    /**
     * 批量生成指定数量 8位唯一抽奖码
     * 分布式锁保证多实例下全局不重复
     */
    public List<String> batchGenerate(int needCount) {
        if (needCount <= 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(needCount);
        RLock lock = redissonClient.getLock(RedisConstant.CODE_GENERATOR_LOCK);
        lock.lock();
        try {
            while (result.size() < needCount) {
                int remain = needCount - result.size();
                Set<String> tempSet = new HashSet<>(remain);
                for (int i = 0; i < remain; i++) {
                    tempSet.add(generateSingleCode());
                }
                List<String> candidateList = new ArrayList<>(tempSet);

                List<String> existCodes = drawCodeMapper.selectCodesByBatch(candidateList);
                Set<String> existSet = new HashSet<>(existCodes);

                List<String> valid = candidateList.stream()
                        .filter(code -> !existSet.contains(code))
                        .collect(Collectors.toList());

                result.addAll(valid);
            }
            return result.stream().limit(needCount).collect(Collectors.toList());
        } finally {
            lock.unlock();
        }
    }

    /**
     * 生成单个8位随机码
     */
    private String generateSingleCode() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        StringBuilder sb = new StringBuilder(CODE_LENGTH);
        for (int i = 0; i < CODE_LENGTH; i++) {
            int idx = random.nextInt(CHAR_POOL_LEN);
            sb.append(SAFE_CHARS.charAt(idx));
        }
        return sb.toString();
    }
}
