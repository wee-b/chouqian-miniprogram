package com.quickstart.draw.module.notify.realtime;

import com.quickstart.draw.module.notify.domain.NotifyMessage;

public interface NotifyRealtimePushService {

    boolean pushToUser(NotifyMessage message);
}
