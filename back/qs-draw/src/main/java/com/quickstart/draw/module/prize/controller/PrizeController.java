package com.quickstart.draw.module.prize.controller;

import com.quickstart.common.annotation.NoNeedLogin;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.domain.prize.Prize;
import com.quickstart.common.domain.prize.dto.PrizeDTO;
import com.quickstart.draw.module.prize.service.PrizeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "奖品模块")
public class PrizeController {

    @Autowired
    private PrizeService prizeService;

    @NoNeedLogin
    @Operation(summary = "根据抽奖id获取奖品")
    @GetMapping("/client/prize/getPrizesByDrawId")
    public ResponseDTO<List<Prize>> getPrizesByDrawId(@RequestParam("drawId") Long drawId) {
        log.info("收到请求：/client/prize/getPrizesByDrawId,drawId={}", drawId);
        List<Prize> res = prizeService.getPrizesByDrawId(drawId);
        return ResponseDTO.ok(res);
    }


    @Operation(summary = "批量添加奖品")
    @PostMapping("/client/prize/batchAddPrize")
    public ResponseDTO<Void> batchAddPrize(@RequestBody List<PrizeDTO> prizes) {
        log.info("收到请求：/client/prize/batchAddPrize");
        prizeService.batchAddPrize(prizes);
        return ResponseDTO.ok();
    }

    @Operation(summary = "修改奖品")
    @PostMapping("/client/prize/updatePrize")
    public ResponseDTO<Void> updatePrize(@RequestBody PrizeDTO dto) {
        log.info("收到请求：/client/prize/updatePrize");
        prizeService.updatePrize(dto);
        return ResponseDTO.ok();
    }

    @Operation(summary = "删除奖品")
    @PostMapping("/client/prize/batchDeletePrize")
    public ResponseDTO<Void> batchDeletePrize(@RequestParam("ids") List<Long> prizeIds) {
        log.info("收到请求：/client/prize/batchDeletePrize,prizeIds={}", prizeIds);
        prizeService.batchDeletePrize(prizeIds);
        return ResponseDTO.ok();
    }



}
