package com.quickstart.draw.module.notify.controller;

import com.quickstart.common.domain.LoginUser;
import com.quickstart.common.domain.PageResult;
import com.quickstart.common.domain.ResponseDTO;
import com.quickstart.common.security.SecurityUserContext;
import com.quickstart.draw.module.notify.dto.NotifyPageDTO;
import com.quickstart.draw.module.notify.service.NotifyMessageService;
import com.quickstart.draw.module.notify.vo.NotifyMessageVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Notify message")
@RestController
@RequestMapping("/client/notify")
public class NotifyMessageController {

    @Resource
    private NotifyMessageService notifyMessageService;

    @PostMapping("/page")
    @Operation(summary = "Page notify messages")
    public ResponseDTO<PageResult<NotifyMessageVO>> page(@RequestBody @Valid NotifyPageDTO dto) {
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        return ResponseDTO.ok(notifyMessageService.page(loginUser.getUserId(), dto));
    }

    @GetMapping("/unreadCount")
    @Operation(summary = "Count unread notify messages")
    public ResponseDTO<Long> unreadCount() {
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        return ResponseDTO.ok(notifyMessageService.unreadCount(loginUser.getUserId()));
    }

    @PutMapping("/read/{notifyId}")
    @Operation(summary = "Mark one notify message read")
    public ResponseDTO<Void> markRead(@PathVariable("notifyId") Long notifyId) {
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        notifyMessageService.markRead(loginUser.getUserId(), notifyId);
        return ResponseDTO.ok();
    }

    @PutMapping("/readAll")
    @Operation(summary = "Mark all notify messages read")
    public ResponseDTO<Void> markAllRead() {
        LoginUser loginUser = SecurityUserContext.getCurrentLoginUser();
        notifyMessageService.markAllRead(loginUser.getUserId());
        return ResponseDTO.ok();
    }
}
