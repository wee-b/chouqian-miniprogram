package com.quickstart.draw.job;

import com.quickstart.draw.module.drawCode.service.DrawOpenService;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
public class DrawOpenTriggerJob {

    private static final String AUTO_OPEN_LOCK_KEY = "qs:draw:auto-open:lock:";

    private final DrawOpenService drawOpenService;
    private final RedissonClient redissonClient;
    private final XxlJobAdminClient xxlJobAdminClient;

    public DrawOpenTriggerJob(DrawOpenService drawOpenService,
                              RedissonClient redissonClient,
                              XxlJobAdminClient xxlJobAdminClient) {
        this.drawOpenService = drawOpenService;
        this.redissonClient = redissonClient;
        this.xxlJobAdminClient = xxlJobAdminClient;
    }

    @XxlJob("triggerOpenDrawHandler")
    public void triggerOpenDrawHandler() {
        String jobParam = XxlJobHelper.getJobParam();

        Long drawId;
        try {
            drawId = Long.valueOf(jobParam);
        } catch (Exception e) {
            XxlJobHelper.handleFail("开奖任务参数错误，必须传 drawId，当前参数：" + jobParam);
            return;
        }

        String lockKey = AUTO_OPEN_LOCK_KEY + drawId;
        RLock lock = redissonClient.getLock(lockKey);
        boolean locked = false;

        try {
            locked = lock.tryLock(0, 60, TimeUnit.SECONDS);
            if (!locked) {
                XxlJobHelper.handleSuccess("抽签正在被其他实例处理，drawId=" + drawId);
                return;
            }

            drawOpenService.openDrawBySystem(drawId);
            stopCurrentJobQuietly();

            XxlJobHelper.handleSuccess("自动开奖成功，drawId=" + drawId);
        } catch (Exception e) {
            log.error("触发式自动开奖失败, drawId={}", drawId, e);
            XxlJobHelper.handleFail("自动开奖失败，drawId=" + drawId + "，原因：" + e.getMessage());
        } finally {
            if (locked && lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private void stopCurrentJobQuietly() {
        try {
            xxlJobAdminClient.stopJob(XxlJobHelper.getJobId());
        } catch (Exception e) {
            log.warn("停止当前 XXL-JOB 开奖任务失败, jobId={}", XxlJobHelper.getJobId(), e);
        }
    }
}
