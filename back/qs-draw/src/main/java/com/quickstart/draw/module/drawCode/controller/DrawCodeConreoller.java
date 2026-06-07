package com.quickstart.draw.module.drawCode.controller;

import com.quickstart.common.annotation.RateLimit;
import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.drawCode.vo.DrawCodeVO;
import com.quickstart.common.domain.winner.vo.WinnerVO;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.draw.module.drawCode.service.DrawCodeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@Tag(name = "参与码模块")
@RestController
public class DrawCodeConreoller {

    @Autowired
    private DrawCodeService drawCodeService;


    @RateLimit(key = "joinDraw", permits = 100)
    @PostMapping("/client/drawCode/join")
    @Operation(summary = "参与抽签")
    public ResponseDTO<List<String>> join(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/drawCode/join");
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        List<String> res = drawCodeService.joinDraw(drawId, loginUser.getUserId());
        return ResponseDTO.ok(res);
    }

    @GetMapping("/client/drawCode/myCodes")
    @Operation(summary = "查询我的参与码")
    public ResponseDTO<List<DrawCodeVO>> myCodes(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/drawCode/myCodes,drawId={}", drawId);
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        List<DrawCodeVO> res = drawCodeService.getMyCodes(drawId, loginUser.getUserId());
        return ResponseDTO.ok(res);
    }

    @PostMapping("/client/draw/open/{drawId}")
    @Operation(summary = "手动开奖")
    public ResponseDTO<Void> open(@PathVariable Long drawId) {
        log.info("收到请求：/client/draw/open/{}", drawId);
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        drawCodeService.openDraw(drawId, loginUser.getUserId());
        return ResponseDTO.ok();
    }

    @GetMapping("/client/draw/winners")
    @Operation(summary = "查询中奖名单")
    public ResponseDTO<List<WinnerVO>> winners(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/draw/winners?drawId={}", drawId);
        List<WinnerVO> winners = drawCodeService.getWinners(drawId);
        return ResponseDTO.ok(winners);
    }

}
