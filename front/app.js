import auth from './utils/auth.js';
import notifyApi from './utils/apis/notify.js';
import { wsBaseUrl } from './utils/config.js';

App({
  globalData: {
    editDrawId: null,
    unreadNotifyCount: 0,
    notifySocketOpen: false,
    notifySocketConnecting: false,
    notifySocketTask: null,
    notifyHeartbeatTimer: null,
    notifyToastTimer: null
  },

  onShow() {
    if (auth.isLogin()) {
      this.refreshUnreadCount();
      this.connectNotifySocket();
    }
  },

  onHide() {
    this.closeNotifySocket();
  },

  async refreshUnreadCount() {
    if (!auth.isLogin()) {
      this.setUnreadCount(0);
      return;
    }
    try {
      const res = await notifyApi.unreadCount();
      if (res.code === 0) {
        this.setUnreadCount(res.data || 0);
      }
    } catch (e) {
      // Unread count is not the core path.
    }
  },

  setUnreadCount(count) {
    const nextCount = Math.max(parseInt(count || 0), 0);
    this.globalData.unreadNotifyCount = nextCount;
    try {
      if (nextCount > 0) {
        wx.setTabBarBadge({
          index: 2,
          text: nextCount > 99 ? '99+' : String(nextCount)
        });
      } else {
        wx.removeTabBarBadge({ index: 2 });
      }
    } catch (e) {
      // setTabBarBadge can fail outside tab pages.
    }
  },

  increaseUnreadCount(delta = 1) {
    this.setUnreadCount((this.globalData.unreadNotifyCount || 0) + delta);
  },

  connectNotifySocket() {
    if (this.globalData.notifySocketOpen || this.globalData.notifySocketConnecting) {
      return;
    }
    const userInfo = auth.getUserInfo();
    const memberCode = userInfo.memberCode;
    if (!memberCode) {
      console.warn('notify socket skipped: memberCode missing');
      return;
    }

    this.globalData.notifySocketConnecting = true;
    const socketTask = wx.connectSocket({
      url: `${wsBaseUrl}/ws/notify?userCode=${encodeURIComponent(memberCode)}`
    });
    console.log('notify socket connecting:', `${wsBaseUrl}/ws/notify`);
    this.globalData.notifySocketTask = socketTask;

    socketTask.onOpen(() => {
      this.globalData.notifySocketOpen = true;
      this.globalData.notifySocketConnecting = false;
      console.log('notify socket opened');
      this.startNotifyHeartbeat();
    });

    socketTask.onMessage((res) => {
      this.handleNotifySocketMessage(res.data);
    });

    socketTask.onClose(() => {
      this.globalData.notifySocketOpen = false;
      this.globalData.notifySocketConnecting = false;
      this.globalData.notifySocketTask = null;
      this.stopNotifyHeartbeat();
      console.log('notify socket closed');
    });

    socketTask.onError((err) => {
      this.globalData.notifySocketOpen = false;
      this.globalData.notifySocketConnecting = false;
      this.globalData.notifySocketTask = null;
      this.stopNotifyHeartbeat();
      console.error('notify socket error:', err);
    });
  },

  startNotifyHeartbeat() {
    this.stopNotifyHeartbeat();
    const interval = 30000 + Math.floor(Math.random() * 5000);
    this.globalData.notifyHeartbeatTimer = setInterval(() => {
      const socketTask = this.globalData.notifySocketTask;
      if (!socketTask || !this.globalData.notifySocketOpen) return;
      socketTask.send({ data: 'ping' });
    }, interval);
  },

  stopNotifyHeartbeat() {
    if (this.globalData.notifyHeartbeatTimer) {
      clearInterval(this.globalData.notifyHeartbeatTimer);
      this.globalData.notifyHeartbeatTimer = null;
    }
  },

  closeNotifySocket() {
    const socketTask = this.globalData.notifySocketTask;
    if (socketTask) {
      socketTask.close({ code: 1000, reason: 'app hide' });
    }
    this.globalData.notifySocketOpen = false;
    this.globalData.notifySocketConnecting = false;
    this.globalData.notifySocketTask = null;
    this.stopNotifyHeartbeat();
  },

  handleNotifySocketMessage(raw) {
    if (raw === 'pong') return;

    let message = null;
    try {
      message = typeof raw === 'string' ? JSON.parse(raw) : raw;
    } catch (e) {
      return;
    }
    if (!message || message.event !== 'NOTIFY_MESSAGE') return;

    this.increaseUnreadCount(1);
    this.showGlobalNotifyToast(message);

    this.refreshCurrentDrawPage(message);
  },

  showGlobalNotifyToast(message) {
    const pages = getCurrentPages();
    const current = pages[pages.length - 1];
    if (!current || typeof current.setData !== 'function') {
      wx.showToast({ title: message.title || '收到新通知', icon: 'none' });
      return;
    }

    if (this.globalData.notifyToastTimer) {
      clearTimeout(this.globalData.notifyToastTimer);
    }

    current.setData({
      globalNotifyVisible: true,
      globalNotifyTop: this.getNotifyToastTop(),
      globalNotifyId: message.notifyId,
      globalNotifyPayload: message.payload,
      globalNotifyTargetDrawId: this.getNotifyTargetDrawId(message.payload),
      globalNotifyBizType: message.bizType || '',
      globalNotifyTitle: message.title || '收到新通知',
      globalNotifyContent: message.content || '',
      globalNotifyTypeText: this.getNotifyTypeText(message.bizType)
    });

    this.globalData.notifyToastTimer = setTimeout(() => {
      const latestPages = getCurrentPages();
      const latest = latestPages[latestPages.length - 1];
      if (latest && typeof latest.setData === 'function') {
        latest.setData({ globalNotifyVisible: false });
      }
      this.globalData.notifyToastTimer = null;
    }, 3500);
  },

  getNotifyTypeText(type) {
    const map = {
      DRAW_CODE_GENERATED: '抽签码',
      DRAW_CODE_FAILED: '失败',
      DRAW_OPENED: '开奖',
      WINNER_NOTICE: '中奖'
    };
    return map[type] || '通知';
  },

  getNotifyToastTop() {
    try {
      const info = wx.getWindowInfo ? wx.getWindowInfo() : wx.getSystemInfoSync();
      return (info.statusBarHeight || 0) + 8;
    } catch (e) {
      return 24;
    }
  },

  handleGlobalNotifyTap(page) {
    const source = page && page.data ? page.data : {};
    const drawId = source.globalNotifyTargetDrawId || this.getNotifyTargetDrawId(source.globalNotifyPayload);
    const notifyId = source.globalNotifyId;

    if (page && typeof page.setData === 'function') {
      page.setData({ globalNotifyVisible: false });
    }
    if (this.globalData.notifyToastTimer) {
      clearTimeout(this.globalData.notifyToastTimer);
      this.globalData.notifyToastTimer = null;
    }

    if (notifyId) {
      notifyApi.markRead(notifyId).then((res) => {
        if (res.code === 0) {
          this.setUnreadCount((this.globalData.unreadNotifyCount || 1) - 1);
        }
      }).catch(() => {
        // Mark read can be retried from message center.
      });
    }

    if (!drawId) return;

    const pages = getCurrentPages();
    const current = pages[pages.length - 1];
    if (current && current.route === 'pages/drawDetail/drawDetail' && String(current.data.drawId) === String(drawId)) {
      if (typeof current.loadAllData === 'function') {
        current.loadAllData({ silent: true });
      }
      return;
    }

    wx.navigateTo({ url: '/pages/drawDetail/drawDetail?drawId=' + drawId });
  },

  getNotifyTargetDrawId(payload) {
    const parsed = this.parsePayload(payload);
    return parsed && parsed.drawId ? parsed.drawId : null;
  },

  refreshCurrentDrawPage(message) {
    const payload = this.parsePayload(message.payload);
    if (!payload || !payload.drawId) return;

    const pages = getCurrentPages();
    const current = pages[pages.length - 1];
    if (!current || current.route !== 'pages/drawDetail/drawDetail') return;
    if (String(current.data.drawId) !== String(payload.drawId)) return;

    if (typeof current.loadAllData === 'function') {
      current.loadAllData({ silent: true });
    }
  },

  parsePayload(payload) {
    if (!payload) return null;
    if (typeof payload === 'object') return payload;
    try {
      return JSON.parse(payload);
    } catch (e) {
      return null;
    }
  }
});
