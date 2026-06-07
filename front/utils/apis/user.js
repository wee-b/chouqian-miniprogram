// utils/apis/user.js
import request from '../request.js';

export default {
  // 登录
  login(data) {
    return request({
      url: '/client/user/login',
      method: 'POST',
      data
    });
  },

  // 注册
  register(data) {
    return request({
      url: '/client/user/register',
      method: 'POST',
      data
    });
  },

  // 退出登录
  logout() {
    return request({
      url: '/client/user/logout',
      method: 'POST'
    });
  },

  // 获取用户信息
  getUserInfo(){
    return request({
      url:'/client/user/me',
      method:'GET'
    });
  },

  // ✅ 修改用户信息（头像、昵称、手机号）
  updateUserInfo(data){
    return request({
      url:'/client/user/updateInfo',
      method:'POST',
      data
    });
  },

  // ✅ 单独上传头像接口（小程序专用）
  uploadAvatar(filePath) {
    return wx.uploadFile({
      url: 'https://你的域名/client/user/uploadAvatar',
      filePath: filePath,
      name: 'avatar',
      header: {
        token: wx.getStorageSync('token')
      }
    });
  }
};