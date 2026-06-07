import request from '../request.js';

export default {

  // ====================== 抽签活动 ======================

  // 获取官方抽奖列表
  getOfficialDraw() {
    return request({
      url: '/client/draw/getOfficialDraw',
      method: 'GET'
    });
  },

  // 获取抽签详情
  getDetailDraw(drawId) {
    return request({
      url: '/client/draw/detail',
      method: 'GET',
      data: { drawId }
    });
  },

  // 创建抽签
  createDraw(data) {
    return request({
      url: '/client/draw/create',
      method: 'POST',
      data
    });
  },

  // 修改抽签
  updateDraw(data) {
    return request({
      url: '/client/draw/update',
      method: 'POST',
      data
    });
  },

  // 删除抽签 (@RequestParam)
  deleteDraw(drawId) {
    return request({
      url: `/client/draw/delete?drawId=${drawId}`,
      method: 'POST'
    });
  },

  // 发布抽签 (@RequestParam)
  publishDraw(drawId) {
    return request({
      url: `/client/draw/publish?drawId=${drawId}`,
      method: 'POST'
    });
  },

  // ====================== 口令 ======================

  // 生成口令
  generatePassCode(data) {
    return request({
      url: '/client/draw/generatePassCode',
      method: 'POST',
      data
    });
  },

  // 禁用口令 (@RequestParam)
  banPassCode(passCode) {
    return request({
      url: `/client/draw/banPassCode?passCode=${passCode}`,
      method: 'POST'
    });
  },

  // 查询口令
  queryPassCode(drawId) {
    return request({
      url: '/client/draw/queryPassCode',
      method: 'GET',
      data: { drawId }
    });
  },

  // 根据口令查询抽奖
  queryDrawByPC(passCode) {
    return request({
      url: '/client/draw/queryDrawByPC',
      method: 'GET',
      data: { passCode }
    });
  },

  // ====================== 我的抽签列表 ======================

  // 查询我参与的所有抽签
  queryJoinedList(data) {
    return request({
      url: '/client/draw/queryJoinedList',
      method: 'POST',
      data
    });
  },

  // 查询我发布的所有抽签
  queryPublishedList(data) {
    return request({
      url: '/client/draw/queryPublishedList',
      method: 'POST',
      data
    });
  },

  // 查询我中奖的所有抽签
  queryRewardedList(data) {
    return request({
      url: '/client/draw/queryRewardedList',
      method: 'POST',
      data
    });
  },

  // 查询参与抽签的统计数据
  queryStatistics() {
    return request({
      url: '/client/draw/queryStatistics',
      method: 'GET'
    });
  },

  // ====================== 参与抽签 ======================

  // 参与抽签 (@RequestParam)
  joinDraw(drawId) {
    return request({
      url: `/client/drawCode/join?drawId=${drawId}`,
      method: 'POST'
    });
  },

  // 查询我的参与码
  getMyCodes(drawId) {
    return request({
      url: '/client/drawCode/myCodes',
      method: 'GET',
      data: { drawId }
    });
  },

  // 手动开奖 (PathVariable)
  openDraw(drawId) {
    return request({
      url: `/client/draw/open/${drawId}`,
      method: 'POST'
    });
  },

  // ====================== 中奖名单 ======================

  getWinners(drawId) {
    return request({
      url: '/client/draw/winners',
      method: 'GET',
      data: { drawId }
    });
  },

  // ====================== 奖品 ======================

  // 根据抽奖ID获取奖品列表
  getPrizesByDrawId(drawId) {
    return request({
      url: '/client/prize/getPrizesByDrawId',
      method: 'GET',
      data: { drawId }
    });
  },

  // 批量添加奖品
  batchAddPrize(prizes) {
    return request({
      url: '/client/prize/batchAddPrize',
      method: 'POST',
      data: prizes
    });
  },

  // 修改奖品
  updatePrize(data) {
    return request({
      url: '/client/prize/updatePrize',
      method: 'POST',
      data
    });
  },

  // 批量删除奖品 (@RequestParam List)
  batchDeletePrize(ids) {
    const query = ids.map(id => `ids=${id}`).join('&');
    return request({
      url: `/client/prize/batchDeletePrize?${query}`,
      method: 'POST'
    });
  }
};
