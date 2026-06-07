package com.quickstart.draw.module.temporaryCode.controller;

import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.temporaryCode.dto.GenerateCodeDTO;
import com.quickstart.common.domain.draw.vo.DrawSmallVO;
import com.quickstart.common.domain.temporaryCode.vo.PassCodeVO;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.draw.module.temporaryCode.service.TemporaryCode;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@Tag(name = "抽签口令")
@RestController
public class TemporaryCodeController {


    @Autowired
    private TemporaryCode temporaryCode;


    @PostMapping("/client/draw/generatePassCode")
    @Operation(summary = "生成口令")
    public ResponseDTO<Void> generatePassCode(@RequestBody @Valid GenerateCodeDTO dto){
        log.info("收到请求：/client/draw/generatePassCode");
        Long userId = SecurityUserContext.getCurrentLoginUser().getUserId();
        temporaryCode.generatePassCode(userId,dto);
        return ResponseDTO.ok();
    }

    @PostMapping("/client/draw/banPassCode")
    @Operation(summary = "禁用口令")
    public ResponseDTO<Void> banPassCode(@RequestParam("passCode") String passCode){
        log.info("收到请求：/client/draw/banPassCode");
        Long userId = SecurityUserContext.getCurrentLoginUser().getUserId();
        temporaryCode.banPassCode(userId,passCode);
        return ResponseDTO.ok();
    }

    @GetMapping("/client/draw/queryPassCode")
    @Operation(summary = "查询口令")
    public ResponseDTO<PassCodeVO> queryPassCode(@RequestParam("drawId") Long drawId){
        log.info("收到请求：/client/draw/queryPassCode,drawId:{}",drawId);
        Long userId = SecurityUserContext.getCurrentLoginUser().getUserId();
        PassCodeVO vo = temporaryCode.queryPassCode(userId,drawId);
        return ResponseDTO.ok(vo);
    }


    @GetMapping("/client/draw/queryDrawByPC")
    @Operation(summary = "根据口令查询抽奖")
    public ResponseDTO<DrawSmallVO> queryDrawByPC(@RequestParam("passCode") String passCode){
        log.info("收到请求：/client/draw/queryDrawByPC,passCode:{}",passCode);
        Long userId = SecurityUserContext.getCurrentLoginUser().getUserId();
        DrawSmallVO vo = temporaryCode.queryDrawByPC(userId,passCode);
        return ResponseDTO.ok(vo);
    }
}
