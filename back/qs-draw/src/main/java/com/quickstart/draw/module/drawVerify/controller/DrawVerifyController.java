package com.quickstart.draw.module.drawVerify.controller;

import com.quickstart.common.annotation.NoNeedLogin;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.draw.vo.DrawVerifyVO;
import com.quickstart.draw.module.drawVerify.service.DrawVerifyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * 可验证开奖接口。
 */
@Slf4j
@Tag(name = "可验证开奖模块")
@RestController
public class DrawVerifyController {

    private final DrawVerifyService drawVerifyService;

    public DrawVerifyController(DrawVerifyService drawVerifyService) {
        this.drawVerifyService = drawVerifyService;
    }

    @NoNeedLogin
    @GetMapping("/client/draw/verify")
    @Operation(summary = "查询可验证开奖信息")
    public ResponseDTO<DrawVerifyVO> verify(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/draw/verify, drawId={}", drawId);
        return ResponseDTO.ok(drawVerifyService.getVerifyInfo(drawId));
    }
}
