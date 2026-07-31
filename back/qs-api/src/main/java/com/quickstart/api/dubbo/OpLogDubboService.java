package com.quickstart.api.dubbo;

import com.quickstart.api.dto.OpLogDTO;

import java.util.concurrent.CompletableFuture;

/**
 * 操作日志 Dubbo 服务。
 * 由 qs-ops 实现。
 *
 * 【Dubbo 价值体现点】
 * 操作日志是"超高频内部调用"——每个接口都要打日志。
 * 若走 Feign HTTP，每次短连接 + JSON 序列化开销在每秒数万次调用下累积成瓶颈；
 * Dubbo 长连接复用 + 二进制序列化把开销降一个数量级，
 * 且用 CompletableFuture 原生异步不阻塞主流程，比 HTTP 异步更优雅。
 */
public interface OpLogDubboService {

    /**
     * 异步记录操作日志（不阻塞主流程）。
     * Dubbo 3 原生支持 CompletableFuture 返回，Provider 端异步执行，Consumer 端立即返回。
     *
     * @param ctx 操作日志上下文
     * @return CompletableFuture，主流程无需等待
     */
    CompletableFuture<Void> recordAsync(OpLogDTO ctx);
}
