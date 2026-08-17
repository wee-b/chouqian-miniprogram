// 本机局域网调试：
const baseUrl = 'http://192.168.31.6:8080';
// 内网穿透真机调试：必须使用证书绑定的域名，不要写公网 IP。
// const baseUrl = 'https://frp-tip.com:34767';
const wsBaseUrl = baseUrl.startsWith('https://')
  ? baseUrl.replace(/^https/, 'wss')
  : baseUrl.replace(/^http/, 'ws');

export { baseUrl, wsBaseUrl };
