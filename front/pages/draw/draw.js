// pages/draw/draw.js
import drawApi from '../../utils/apis/draw.js';
import auth from '../../utils/auth.js';

Page({
  data: {
    passCode: '',
    searching: false,
    searchResult: null,
    searched: false
  },

  onPassCodeInput(e) {
    this.setData({ passCode: e.detail.value });
  },

  async handleSearch() {
    const code = this.data.passCode.trim();
    if (!code) {
      wx.showToast({ title: '请输入口令', icon: 'none' });
      return;
    }
    if (!auth.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }

    this.setData({ searching: true, searched: false, searchResult: null });

    try {
      const res = await drawApi.queryDrawByPC(code);
      if (res.code === 0 && res.data) {
        this.setData({
          searchResult: res.data,
          searched: true
        });
      } else {
        this.setData({ searched: true });
        wx.showToast({ title: res.msg || '未找到对应抽签', icon: 'none' });
      }
    } catch (e) {
      this.setData({ searched: true });
      wx.showToast({ title: '查询失败', icon: 'none' });
    }
    this.setData({ searching: false });
  },

  goToResult() {
    if (!this.data.searchResult) return;
    const drawId = this.data.searchResult.drawId;
    wx.navigateTo({ url: `/pages/drawDetail/drawDetail?drawId=${drawId}` });
  },

  // 扫码输入（调用微信扫码）
  handleScan() {
    wx.scanCode({
      onlyFromCamera: true,
      success: (res) => {
        // 假设二维码内容是口令
        this.setData({ passCode: res.result });
        this.handleSearch();
      },
      fail: () => {
        // 用户取消
      }
    });
  },

  // 清空搜索
  handleClear() {
    this.setData({
      passCode: '',
      searchResult: null,
      searched: false
    });
  }
});
