package com.quickstart.draw.module.notify.service;

import com.quickstart.common.domain.PageResult;
import com.quickstart.draw.module.notify.domain.NotifyMessage;
import com.quickstart.draw.module.notify.dto.NotifyPageDTO;
import com.quickstart.draw.module.notify.vo.NotifyMessageVO;

public interface NotifyMessageService {

    NotifyMessage createIfAbsent(NotifyMessage message);

    PageResult<NotifyMessageVO> page(Long userId, NotifyPageDTO dto);

    Long unreadCount(Long userId);

    void markRead(Long userId, Long notifyId);

    void markAllRead(Long userId);
}
