package com.quickstart.draw.util;

import com.quickstart.draw.module.drawCode.mapper.DrawCodeMapper;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

@Component
public class DrawCodeGenerator {

    // 安全字符池
    private static final String SAFE_CHARS =
            "123456789ABCDEFGHJKLMNPQRSTUVWXYZabcdefghjkmnpqrstuvwxyz";
    private static final int CODE_LENGTH = 8;
    private static final int CHAR_POOL_LEN = SAFE_CHARS.length();

    // 可重入锁：保证批量生成码时串行执行，线程安全
    private final ReentrantLock lock = new ReentrantLock();

    @Resource
    private DrawCodeMapper drawCodeMapper;

    // 饿汉单例Spring托管，本身就是单例
    public DrawCodeGenerator() {}

    /**
     * 批量生成指定数量 8位唯一抽奖码
     * 线程安全、全局不重复、库内不重复
     */
    public List<String> batchGenerate(int needCount) {
        if (needCount <= 0) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>(needCount);
        // 加锁：多请求并发时，排队生成，防止同一瞬间生成相同随机码
        lock.lock();
        try {
            while (result.size() < needCount) {
                // 1. 内存批量随机生成一批
                int remain = needCount - result.size();
                Set<String> tempSet = new HashSet<>(remain);
                for (int i = 0; i < remain; i++) {
                    tempSet.add(generateSingleCode());
                }
                List<String> candidateList = new ArrayList<>(tempSet);

                // 2. 批量查库，过滤已存在的码
                List<String> existCodes = drawCodeMapper.selectCodesByBatch(candidateList);
                Set<String> existSet = new HashSet<>(existCodes);

                // 3. 过滤出数据库不存在的合法码
                List<String> valid = candidateList.stream()
                        .filter(code -> !existSet.contains(code))
                        .collect(Collectors.toList());

                result.addAll(valid);
            }
            // 截断到需要的数量
            return result.stream().limit(needCount).collect(Collectors.toList());
        } finally {
            // 一定要释放锁
            lock.unlock();
        }
    }

    /**
     * 生成单个8位随机码 线程安全
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
