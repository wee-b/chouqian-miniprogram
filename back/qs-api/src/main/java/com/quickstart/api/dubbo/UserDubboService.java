package com.quickstart.api.dubbo;

import com.quickstart.api.dto.UserDTO;

/**
 * 用户查询 Dubbo 服务。
 * 由 qs-client 实现，供 qs-draw / qs-ops 在参与抽奖、开奖通知等链路查询用户信息。
 *
 * 典型调用方：qs-draw 参与抽奖前校验用户状态、qs-ops WebSocket 推送前查用户连接归属。
 */
public interface UserDubboService {

    /**
     * 按 userCode 查询用户信息。
     *
     * @param userCode 用户编码
     * @return 用户DTO，不存在返回 null
     */
    UserDTO getUserByCode(String userCode);

    /**
     * 按用户ID查询用户信息。
     *
     * @param userId 用户ID
     * @return 用户DTO，不存在返回 null
     */
    UserDTO getUserById(Long userId);

    /**
     * 校验用户状态是否正常（status=1）。
     * 用于参与抽奖热路径上的快速校验。
     *
     * @param userId 用户ID
     * @return true 正常可参与
     */
    boolean checkUserStatus(Long userId);
}
