import userApi from '../../utils/apis/user.js';
import auth from '../../utils/auth.js';

Page({
  data: {
    account: '',
    pwd: '',
    showPwd: false,
    submitting: false
  },

  onAccountInput(e) {
    this.setData({ account: e.detail.value });
  },

  onPwdInput(e) {
    this.setData({ pwd: e.detail.value });
  },

  togglePwd() {
    this.setData({ showPwd: !this.data.showPwd });
  },

  async doLogin() {
    if (this.data.submitting) return;

    const phone = this.data.account.trim();
    const password = this.data.pwd;

    if (!phone) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (!/^1\d{10}$/.test(phone)) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' });
      return;
    }
    if (!password) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '登录中...', mask: true });

    try {
      const res = await userApi.login({ phone, password });
      wx.hideLoading();

      if (res.code === 0) {
        const token = res.data.token;
        const user = res.data.user || {};
        const userInfo = {
          userId: user.userId,
          nickName: user.userName || '用户' + phone.slice(-4),
          avatarUrl: user.avatarUrl || '/images/login-avatar.png',
          phone: phone,
          level: user.level || 1
        };

        auth.setLoginInfo(token, userInfo);
        wx.showToast({ title: '登录成功', icon: 'success' });
        setTimeout(() => wx.navigateBack(), 1200);
      } else {
        wx.showToast({ title: res.msg || '登录失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('登录异常', err);
      wx.showToast({ title: '网络异常，请重试', icon: 'none' });
    }

    this.setData({ submitting: false });
  },

  goRegister() {
    wx.navigateTo({ url: '/pages/register/register' });
  }
});
