package com.quickstart.draw.job;

import com.quickstart.common.domain.draw.Draw;
import com.quickstart.draw.constant.DrawConstants;
import com.quickstart.draw.module.draw.mapper.DrawMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

@Slf4j
@Service
@ConditionalOnProperty(prefix = "xxl.job", name = "enabled", havingValue = "true")
public class DrawOpenScheduleService {

    private final XxlJobAdminClient xxlJobAdminClient;
    private final DrawMapper drawMapper;

    public DrawOpenScheduleService(XxlJobAdminClient xxlJobAdminClient,
                                   DrawMapper drawMapper) {
        this.xxlJobAdminClient = xxlJobAdminClient;
        this.drawMapper = drawMapper;
    }

    public void scheduleAfterCommit(Draw draw) {
        if (draw == null || draw.getDrawId() == null) {
            return;
        }
        if (draw.getStatus() == null || draw.getStatus() != DrawConstants.DRAW_STATUS_RUNNING) {
            return;
        }
        if (draw.getDrawingWay() == null || draw.getDrawingWay() != DrawConstants.DRAWING_WAY_TIME) {
            return;
        }
        if (draw.getJoinDeadline() == null) {
            return;
        }
        if (draw.getXxlJobId() != null) {
            log.info("抽签已存在 XXL-JOB 自动开奖任务，跳过重复注册, drawId={}, jobId={}",
                    draw.getDrawId(), draw.getXxlJobId());
            return;
        }

        Runnable registerTask = () -> {
            try {
                Draw latest = drawMapper.selectById(draw.getDrawId());
                if (latest == null || latest.getXxlJobId() != null) {
                    return;
                }

                Integer jobId = xxlJobAdminClient.addAndStartOpenDrawJob(
                        latest.getDrawId(), latest.getJoinDeadline());
                latest.setXxlJobId(jobId);
                drawMapper.updateById(latest);
            } catch (Exception e) {
                log.error("注册 XXL-JOB 自动开奖任务失败, drawId={}", draw.getDrawId(), e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    registerTask.run();
                }
            });
        } else {
            registerTask.run();
        }
    }

    public void removeAfterCommit(Draw draw) {
        if (draw == null || draw.getDrawId() == null || draw.getXxlJobId() == null) {
            return;
        }

        Integer jobId = draw.getXxlJobId();
        Long drawId = draw.getDrawId();
        Runnable removeTask = () -> {
            try {
                xxlJobAdminClient.removeJob(jobId);

                Draw latest = drawMapper.selectById(drawId);
                if (latest != null && jobId.equals(latest.getXxlJobId())) {
                    latest.setXxlJobId(null);
                    drawMapper.updateById(latest);
                }
            } catch (Exception e) {
                log.error("删除 XXL-JOB 自动开奖任务失败, drawId={}, jobId={}", drawId, jobId, e);
            }
        };

        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    removeTask.run();
                }
            });
        } else {
            removeTask.run();
        }
    }
}
