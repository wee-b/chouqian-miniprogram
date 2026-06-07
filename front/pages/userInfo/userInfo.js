// pages/userInfo/userInfo.js
import api from '../../utils/apis/user.js'
import auth from '../../utils/auth.js'

Page({
  data: {
    isLogin: false,
    userDetailInfo: {
      avatarUrl: '',
      nickName: '点击登录',
      level: 4,
      memberCode: '',
      phone: '',
      email: '',
      createTime: ''
    },
    stats: {}
  },

  onShow() {
    this.loadUserPageData()
  },

  // 加载页面数据
  async loadUserPageData() {
    const isLogin = auth.isLogin()
    const localUserInfo = auth.getUserInfo() || {}

    this.setData({
      isLogin,
      userDetailInfo: {
        ...this.data.userDetailInfo,
        ...localUserInfo
      }
    })

    if (!isLogin) return
    await this.getUserInfoFromApi()
  },

  // 从接口拉取用户信息
  async getUserInfoFromApi() {
    try {
      const res = await api.getUserInfo()
      if (res.code === 0) {
        const userData = res.data || {}
        this.setData({
          userDetailInfo: {
            ...this.data.userDetailInfo,
            ...userData
          },
          stats: userData.stats || this.data.stats
        })
        auth.setLoginInfo(auth.getToken(), this.data.userDetailInfo)
      }
    } catch (err) {
      console.error(err)
    }
  },

  // ======================================
  // ✅ 以下是【修改信息】核心功能
  // ======================================

  // 1. 修改头像
  async changeAvatar() {
    if (!this.data.isLogin) {
      wx.showToast({ title: '请先登录', icon: 'none' })
      return
    }

    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      success: async (res) => {
        const filePath = res.tempFiles[0].tempFilePath
        
        // 上传头像到服务器
        const uploadRes = await api.uploadAvatar(filePath)
        const data = JSON.parse(uploadRes.data)
        
        if (data.code === 0) {
          const avatarUrl = data.data.avatarUrl
          
          this.setData({
            'userDetailInfo.avatarUrl': avatarUrl
          })

          // 保存到本地 & 后端
          auth.setLoginInfo(auth.getToken(), this.data.userDetailInfo)
          await this.saveUserInfo()
          
          wx.showToast({ title: '头像更新成功' })
        }
      }
    })
  },

  // 2. 修改昵称
  changeNickName() {
    if (!this.data.isLogin) return
    wx.showModal({
      title: '修改昵称',
      editable: true,
      placeholderText: '请输入新昵称',
      success: async (res) => {
        if (res.confirm && res.content) {
          this.setData({
            'userDetailInfo.nickName': res.content
          })
          await this.saveUserInfo()
        }
      }
    })
  },

  // 3. 修改手机号
  changePhone() {
    if (!this.data.isLogin) return
    wx.showModal({
      title: '修改手机号',
      editable: true,
      placeholderText: '请输入新手机号',
      success: async (res) => {
        if (res.confirm && res.content) {
          this.setData({
            'userDetailInfo.phone': res.content
          })
          await this.saveUserInfo()
        }
      }
    })
  },

  // 4. 保存用户信息到后端
  async saveUserInfo() {
    try {
      const res = await api.updateUserInfo(this.data.userDetailInfo)
      if (res.code === 0) {
        auth.setLoginInfo(auth.getToken(), this.data.userDetailInfo)
        return true
      }
    } catch (err) {
      wx.showToast({ title: '保存失败', icon: 'none' })
      return false
    }
  },

  // 去登录
  goLogin() {
    wx.navigateTo({ url: '/pages/login/login' })
  },

  // 退出登录
  logout() {
    wx.showModal({
      title: '提示',
      content: '确定退出登录？',
      success: () => {
        auth.logout()
        this.loadUserPageData()
      }
    })
  }
})