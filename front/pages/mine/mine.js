import api from '../../utils/apis/user.js'
import drawApi from '../../utils/apis/draw.js'
import auth from '../../utils/auth.js'

Page({
  data: {
    isLogin: false,
    // 用户信息
    userInfo: {
      avatarUrl: '',
      nickName: '点击登录',
      level: 4
    },
    // 统计数据
    stats: {
      totalDraw: 0,
      initiatedDraw: 0,
      winRecord: 0
    },
    // 功能列表
    funcList1: [
      { id: 1, name: '红包余额', icon: '/images/icons/wallet.png', path: '' },
      { id: 2, name: '优惠券', icon: '/images/icons/coupon.png', path: '' },
      { id: 3, name: '订单', icon: '/images/icons/order.png', path: '' },
      { id: 4, name: '商城', icon: '/images/icons/shop.png', path: '' }
    ],
    funcList2: [
      { id: 5, name: '高级版', icon: '/images/icons/vip.png', path: '' },
      { id: 6, name: '个人主页', icon: '/images/icons/homepage.png', path: '/pages/userInfo/userInfo' },
      { id: 7, name: '推广合作', icon: '/images/icons/cooperate.png', path: '' },
      { id: 8, name: '设置', icon: '/images/icons/setting.png', path: '/pages/setting/setting' }
    ]
  },

  onShow() {
    this.loadUserPageData()
  },

  /**
   * 统一加载页面数据（登录状态 + 用户信息）
   */
  async loadUserPageData() {
    // 1. 读取本地状态
    const isLogin = auth.isLogin()
    const localUserInfo = auth.getUserInfo()

    // 2. 更新页面
    this.setData({
      isLogin,
      userInfo: {
        ...this.data.userInfo,
        ...localUserInfo
      }
    })

    // 3. 未登录直接返回
    if (!isLogin) return

    // 4. 已登录 → 请求最新数据
    await Promise.all([this.getUserInfoFromApi(), this.loadStatistics()])
  },

  /**
   * 从接口拉取最新用户信息
   */
  async getUserInfoFromApi() {
    try {
      const res = await api.getUserInfo()

      if (res.code === 0) {
        const userData = res.data || {}
        this.setData({
          userInfo: {
            ...this.data.userInfo,
            ...userData
          }
        })

        // 同步更新本地存储（保持最新）
        auth.setLoginInfo(auth.getToken(), this.data.userInfo)
      }else if(res.code === 401){
        wx.showToast({ title: '登录信息已失效', icon: 'none' })
        // this.goLogin()
        auth.logout()
      }else {
        wx.showToast({ title: '获取信息失败', icon: 'none' })
      }
    } catch (err) {
      console.error('获取用户信息失败：', err)
      wx.showToast({ title: '网络异常', icon: 'none' })
    }
  },

  /**
   * 拉取抽签统计数据
   */
  async loadStatistics() {
    try {
      const res = await drawApi.queryStatistics()
      if (res.code === 0 && res.data) {
        this.setData({
          stats: {
            totalDraw: res.data.joinedCount || 0,
            initiatedDraw: res.data.publishedCount || 0,
            winRecord: res.data.rewardCount || 0
          }
        })
      }
    } catch (e) {
      // 统计非核心，静默失败
    }
  },

  // 去登录
  goLogin() {
    if (this.data.isLogin) return
    wx.navigateTo({ url: '/pages/login/login' })
  },

  // 退出登录
  goLogout() {
    if (!this.data.isLogin) return

    wx.showModal({
      title: '提示',
      content: '确定退出登录吗？',
      success: (res) => {
        if (!res.confirm) return

        // 清除登录状态
        auth.logout()

        // 重置页面
        this.setData({
          isLogin: false,
          userInfo: {
            avatarUrl: '',
            nickName: '点击登录',
            level: 4
          }
        })

        wx.showToast({ title: '已退出登录', icon: 'success' })
      }
    })
  },

  // 心愿
  goToWish() {
    if (!this.data.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' })
      return
    }
    wx.showToast({ title: '心愿功能开发中', icon: 'none' })
  },

  // 发布抽奖
  goToPublish() {
    wx.switchTab({ url: '/pages/publish/publish' })
  },

  // 统计项点击 → 进入抽签列表
  handleStatTap(e) {
    if (!this.data.isLogin) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    const tab = e.currentTarget.dataset.tab || 0;
    wx.navigateTo({ url: '/pages/drawList/drawList?tab=' + tab });
  },

  // 功能项点击
  handleFuncTap(e) {
    const { path } = e.currentTarget.dataset
    if (!path) {
      wx.showToast({ title: '该功能暂未开放', icon: 'none' })
      return
    }
    wx.navigateTo({ url: path })
  }
})