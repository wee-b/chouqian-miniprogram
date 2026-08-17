package com.quickstart.draw.module.notify.service;

import java.util.List;

public interface DrawNotifyService {

    void notifyDrawOpenedAsync(Long drawId);

    void notifyDrawJoinedAsync(Long drawId, Long userId, List<String> codeValues);

    void notifyDrawOpenedAfterCommit(Long drawId);

    void notifyDrawJoinedAfterCommit(Long drawId, Long userId, List<String> codeValues);
}
