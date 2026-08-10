import drawApi from '../../utils/apis/draw.js';

Page({
  data: {
    drawId: null,
    loading: true,
    verifyInfo: null,
    winnerText: ''
  },

  onLoad(options) {
    if (!options.drawId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1200);
      return;
    }
    this.setData({ drawId: options.drawId });
    this.loadVerifyInfo();
  },

  async loadVerifyInfo() {
    this.setData({ loading: true });
    try {
      const res = await drawApi.getVerifyInfo(this.data.drawId);
      if (res.code === 0 && res.data) {
        const info = res.data;
        this.setData({
          verifyInfo: info,
          winnerText: (info.winnerCodes || []).join('、') || '暂无中奖码'
        });
      } else {
        wx.showToast({ title: res.msg || '获取失败', icon: 'none' });
      }
    } catch (e) {
      console.error('获取验证信息失败', e);
    }
    this.setData({ loading: false });
  },

  copyValue(e) {
    const value = e.currentTarget.dataset.value || '';
    if (!value) return;
    wx.setClipboardData({ data: value });
  }
});
