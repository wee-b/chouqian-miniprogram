import drawApi from '../../utils/apis/draw.js';

Page({
  data: {
    drawList: [],
    banners: [],
    loading: true,
    scrollTop: 0,
    statusBarHeight: 0,
    navBarHeight: 0,
    safeHeight: 0,
    bottomSafeHeight: 0,
    navBgOpacity: 0,
    showNavBtns: false
  },

  onLoad() {
    this.getSystemInfo();
    this.loadBanners();
    this.getOfficialDrawList();
  },

  loadBanners() {
    this.setData({
      banners: [
        {
          image: 'https://images.unsplash.com/photo-1695048133142-1a20484d2569?w=800&h=400&fit=crop',
          title: 'iPhone 17 Pro Max'
        },
        {
          image: 'https://images.unsplash.com/photo-1587202372775-e229f172b9d7?w=800&h=400&fit=crop',
          title: 'NVIDIA RTX 5090'
        },
        {
          image: 'https://images.unsplash.com/photo-1507582020474-9a35b7d455d9?w=800&h=400&fit=crop',
          title: 'DJI 大疆无人机'
        }
      ]
    });
  },

  onBannerError(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ ['banners[' + index + '].image']: '/images/default-draw.png' });
  },

  onShow() {
    // 每次返回首页时更新 active tab 样式（适配 tabbar 切换场景，不做多余请求）
  },

  getSystemInfo() {
    const windowInfo = wx.getWindowInfo();
    const menuButtonInfo = wx.getMenuButtonBoundingClientRect();

    let statusBarHeight = windowInfo.statusBarHeight;
    let navBarHeight = menuButtonInfo.height + (menuButtonInfo.top - statusBarHeight) * 2;
    let safeHeight = windowInfo.safeArea.height;
    let bottomSafeHeight = windowInfo.screenHeight - windowInfo.safeArea.height - statusBarHeight;

    this.setData({ statusBarHeight, navBarHeight, safeHeight, bottomSafeHeight });
  },

  async getOfficialDrawList() {
    try {
      const res = await drawApi.getOfficialDraw();
      if (res.code === 0 && res.ok) {
        this.setData({ drawList: res.data, loading: false });
      } else {
        wx.showToast({ title: res.msg || '获取失败', icon: 'none' });
        this.setData({ loading: false });
      }
    } catch (err) {
      wx.showToast({ title: '网络请求失败', icon: 'none' });
      this.setData({ loading: false });
    }
    wx.stopPullDownRefresh();
  },

  onPullDownRefresh() {
    this.setData({ loading: true });
    this.getOfficialDrawList();
  },

  goDetail(e) {
    const drawId = e.currentTarget.dataset.id;
    wx.navigateTo({ url: '/pages/drawDetail/drawDetail?drawId=' + drawId });
  },

  goPassCode() {
    wx.navigateTo({ url: '/pages/draw/draw' });
  },

  goCheckIn() {
    wx.showToast({ title: '签到功能开发中', icon: 'none' });
  },

  onCoverError(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ ['drawList[' + index + '].drawCover']: '/images/default-draw.png' });
  },

  onAvatarError(e) {
    const index = e.currentTarget.dataset.index;
    this.setData({ ['drawList[' + index + '].publisherAvatar']: '/images/default-avatar.png' });
  },

  onPageScroll(e) {
    const scrollTop = e.detail.scrollTop;
    const opacity = Math.min(scrollTop / 200, 1);
    // quick-bar 完全划出屏幕后再显示导航栏按钮
    const showNavBtns = scrollTop > this.data.navBarHeight + 120;
    this.setData({
      scrollTop,
      navBgOpacity: opacity.toFixed(2),
      showNavBtns
    });
  }
});
