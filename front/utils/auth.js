// 本地存储工具类：统一管理登录状态、token、用户信息
const AUTH_KEYS = {
  isLogin: 'isLogin',
  token: 'token',
  userInfo: 'userInfo'
}

const auth = {
  // 保存登录信息（登录成功调用）
  setLoginInfo(token, userInfo) {
    try {
      wx.setStorageSync(AUTH_KEYS.isLogin, true)
      wx.setStorageSync(AUTH_KEYS.token, token || '')
      wx.setStorageSync(AUTH_KEYS.userInfo, userInfo || {})
    } catch (e) {
      console.error('存储登录信息失败：', e)
    }
  },

  // 获取登录状态
  isLogin() {
    try {
      return wx.getStorageSync(AUTH_KEYS.isLogin) || false
    } catch (e) {
      return false
    }
  },

  // 获取token
  getToken() {
    try {
      return wx.getStorageSync(AUTH_KEYS.token) || ''
    } catch (e) {
      return ''
    }
  },

  // 获取用户信息
  getUserInfo() {
    try {
      return wx.getStorageSync(AUTH_KEYS.userInfo) || {}
    } catch (e) {
      return {}
    }
  },

  // 退出登录（清除所有信息）
  logout() {
    try {
      wx.setStorageSync(AUTH_KEYS.isLogin, false)
      wx.removeStorageSync(AUTH_KEYS.token)
      wx.removeStorageSync(AUTH_KEYS.userInfo)
    } catch (e) {
      console.error('退出登录失败：', e)
    }
  }
}

export default auth