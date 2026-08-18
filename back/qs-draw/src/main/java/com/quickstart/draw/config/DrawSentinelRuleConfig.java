package com.quickstart.draw.config;

import com.alibaba.csp.sentinel.slots.block.RuleConstant;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRule;
import com.alibaba.csp.sentinel.slots.block.degrade.DegradeRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.FlowRuleManager;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRule;
import com.alibaba.csp.sentinel.slots.block.flow.param.ParamFlowRuleManager;
import com.alibaba.csp.sentinel.slots.system.SystemRule;
import com.alibaba.csp.sentinel.slots.system.SystemRuleManager;
import com.quickstart.draw.constant.SentinelResourceConstants;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class DrawSentinelRuleConfig {

    @PostConstruct
    public void loadRules() {
        FlowRuleManager.loadRules(flowRules());
        ParamFlowRuleManager.loadRules(paramFlowRules());
        DegradeRuleManager.loadRules(degradeRules());
        SystemRuleManager.loadRules(systemRules());
    }

    private List<FlowRule> flowRules() {
        List<FlowRule> rules = new ArrayList<>();
        rules.add(qpsRule(SentinelResourceConstants.DRAW_JOIN, 100));
        rules.add(threadRule(SentinelResourceConstants.DRAW_JOIN, 50));
        rules.add(qpsRule(SentinelResourceConstants.DRAW_OPEN, 20));
        rules.add(qpsRule(SentinelResourceConstants.DRAW_WINNERS, 200));
        rules.add(qpsRule(SentinelResourceConstants.DRAW_DETAIL, 300));
        rules.add(qpsRule(SentinelResourceConstants.DRAW_MY_CODES, 200));
        return rules;
    }

    private List<ParamFlowRule> paramFlowRules() {
        ParamFlowRule joinDrawIdRule = new ParamFlowRule(SentinelResourceConstants.DRAW_JOIN)
                .setParamIdx(0)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(30)
                .setDurationInSec(1);
        return Collections.singletonList(joinDrawIdRule);
    }

    private List<DegradeRule> degradeRules() {
        List<DegradeRule> rules = new ArrayList<>();
        rules.add(slowRequestRule(SentinelResourceConstants.DRAW_WINNERS, 500, 0.5, 10, 20));
        rules.add(slowRequestRule(SentinelResourceConstants.DRAW_DETAIL, 500, 0.5, 10, 20));
        rules.add(slowRequestRule(SentinelResourceConstants.DRAW_STATISTICS, 500, 0.5, 10, 20));
        return rules;
    }

    private List<SystemRule> systemRules() {
        SystemRule systemRule = new SystemRule();
        systemRule.setAvgRt(800);
        systemRule.setMaxThread(200);
        systemRule.setQps(800);
        systemRule.setHighestCpuUsage(0.85);
        return Collections.singletonList(systemRule);
    }

    private FlowRule qpsRule(String resource, double count) {
        return new FlowRule(resource)
                .setGrade(RuleConstant.FLOW_GRADE_QPS)
                .setCount(count);
    }

    private FlowRule threadRule(String resource, double count) {
        return new FlowRule(resource)
                .setGrade(RuleConstant.FLOW_GRADE_THREAD)
                .setCount(count);
    }

    private DegradeRule slowRequestRule(String resource, double maxRt, double ratio, int timeWindow, int minRequestAmount) {
        return new DegradeRule(resource)
                .setGrade(RuleConstant.DEGRADE_GRADE_RT)
                .setCount(maxRt)
                .setSlowRatioThreshold(ratio)
                .setTimeWindow(timeWindow)
                .setMinRequestAmount(minRequestAmount);
    }
}
