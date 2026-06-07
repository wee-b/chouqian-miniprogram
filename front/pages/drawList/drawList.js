import drawApi from '../../utils/apis/draw.js';
import auth from '../../utils/auth.js';

const PAGE_SIZE = 10;
const STATUS_MAP = { 0: '草稿', 1: '进行中', 2: '已开奖', 3: '流局' };

const TAB_CONFIG = [
  { label: '我参与的', queryType: 1, api: 'queryJoinedList' },
  { label: '我发布的', queryType: 2, api: 'queryPublishedList' },
  { label: '我中奖的', queryType: 3, api: 'queryRewardedList' }
];

Page({
  data: {
    activeTab: 0,
    tabs: TAB_CONFIG.map(t => t.label),
    list: [],
    empty: false,
    loading: false,
    hasMore: true,
    page: 1
  },

  onLoad(options) {
    if (!auth.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const tab = parseInt(options.tab) || 0;
    this.setData({ activeTab: tab });
    this.loadData();
  },

  onShow() {
    if (!auth.isLogin()) {
      this.setData({ list: [], empty: true, hasMore: false });
    }
  },

  switchTab(e) {
    const index = e.currentTarget.dataset.index;
    if (index === this.data.activeTab) return;
    this.setData({
      activeTab: index,
      list: [],
      page: 1,
      hasMore: true,
      empty: false
    });
    this.loadData();
  },

  async loadData() {
    if (this.data.loading || !this.data.hasMore) return;
    this.setData({ loading: true });

    const cfg = TAB_CONFIG[this.data.activeTab];
    try {
      const params = {
        page: this.data.page,
        pageSize: PAGE_SIZE,
        queryType: cfg.queryType
      };
      const res = await drawApi[cfg.api](params);
      if (res.code === 0) {
        const result = res.data || {};
        const newList = (result.data || []).map(function(item) {
          item.statusText = STATUS_MAP[item.status] || '未知';
          return item;
        });
        const total = result.total || (result.Total ? result.Total : 0);

        const list = this.data.list.concat(newList);
        this.setData({
          list,
          empty: list.length === 0,
          hasMore: list.length < total,
          page: this.data.page + 1
        });
      } else {
        wx.showToast({ title: res.msg || '加载失败', icon: 'none' });
      }
    } catch (e) {
      wx.showToast({ title: '网络异常', icon: 'none' });
    }
    this.setData({ loading: false });
  },

  loadMore() {
    this.loadData();
  },

  goDetail(e) {
    const drawId = e.currentTarget.dataset.drawid;
    wx.navigateTo({ url: '/pages/drawDetail/drawDetail?drawId=' + drawId });
  },

  onShareAppMessage() {
    return { title: '抽签列表', path: '/pages/drawList/drawList' };
  }
});
