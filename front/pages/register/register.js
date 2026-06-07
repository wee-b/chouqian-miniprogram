import userApi from '../../utils/apis/user.js';
import auth from '../../utils/auth.js';

Page({
  data: {
    phone: '',
    userName: '',
    pwd: '',
    confirmPwd: '',
    showPwd: false,
    showConfirmPwd: false,
    submitting: false,
    agreeTerms: false
  },

  onPhoneInput(e) { this.setData({ phone: e.detail.value }); },
  onUserNameInput(e) { this.setData({ userName: e.detail.value }); },
  onPwdInput(e) { this.setData({ pwd: e.detail.value }); },
  onConfirmPwdInput(e) { this.setData({ confirmPwd: e.detail.value }); },

  togglePwd() {
    this.setData({ showPwd: !this.data.showPwd });
  },

  toggleConfirmPwd() {
    this.setData({ showConfirmPwd: !this.data.showConfirmPwd });
  },

  toggleAgree() {
    this.setData({ agreeTerms: !this.data.agreeTerms });
  },

  async doRegister() {
    if (this.data.submitting) return;

    const { phone, userName, pwd, confirmPwd, agreeTerms } = this.data;

    // 校验
    if (!phone.trim()) {
      wx.showToast({ title: '请输入手机号', icon: 'none' });
      return;
    }
    if (!/^1\d{10}$/.test(phone.trim())) {
      wx.showToast({ title: '手机号格式不正确', icon: 'none' });
      return;
    }
    if (!userName.trim()) {
      wx.showToast({ title: '请输入昵称', icon: 'none' });
      return;
    }
    if (userName.trim().length > 20) {
      wx.showToast({ title: '昵称不能超过20个字符', icon: 'none' });
      return;
    }
    if (!pwd) {
      wx.showToast({ title: '请输入密码', icon: 'none' });
      return;
    }
    if (pwd.length < 6) {
      wx.showToast({ title: '密码不能少于6位', icon: 'none' });
      return;
    }
    if (pwd !== confirmPwd) {
      wx.showToast({ title: '两次密码不一致', icon: 'none' });
      return;
    }
    if (!agreeTerms) {
      wx.showToast({ title: '请先阅读并同意用户协议', icon: 'none' });
      return;
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '注册中...', mask: true });

    try {
      const res = await userApi.register({
        phone: phone.trim(),
        userName: userName.trim(),
        password: pwd,
        registerSource: 1
      });
      wx.hideLoading();

      if (res.code === 0) {
        wx.showToast({ title: '注册成功', icon: 'success' });
        setTimeout(() => wx.navigateBack(), 1500);
      } else {
        wx.showToast({ title: res.msg || '注册失败', icon: 'none' });
      }
    } catch (err) {
      wx.hideLoading();
      console.error('注册异常', err);
      wx.showToast({ title: '网络异常，请重试', icon: 'none' });
    }

    this.setData({ submitting: false });
  },

  goLogin() {
    wx.navigateBack();
  }
});
