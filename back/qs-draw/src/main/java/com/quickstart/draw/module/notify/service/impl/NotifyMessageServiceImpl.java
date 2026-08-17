package com.quickstart.draw.module.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.quickstart.common.domain.PageResult;
import com.quickstart.draw.module.notify.constant.NotifyConstants;
import com.quickstart.draw.module.notify.domain.NotifyMessage;
import com.quickstart.draw.module.notify.dto.NotifyPageDTO;
import com.quickstart.draw.module.notify.mapper.NotifyMessageMapper;
import com.quickstart.draw.module.notify.service.NotifyMessageService;
import com.quickstart.draw.module.notify.vo.NotifyMessageVO;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class NotifyMessageServiceImpl implements NotifyMessageService {

    @Resource
    private NotifyMessageMapper notifyMessageMapper;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public NotifyMessage createIfAbsent(NotifyMessage message) {
        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotifyMessage::getUserId, message.getUserId());
        wrapper.eq(NotifyMessage::getBizType, message.getBizType());
        wrapper.eq(NotifyMessage::getBizId, message.getBizId());
        wrapper.eq(NotifyMessage::getDeletedFlag, NotifyConstants.DELETED_NO);
        List<NotifyMessage> exists = notifyMessageMapper.selectList(wrapper);
        if (!exists.isEmpty()) {
            return exists.get(0);
        }

        LocalDateTime now = LocalDateTime.now();
        message.setReadFlag(NotifyConstants.READ_FLAG_UNREAD);
        message.setPushStatus(NotifyConstants.PUSH_STATUS_PENDING);
        message.setDeletedFlag(NotifyConstants.DELETED_NO);
        message.setCreateTime(now);
        message.setUpdateTime(now);
        notifyMessageMapper.insert(message);
        return message;
    }

    @Override
    public PageResult<NotifyMessageVO> page(Long userId, NotifyPageDTO dto) {
        Page<NotifyMessage> page = new Page<>(dto.getPage(), dto.getPageSize());
        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotifyMessage::getUserId, userId);
        wrapper.eq(NotifyMessage::getDeletedFlag, NotifyConstants.DELETED_NO);
        if (dto.getReadFlag() != null) {
            wrapper.eq(NotifyMessage::getReadFlag, dto.getReadFlag());
        }
        if (StringUtils.hasText(dto.getBizType())) {
            wrapper.eq(NotifyMessage::getBizType, dto.getBizType());
        }
        wrapper.orderByDesc(NotifyMessage::getCreateTime);
        wrapper.orderByDesc(NotifyMessage::getNotifyId);

        Page<NotifyMessage> result = notifyMessageMapper.selectPage(page, wrapper);
        PageResult<NotifyMessageVO> pageResult = new PageResult<>();
        pageResult.setCurPage(dto.getPage());
        pageResult.setTotal(result.getTotal());
        pageResult.setData(result.getRecords().stream().map(this::toVO).toList());
        return pageResult;
    }

    @Override
    public Long unreadCount(Long userId) {
        LambdaQueryWrapper<NotifyMessage> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(NotifyMessage::getUserId, userId);
        wrapper.eq(NotifyMessage::getReadFlag, NotifyConstants.READ_FLAG_UNREAD);
        wrapper.eq(NotifyMessage::getDeletedFlag, NotifyConstants.DELETED_NO);
        return notifyMessageMapper.selectCount(wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markRead(Long userId, Long notifyId) {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotifyMessage::getNotifyId, notifyId);
        wrapper.eq(NotifyMessage::getUserId, userId);
        wrapper.eq(NotifyMessage::getDeletedFlag, NotifyConstants.DELETED_NO);
        wrapper.set(NotifyMessage::getReadFlag, NotifyConstants.READ_FLAG_READ);
        wrapper.set(NotifyMessage::getUpdateTime, LocalDateTime.now());
        notifyMessageMapper.update(null, wrapper);
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public void markAllRead(Long userId) {
        LambdaUpdateWrapper<NotifyMessage> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(NotifyMessage::getUserId, userId);
        wrapper.eq(NotifyMessage::getReadFlag, NotifyConstants.READ_FLAG_UNREAD);
        wrapper.eq(NotifyMessage::getDeletedFlag, NotifyConstants.DELETED_NO);
        wrapper.set(NotifyMessage::getReadFlag, NotifyConstants.READ_FLAG_READ);
        wrapper.set(NotifyMessage::getUpdateTime, LocalDateTime.now());
        notifyMessageMapper.update(null, wrapper);
    }

    private NotifyMessageVO toVO(NotifyMessage message) {
        NotifyMessageVO vo = new NotifyMessageVO();
        vo.setNotifyId(message.getNotifyId());
        vo.setBizType(message.getBizType());
        vo.setBizId(message.getBizId());
        vo.setTitle(message.getTitle());
        vo.setContent(message.getContent());
        vo.setPayload(message.getPayload());
        vo.setReadFlag(message.getReadFlag());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
