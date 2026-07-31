package com.quickstart.draw.dubbo;

import com.quickstart.api.dubbo.IdDubboService;
import com.quickstart.api.dubbo.OpLogDubboService;
import com.quickstart.api.dubbo.RiskRuleDubboService;
import com.quickstart.api.dubbo.StatDubboService;
import com.quickstart.api.dubbo.UserDubboService;
import org.apache.dubbo.config.annotation.DubboReference;
import org.springframework.stereotype.Component;

/**
 * Dubbo Consumer 持有者：集中管理对 qs-client / qs-ops 的 Dubbo 服务引用。
 *
 * 【架构价值】
 * 参与抽奖热路径上，本服务通过 Dubbo 长连接同步调用：
 * - user() 校验用户状态
 * - riskRule() 实时风控校验
 * - id() 生成参与记录ID
 * - stat() 自增参与人数
 * 每个接口再异步调 opLog() 落日志。
 *
 * 这组高频内部调用正是 Dubbo 价值的体现：
 * TCP 长连接复用 + 二进制序列化，开销比 HTTP 短连接低一个数量级。
 *
 * check=false：启动时不检查 Provider 是否就绪，避免启动顺序耦合。
 */
@Component
public class DrawDubboConsumer {

    @DubboReference(check = false)
    private UserDubboService userDubboService;

    @DubboReference(check = false)
    private OpLogDubboService opLogDubboService;

    @DubboReference(check = false)
    private RiskRuleDubboService riskRuleDubboService;

    @DubboReference(check = false)
    private IdDubboService idDubboService;

    @DubboReference(check = false)
    private StatDubboService statDubboService;

    public UserDubboService user() {
        return userDubboService;
    }

    public OpLogDubboService opLog() {
        return opLogDubboService;
    }

    public RiskRuleDubboService riskRule() {
        return riskRuleDubboService;
    }

    public IdDubboService id() {
        return idDubboService;
    }

    public StatDubboService stat() {
        return statDubboService;
    }
}
