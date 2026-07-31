package com.quickstart.ops.dubbo;

import com.alibaba.fastjson.JSON;
import com.quickstart.api.dto.OpLogDTO;
import com.quickstart.api.dubbo.OpLogDubboService;
import org.apache.dubbo.config.annotation.DubboService;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.concurrent.CompletableFuture;

/**
 * 操作日志 Dubbo Provider 实现。
 *
 * 【Dubbo 价值体现点】
 * 操作日志是超高频内部调用——每个接口都打日志。
 * 走 Dubbo 长连接 + 二进制序列化，开销比 HTTP 短连接低一个数量级；
 * 且用 CompletableFuture 原生异步，Provider 端异步执行，Consumer 端立即返回不阻塞主流程。
 *
 * 落库策略：先异步写 Redis List 缓冲，由后台任务（XXL-JOB）批量刷库，避免高频写打挂 DB。
 */
@DubboService
public class OpLogDubboServiceImpl implements OpLogDubboService {

    private final StringRedisTemplate redisTemplate;

    public OpLogDubboServiceImpl(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public CompletableFuture<Void> recordAsync(OpLogDTO ctx) {
        // Dubbo 3 原生异步：返回 CompletableFuture，Provider 端异步执行
        return CompletableFuture.runAsync(() -> {
            if (ctx.getGmtCreate() == null) {
                ctx.setGmtCreate(LocalDateTime.now());
            }
            // 异步写 Redis List 缓冲，后续由 XXL-JOB 批量刷库
            redisTemplate.opsForList().leftPush("ops:oplog", JSON.toJSONString(ctx));
        });
    }
}
