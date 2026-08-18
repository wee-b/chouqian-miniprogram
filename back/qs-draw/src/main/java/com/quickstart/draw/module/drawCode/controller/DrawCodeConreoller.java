package com.quickstart.draw.module.drawCode.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.EntryType;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.quickstart.common.annotation.RateLimit;
import com.quickstart.common.domain.ErrorCode;
import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.PageResult;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.drawCode.dto.DrawJoinRecordPageDTO;
import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.domain.drawCode.vo.DrawJoinRecordVO;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import com.quickstart.common.exception.BusinessException;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.draw.constant.SentinelResourceConstants;
import com.quickstart.draw.module.drawCode.service.DrawJoinService;
import com.quickstart.draw.module.drawCode.service.DrawOpenService;
import com.quickstart.draw.module.drawCode.service.DrawQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "参与码模块")
@RestController
public class DrawCodeConreoller {

    @Autowired
    private DrawJoinService drawJoinService;
    @Autowired
    private DrawOpenService drawOpenService;
    @Autowired
    private DrawQueryService drawQueryService;


    @RateLimit(key = "joinDraw", permits = 100)
    @SentinelResource(
            value = SentinelResourceConstants.DRAW_JOIN,
            entryType = EntryType.IN,
            blockHandler = "joinBlockHandler",
            fallback = "joinFallback",
            exceptionsToIgnore = BusinessException.class
    )
    @PostMapping("/client/drawCode/join")
    @Operation(summary = "参与抽签")
    public ResponseDTO<List<String>> join(
            @RequestParam("drawId") Long drawId,
            HttpServletRequest request
    ) {
        log.info("收到请求：/client/drawCode/join");
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        String ip = extractIp(request);
        List<String> res = drawJoinService.joinDraw(drawId, loginUser.getUserId(),ip);
        return ResponseDTO.ok(res);
    }

    public ResponseDTO<List<String>> joinBlockHandler(Long drawId, HttpServletRequest request, BlockException ex) {
        log.warn("Sentinel block draw join, drawId={}, reason={}", drawId, ex.getClass().getSimpleName());
        return ResponseDTO.error(ErrorCode.TOO_MANY_REQUESTS, "当前活动参与人数较多，请稍后再试");
    }

    public ResponseDTO<List<String>> joinFallback(Long drawId, HttpServletRequest request, Throwable ex) {
        log.error("Sentinel fallback draw join, drawId={}", drawId, ex);
        return ResponseDTO.error(ErrorCode.INTERNAL_SERVER_ERROR, "参与抽签失败，请稍后重试");
    }

    @SentinelResource(
            value = SentinelResourceConstants.DRAW_MY_CODES,
            entryType = EntryType.IN,
            blockHandler = "myCodesBlockHandler",
            fallback = "myCodesFallback",
            exceptionsToIgnore = BusinessException.class
    )
    @GetMapping("/client/drawCode/myCodes")
    @Operation(summary = "查询我的参与码")
    public ResponseDTO<List<DrawCodeVO>> myCodes(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/drawCode/myCodes,drawId={}", drawId);
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        List<DrawCodeVO> res = drawQueryService.getMyCodes(drawId, loginUser.getUserId());
        return ResponseDTO.ok(res);
    }

    public ResponseDTO<List<DrawCodeVO>> myCodesBlockHandler(Long drawId, BlockException ex) {
        log.warn("Sentinel block my draw codes, drawId={}, reason={}", drawId, ex.getClass().getSimpleName());
        return ResponseDTO.error(ErrorCode.TOO_MANY_REQUESTS, "查询频繁，请稍后再试");
    }

    public ResponseDTO<List<DrawCodeVO>> myCodesFallback(Long drawId, Throwable ex) {
        log.error("Sentinel fallback my draw codes, drawId={}", drawId, ex);
        return ResponseDTO.error(ErrorCode.INTERNAL_SERVER_ERROR, "参与码查询失败，请稍后重试");
    }

    @SentinelResource(
            value = SentinelResourceConstants.DRAW_OPEN,
            entryType = EntryType.IN,
            blockHandler = "openBlockHandler",
            fallback = "openFallback",
            exceptionsToIgnore = BusinessException.class
    )
    @PostMapping("/client/draw/open/{drawId}")
    @Operation(summary = "手动开奖")
    public ResponseDTO<Void> open(@PathVariable("drawId") Long drawId) {
        log.info("收到请求：/client/draw/open/{}", drawId);
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        drawOpenService.openDraw(drawId, loginUser.getUserId());
        return ResponseDTO.ok();
    }

    public ResponseDTO<Void> openBlockHandler(Long drawId, BlockException ex) {
        log.warn("Sentinel block draw open, drawId={}, reason={}", drawId, ex.getClass().getSimpleName());
        return ResponseDTO.error(ErrorCode.TOO_MANY_REQUESTS, "开奖请求频繁，请稍后再试");
    }

    public ResponseDTO<Void> openFallback(Long drawId, Throwable ex) {
        log.error("Sentinel fallback draw open, drawId={}", drawId, ex);
        return ResponseDTO.error(ErrorCode.INTERNAL_SERVER_ERROR, "开奖失败，请稍后重试");
    }

    @SentinelResource(
            value = SentinelResourceConstants.DRAW_WINNERS,
            entryType = EntryType.IN,
            blockHandler = "winnersBlockHandler",
            fallback = "winnersFallback",
            exceptionsToIgnore = BusinessException.class
    )
    @GetMapping("/client/draw/winners")
    @Operation(summary = "查询中奖名单")
    public ResponseDTO<List<WinnerVO>> winners(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/draw/winners?drawId={}", drawId);
        List<WinnerVO> winners = drawQueryService.getWinners(drawId);
        return ResponseDTO.ok(winners);
    }

    public ResponseDTO<List<WinnerVO>> winnersBlockHandler(Long drawId, BlockException ex) {
        log.warn("Sentinel block draw winners, drawId={}, reason={}", drawId, ex.getClass().getSimpleName());
        return ResponseDTO.error(ErrorCode.TOO_MANY_REQUESTS, "查询人数较多，请稍后刷新");
    }

    public ResponseDTO<List<WinnerVO>> winnersFallback(Long drawId, Throwable ex) {
        log.error("Sentinel fallback draw winners, drawId={}", drawId, ex);
        return ResponseDTO.error(ErrorCode.INTERNAL_SERVER_ERROR, "中奖名单查询失败，请稍后刷新");
    }

    @PostMapping("/client/drawCode/joinRecords")
    @Operation(summary = "分页查询抽签参与记录")
    public ResponseDTO<PageResult<DrawJoinRecordVO>> joinRecords(@RequestBody @Valid DrawJoinRecordPageDTO dto) {
        log.info("收到请求：/client/drawCode/joinRecords, drawId={}", dto.getDrawId());
        return ResponseDTO.ok(drawQueryService.queryJoinRecords(dto));
    }

    /** 从请求头提取真实客户端IP（穿透网关/代理） */
    private String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 多级代理取第一个
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }

}
