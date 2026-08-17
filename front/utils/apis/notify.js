import request from '../request.js';

export default {
  page(data) {
    return request({
      url: '/client/notify/page',
      method: 'POST',
      data
    });
  },

  unreadCount() {
    return request({
      url: '/client/notify/unreadCount',
      method: 'GET'
    });
  },

  markRead(notifyId) {
    return request({
      url: `/client/notify/read/${notifyId}`,
      method: 'PUT'
    });
  },

  markAllRead() {
    return request({
      url: '/client/notify/readAll',
      method: 'PUT'
    });
  }
};
