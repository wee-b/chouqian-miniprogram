// 基础地址（你的SpringBoot后端IP）
const baseUrl = "http://10.138.253.232:8080";
import auth from './auth.js';

// ======================
// 白名单：不需要 token 的接口
// ======================
const WHITE_LIST = [
  "/client/user/login",
  "/client/user/register",
  "/client/draw/getOfficialDraw"
];

// ======================
// 日志打印工具方法
// ======================
const logger = {
  // 请求开始日志
  request(url, method, data) {
    // console.log(`\n🚀 【请求接口】${method} ${url}`);
    // console.log(`📦 请求参数：`, data);
  },
  // 请求成功日志
  success(url, res) {
    console.log(`✅ 【请求成功】${url}`);
    console.log(`📄 响应结果：`, res);
  },
  // 请求失败日志
  fail(url, err) {
    console.log(`❌ 【请求失败】${url}`);
    console.log(`💥 错误信息：`, err);
  }
};

// 统一请求封装
const request = (options) => {
  return new Promise((resolve, reject) => {
    const url = options.url;
    const method = options.method || "GET";
    const data = options.data || {};
    const token = auth.getToken();
    const isLogin = auth.isLogin();

    // ======================
    // 打印请求日志
    // ======================
    logger.request(url, method, data);

    // ======================
    // 请求前拦截：非白名单必须登录
    // ======================
    if (!WHITE_LIST.includes(url)) {
      if (!isLogin || !token) {
        wx.showModal({
          title: "请先登录",
          content: "您还未登录或登录已失效",
          showCancel: false,
          success: () => {
            wx.reLaunch({ url: "/pages/login/login" });
          }
        });
        reject("未登录，已拦截请求");
        return;
      }
    }

    // 配置请求头
    let header = {
      "content-type": "application/json"
    };

    // 非白名单自动携带 Bearer token
    if (!WHITE_LIST.includes(url) && token) {
      header["Authorization"] = "Bearer " + token;
    }

    // 发送请求
    wx.request({
      url: baseUrl + url,
      method: method,
      data: data,
      header: header,
      success: (res) => {
        // 打印成功日志
        logger.success(url, res.data);
        resolve(res.data);
      },
      fail: (err) => {
        // 打印失败日志
        logger.fail(url, err);
        wx.showToast({ title: "网络异常", icon: "none" });
        reject(err);
      }
    });
  });
};

export default request;