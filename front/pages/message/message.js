import notifyApi from '../../utils/apis/notify.js';
import auth from '../../utils/auth.js';

const PAGE_SIZE = 10;
const TYPE_TEXT = {
  DRAW_CODE_GENERATED: '抽签码',
  DRAW_CODE_FAILED: '失败',
  DRAW_OPENED: '开奖',
  WINNER_NOTICE: '中奖'
};

Page({
  data: {
    list: [],
    page: 1,
    loading: false,
    refreshing: false,
    hasMore: true,
    empty: false,
    pageTop: 96
  },

  onReady() {
    this.setSafeTop();
  },

  onLoad() {
    if (!auth.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    this.refresh();
  },

  onShow() {
    const app = getApp();
    if (app && app.refreshUnreadCount) {
      app.refreshUnreadCount();
    }
  },

  async onPullDownRefresh() {
    await this.refresh();
    wx.stopPullDownRefresh();
  },

  onReachBottom() {
    this.loadMore();
  },

  async refresh() {
    this.setData({
      list: [],
      page: 1,
      hasMore: true,
      empty: false,
      refreshing: true
    });
    await this.loadData(true);
    this.setData({ refreshing: false });
  },

  setSafeTop() {
    try {
      const windowInfo = wx.getWindowInfo();
      const menuButtonInfo = wx.getMenuButtonBoundingClientRect();
      const navBottom = menuButtonInfo.bottom || (windowInfo.statusBarHeight + 44);
      this.setData({ pageTop: navBottom + 16 });
    } catch (e) {
      this.setData({ pageTop: 96 });
    }
  },

  async loadMore() {
    await this.loadData(false);
  },

  goBack() {
    const pages = getCurrentPages();
    if (pages.length > 1) {
      wx.navigateBack();
    } else {
      wx.switchTab({ url: '/pages/mine/mine' });
    }
  },

  async loadData(reset) {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });

    try {
      const res = await notifyApi.page({
        page: this.data.page,
        pageSize: PAGE_SIZE
      });
      if (res.code === 0) {
        const result = res.data || {};
        const incoming = (result.data || []).map(item => this.normalizeMessage(item));
        const merged = reset ? incoming : this.data.list.concat(incoming);
        const total = result.total != null ? result.total : (result.Total || 0);

        this.setData({
          list: merged,
          page: this.data.page + 1,
          empty: merged.length === 0,
          hasMore: total ? merged.length < total : incoming.length >= PAGE_SIZE
        });
      } else {
        wx.showToast({ title: res.msg || '加载失败', icon: 'none' });
      }
    } catch (e) {
      wx.showToast({ title: '网络异常', icon: 'none' });
    }

    this.setData({ loading: false });
  },

  normalizeMessage(item) {
    const payload = this.parsePayload(item.payload);
    return {
      ...item,
      payloadObj: payload,
      typeText: TYPE_TEXT[item.bizType] || '通知',
      timeText: this.formatTime(item.createTime),
      unread: item.readFlag === 0
    };
  },

  parsePayload(payload) {
    if (!payload) return {};
    if (typeof payload === 'object') return payload;
    try {
      return JSON.parse(payload);
    } catch (e) {
      return {};
    }
  },

  formatTime(value) {
    if (!value) return '';
    if (Array.isArray(value)) {
      const p = n => (n < 10 ? '0' + n : '' + n);
      return `${value[0]}-${p(value[1])}-${p(value[2])} ${p(value[3])}:${p(value[4])}`;
    }
    return String(value).replace('T', ' ').substring(0, 16);
  },

  async handleMessageTap(e) {
    const index = e.currentTarget.dataset.index;
    const item = this.data.list[index];
    if (!item) return;

    if (item.unread) {
      await this.markRead(item.notifyId, index);
    }

    const drawId = item.payloadObj && item.payloadObj.drawId;
    if (drawId) {
      wx.navigateTo({ url: '/pages/drawDetail/drawDetail?drawId=' + drawId });
    }
  },

  handleGlobalNotifyTap() {
    const app = getApp();
    if (app && app.handleGlobalNotifyTap) {
      app.handleGlobalNotifyTap(this);
    }
  },

  async markRead(notifyId, index) {
    try {
      const res = await notifyApi.markRead(notifyId);
      if (res.code === 0) {
        this.setData({
          [`list[${index}].readFlag`]: 1,
          [`list[${index}].unread`]: false
        });
        const app = getApp();
        if (app && app.setUnreadCount) {
          app.setUnreadCount((app.globalData.unreadNotifyCount || 1) - 1);
        }
      }
    } catch (e) {
      // Mark read can be retried next time.
    }
  },

  async handleReadAll() {
    if (!this.data.list.some(item => item.unread)) {
      wx.showToast({ title: '暂无未读消息', icon: 'none' });
      return;
    }
    try {
      const res = await notifyApi.markAllRead();
      if (res.code === 0) {
        const list = this.data.list.map(item => ({ ...item, readFlag: 1, unread: false }));
        this.setData({ list });
        const app = getApp();
        if (app && app.setUnreadCount) {
          app.setUnreadCount(0);
        }
        wx.showToast({ title: '已全部标记', icon: 'success' });
      }
    } catch (e) {
      wx.showToast({ title: '操作失败', icon: 'none' });
    }
  }
});
