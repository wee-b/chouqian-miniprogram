package com.quickstart.draw.module.notify.service;

public interface DrawNotifyService {

    /**
     * 开奖成功后异步通知。
     */
    void notifyDrawOpenedAsync(Long drawId);

}
