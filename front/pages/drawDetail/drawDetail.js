// pages/drawDetail/drawDetail.js
import drawApi from '../../utils/apis/draw.js';
import auth from '../../utils/auth.js';

const STATUS_MAP = { 0: '草稿', 1: '参与中', 2: '已开奖', 3: '已流局' };
const WAY_MAP = { 0: '按时间开奖', 1: '按人数开奖' };
const TYPE_MAP = { 1: '一等奖', 2: '二等奖', 3: '三等奖', 4: '四等奖', 5: '五等奖', 6: '六等奖' };
const TYPE_NAMES = ['', '一等奖', '二等奖', '三等奖', '四等奖', '五等奖', '六等奖'];
const GIVEAWAY_NAMES = ['', '快递邮寄', '联系发布者', '填写信息', '其他'];

Page({
  data: {
    drawId: null,
    drawInfo: null,
    prizes: [],
    winners: [],
    myCodes: [],
    passCodeInfo: null,
    joined: false,
    isOwner: false,
    loading: true,

    // 预处理字段
    statusText: '',
    statusClass: '',
    wayText: '',
    deadlineText: '',
    prizeCount: 0,
    codeCount: 0,

    // 口令
    showPassCodeModal: false,
    passCodeExpireHours: 24,
    passCodeExpireText: '',
    generating: false,

    // 编辑模式
    isEditing: false,
    savingEdit: false,
    editForm: {},

    // 奖品编辑弹层
    addingPrize: false,
    editingPrizeIndex: -1,
    prizeForm: { prizeName: '', prizeCover: '', prizeType: 1, amount: 1, giveaway: 4 },
    prizeTypeName: '一等奖',
    prizeGiveawayName: '其他'
  },

  onLoad(options) {
    const drawId = options.drawId;
    if (!drawId) {
      wx.showToast({ title: '参数错误', icon: 'none' });
      setTimeout(() => wx.navigateBack(), 1500);
      return;
    }
    this.setData({ drawId });
  },

  onShow() {
    this.loadAllData();
  },

  async loadAllData() {
    this.setData({ loading: true });
    try {
      await Promise.all([this.loadDrawDetail(), this.loadPrizes()]);
      var info = this.data.drawInfo;
      if (info && info.status === 2) {
        this.loadWinners();
      }
      if (auth.isLogin() && info) {
        this.loadMyCodes();
      }
    } catch (e) {
      console.error('加载数据失败', e);
    }
    this.setData({ loading: false });
  },

  async loadDrawDetail() {
    try {
      const res = await drawApi.getDetailDraw(this.data.drawId);
      if (res.code === 0 && res.data) {
        const info = res.data;
        const status = (info.status != null) ? info.status : 1;
        const deadlineText = this.formatDeadline(info.joinDeadline);

        this.setData({
          drawInfo: info,
          isOwner: info.isOwner || false,
          statusText: STATUS_MAP[status] || '未知',
          statusClass: 's' + status,
          wayText: WAY_MAP[info.drawingWay] || '未知',
          deadlineText: deadlineText
        });
      } else {
        wx.showToast({ title: res.msg || '获取详情失败', icon: 'none' });
      }
    } catch (e) {
      console.error('获取详情失败', e);
    }
  },

  formatDeadline(d) {
    if (!d) return '--';
    if (Array.isArray(d)) {
      var arr = d;
      return arr[0] + '-' + this.pad(arr[1]) + '-' + this.pad(arr[2]) + ' ' + this.pad(arr[3]) + ':' + this.pad(arr[4]);
    }
    var s = String(d);
    return s.replace('T', ' ');
  },

  parseDeadline(d) {
    if (!d) return { date: '', time: '' };
    var s = '';
    if (Array.isArray(d)) {
      var a = d;
      var p = function(n) { return n < 10 ? '0' + n : '' + n; };
      s = a[0] + '-' + p(a[1]) + '-' + p(a[2]) + 'T' + p(a[3]) + ':' + p(a[4]);
    } else {
      s = String(d);
    }
    var parts = s.split('T');
    return {
      date: parts[0] || '',
      time: (parts[1] || '').substring(0, 5)
    };
  },

  pad(n) {
    return n < 10 ? '0' + n : '' + n;
  },

  async loadPrizes() {
    try {
      const res = await drawApi.getPrizesByDrawId(this.data.drawId);
      if (res.code === 0) {
        var list = res.data || [];
        list = list.map(function(item, i) {
          item.typeName = TYPE_MAP[item.prizeType] || '奖品' + (i + 1);
          return item;
        });
        this.setData({ prizes: list, prizeCount: list.length });
      }
    } catch (e) {
      console.error('获取奖品失败', e);
    }
  },

  async loadMyCodes() {
    try {
      const res = await drawApi.getMyCodes(this.data.drawId);
      if (res.code === 0) {
        var list = res.data || [];
        this.setData({
          myCodes: list,
          joined: list.length > 0,
          codeCount: list.length
        });
      }
    } catch (e) { /* 未参与 */ }
  },

  async loadWinners() {
    try {
      const res = await drawApi.getWinners(this.data.drawId);
      if (res.code === 0) {
        var list = res.data || [];
        this.setData({ winners: list });
      }
    } catch (e) {
      console.error('获取中奖名单失败', e);
    }
  },

  // ====================== 编辑模式 ======================

  handleEdit() {
    var info = this.data.drawInfo;
    var dd = this.parseDeadline(info.joinDeadline);

    this.setData({
      isEditing: true,
      editForm: {
        title: info.title || '',
        drawCover: info.drawCover || '',
        description: info.description || '',
        hasPrize: (info.hasPrize != null) ? info.hasPrize : 0,
        drawingWay: (info.drawingWay != null) ? info.drawingWay : 0,
        deadlineDate: dd.date,
        deadlineTime: dd.time,
        minPerson: info.minPerson ? String(info.minPerson) : '',
        perCodeNum: info.perCodeNum || 5,
        prizes: this.data.prizes.map(function(p) {
          return {
            prizeName: p.prizeName,
            prizeCover: p.prizeCover,
            prizeType: p.prizeType || 1,
            amount: p.amount || 1,
            giveaway: p.giveaway || 4,
            typeName: TYPE_MAP[p.prizeType] || ('奖品' + (p.prizeType || ''))
          };
        })
      }
    });
  },

  handleCancelEdit() {
    var that = this;
    wx.showModal({
      title: '放弃编辑',
      content: '确定放弃当前修改吗？',
      success: function(mr) {
        if (mr.confirm) {
          that.setData({ isEditing: false });
          that.loadPrizes();
        }
      }
    });
  },

  async handleSaveEdit() {
    if (this.data.savingEdit) return;
    var form = this.data.editForm;

    if (!form.title.trim()) {
      wx.showToast({ title: '请输入抽签标题', icon: 'none' });
      return;
    }
    if (!form.deadlineDate) {
      wx.showToast({ title: '请选择截止日期', icon: 'none' });
      return;
    }

    var joinDeadline = form.deadlineDate + 'T' + (form.deadlineTime || '23:59') + ':00';

    var params = {
      drawId: parseInt(this.data.drawId),
      title: form.title.trim(),
      drawCover: form.drawCover || '',
      description: form.description.trim(),
      hasPrize: form.hasPrize,
      drawingWay: form.drawingWay,
      joinDeadline: joinDeadline,
      minPerson: form.minPerson ? parseInt(form.minPerson) : 0,
      perCodeNum: form.perCodeNum || 5
    };

    this.setData({ savingEdit: true });
    wx.showLoading({ title: '保存中...', mask: true });

    var that = this;
    try {
      const res = await drawApi.updateDraw(params);
      wx.hideLoading();
      if (res.code === 0) {
        // 同步奖品
        if (form.hasPrize === 1) {
          await this.syncPrizes(this.data.drawId);
        }
        wx.showToast({ title: '修改成功', icon: 'success' });
        this.setData({ isEditing: false, savingEdit: false });
        this.loadAllData();
      } else {
        wx.showToast({ title: res.msg || '保存失败', icon: 'none' });
        this.setData({ savingEdit: false });
      }
    } catch (e) {
      wx.hideLoading();
      this.setData({ savingEdit: false });
    }
  },

  syncPrizes(drawId) {
    var prizes = this.data.editForm.prizes.map(function(p) {
      return {
        drawId: parseInt(drawId),
        prizeName: p.prizeName,
        prizeCover: p.prizeCover || '',
        prizeType: p.prizeType || 1,
        amount: p.amount || 1,
        giveaway: p.giveaway || 4
      };
    });
    return drawApi.batchAddPrize(prizes).catch(function(e) {
      console.error('同步奖品失败', e);
    });
  },

  // 编辑表单输入
  onEditTitleInput(e) { this.setData({ 'editForm.title': e.detail.value }); },
  onEditDescInput(e) { this.setData({ 'editForm.description': e.detail.value }); },
  onEditMinPerson(e) { this.setData({ 'editForm.minPerson': e.detail.value }); },
  onEditPerCodeInput(e) { this.setData({ 'editForm.perCodeNum': parseInt(e.detail.value) || 5 }); },

  onEditPerCodeStep(e) {
    var d = parseInt(e.currentTarget.dataset.delta);
    var v = (this.data.editForm.perCodeNum || 5) + d;
    if (v < 1) v = 1;
    if (v > 50) v = 50;
    this.setData({ 'editForm.perCodeNum': v });
  },

  onEditHasPrize(e) {
    var val = parseInt(e.currentTarget.dataset.value);
    this.setData({ 'editForm.hasPrize': val });
    if (val === 0) this.setData({ 'editForm.prizes': [] });
  },

  onEditDrawingWay(e) {
    this.setData({ 'editForm.drawingWay': parseInt(e.currentTarget.dataset.value) });
  },

  onEditDeadlineDate(e) {
    this.setData({ 'editForm.deadlineDate': e.detail.value });
  },

  onEditDeadlineTime(e) {
    this.setData({ 'editForm.deadlineTime': e.detail.value });
  },

  // 封面图
  onChooseCover() {
    var that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        var path = res.tempFiles[0].tempFilePath;
        that.setData({ 'editForm.drawCover': path });
      }
    });
  },

  // ====================== 奖品编辑弹层 ======================

  showAddPrize() {
    this.setData({
      addingPrize: true,
      editingPrizeIndex: -1,
      prizeForm: { prizeName: '', prizeCover: '', prizeType: 1, amount: 1, giveaway: 4 },
      prizeTypeName: '一等奖',
      prizeGiveawayName: '其他'
    });
  },

  hidePrizeForm() {
    this.setData({ addingPrize: false, editingPrizeIndex: -1 });
  },

  onPrizeNameInput(e) { this.setData({ 'prizeForm.prizeName': e.detail.value }); },
  onPrizeAmountInput(e) { this.setData({ 'prizeForm.amount': parseInt(e.detail.value) || 1 }); },

  onPrizeTypePrev() {
    var t = this.data.prizeForm.prizeType;
    if (t > 1) {
      t--;
      this.setData({ 'prizeForm.prizeType': t, prizeTypeName: TYPE_NAMES[t] });
    }
  },

  onPrizeTypeNext() {
    var t = this.data.prizeForm.prizeType;
    if (t < 6) {
      t++;
      this.setData({ 'prizeForm.prizeType': t, prizeTypeName: TYPE_NAMES[t] });
    }
  },

  onGiveawayPrev() {
    var g = this.data.prizeForm.giveaway;
    if (g > 1) {
      g--;
      this.setData({ 'prizeForm.giveaway': g, prizeGiveawayName: GIVEAWAY_NAMES[g] });
    }
  },

  onGiveawayNext() {
    var g = this.data.prizeForm.giveaway;
    if (g < 4) {
      g++;
      this.setData({ 'prizeForm.giveaway': g, prizeGiveawayName: GIVEAWAY_NAMES[g] });
    }
  },

  onChoosePrizeCover() {
    var that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        var path = res.tempFiles[0].tempFilePath;
        that.setData({ 'prizeForm.prizeCover': path });
      }
    });
  },

  savePrize() {
    var form = this.data.prizeForm;
    if (!form.prizeName.trim()) {
      wx.showToast({ title: '请输入奖品名称', icon: 'none' });
      return;
    }
    var prizes = this.data.editForm.prizes.slice();
    var entry = {
      prizeName: form.prizeName,
      prizeCover: form.prizeCover || '',
      prizeType: form.prizeType,
      amount: form.amount,
      giveaway: form.giveaway,
      typeName: TYPE_NAMES[form.prizeType]
    };
    if (this.data.editingPrizeIndex >= 0) {
      prizes[this.data.editingPrizeIndex] = entry;
    } else {
      prizes.push(entry);
    }
    this.setData({ 'editForm.prizes': prizes, addingPrize: false, editingPrizeIndex: -1 });
  },

  editPrize(e) {
    var i = e.currentTarget.dataset.index;
    var p = this.data.editForm.prizes[i];
    this.setData({
      addingPrize: true,
      editingPrizeIndex: i,
      prizeForm: {
        prizeName: p.prizeName,
        prizeCover: p.prizeCover || '',
        prizeType: p.prizeType || 1,
        amount: p.amount || 1,
        giveaway: p.giveaway || 4
      },
      prizeTypeName: TYPE_NAMES[p.prizeType] || '一等奖',
      prizeGiveawayName: GIVEAWAY_NAMES[p.giveaway] || '其他'
    });
  },

  removePrize(e) {
    var i = e.currentTarget.dataset.index;
    var prizes = this.data.editForm.prizes.slice();
    prizes.splice(i, 1);
    this.setData({ 'editForm.prizes': prizes });
  },

  // ====================== 操作 ======================

  async handleJoin() {
    if (!auth.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
      return;
    }
    wx.showLoading({ title: '参与中...', mask: true });
    try {
      const res = await drawApi.joinDraw(this.data.drawId);
      wx.hideLoading();
      if (res.code === 0) {
        wx.showToast({ title: '参与成功', icon: 'success' });
        this.loadMyCodes();
      } else {
        wx.showToast({ title: res.msg || '参与失败', icon: 'none' });
      }
    } catch (e) {
      wx.hideLoading();
    }
  },

  handleOpenDraw() {
    var that = this;
    wx.showModal({
      title: '确认开奖',
      content: '确定立即开奖吗？不可撤销。',
      success: function(mr) {
        if (!mr.confirm) return;
        wx.showLoading({ title: '开奖中...', mask: true });
        drawApi.openDraw(that.data.drawId).then(function(res) {
          wx.hideLoading();
          if (res.code === 0) {
            wx.showToast({ title: '开奖成功', icon: 'success' });
            that.loadAllData();
            that.loadWinners();
          } else {
            wx.showToast({ title: res.msg || '开奖失败', icon: 'none' });
          }
        }).catch(function() { wx.hideLoading(); });
      }
    });
  },

  handlePublish() {
    var that = this;
    wx.showModal({
      title: '确认发布',
      content: '发布后用户即可参与。',
      success: function(mr) {
        if (!mr.confirm) return;
        wx.showLoading({ title: '发布中...', mask: true });
        drawApi.publishDraw(that.data.drawId).then(function(res) {
          wx.hideLoading();
          if (res.code === 0) {
            wx.showToast({ title: '发布成功', icon: 'success' });
            that.loadDrawDetail();
          } else {
            wx.showToast({ title: res.msg || '失败', icon: 'none' });
          }
        }).catch(function() { wx.hideLoading(); });
      }
    });
  },

  handleDelete() {
    var that = this;
    wx.showModal({
      title: '确认删除',
      content: '删除后不可恢复。',
      success: function(mr) {
        if (!mr.confirm) return;
        wx.showLoading({ title: '删除中...', mask: true });
        drawApi.deleteDraw(that.data.drawId).then(function(res) {
          wx.hideLoading();
          if (res.code === 0) {
            wx.showToast({ title: '已删除', icon: 'success' });
            setTimeout(function() { wx.navigateBack(); }, 1200);
          } else {
            wx.showToast({ title: res.msg || '失败', icon: 'none' });
          }
        }).catch(function() { wx.hideLoading(); });
      }
    });
  },

  // ====================== 口令 ======================

  handleQueryPassCode() {
    var that = this;
    wx.showLoading({ title: '查询中...' });
    drawApi.queryPassCode(that.data.drawId).then(function(res) {
      wx.hideLoading();
      if (res.code === 0 && res.data) {
        that.setPassCodeInfo(res.data);
      }
      that.setData({ showPassCodeModal: true });
    }).catch(function() {
      wx.hideLoading();
      that.setData({ showPassCodeModal: true });
    });
  },

  onPassCodeExpireInput(e) {
    this.setData({ passCodeExpireHours: parseInt(e.detail.value) || 24 });
  },

  setPassCodeInfo(info) {
    this.setData({
      passCodeInfo: info,
      passCodeExpireText: this.buildExpireText(info)
    });
  },

  buildExpireText(info) {
    if (!info || !info.remainValidSecond) return '未知';
    if (info.remainValidSecond > 3600) {
      return (info.remainValidSecond / 3600).toFixed(1) + ' 小时';
    }
    return Math.ceil(info.remainValidSecond / 60) + ' 分钟';
  },

  handleGeneratePassCode() {
    if (this.data.generating) return;
    var that = this;
    this.setData({ generating: true });
    drawApi.generatePassCode({
      drawId: parseInt(that.data.drawId),
      expireHours: that.data.passCodeExpireHours
    }).then(function(res) {
      if (res.code === 0) {
        wx.showToast({ title: '生成成功', icon: 'success' });
        drawApi.queryPassCode(that.data.drawId).then(function(pcRes) {
          if (pcRes.code === 0 && pcRes.data) that.setPassCodeInfo(pcRes.data);
        });
      } else {
        wx.showToast({ title: res.msg || '失败', icon: 'none' });
      }
      that.setData({ generating: false });
    }).catch(function() {
      wx.showToast({ title: '生成失败', icon: 'none' });
      that.setData({ generating: false });
    });
  },

  handleBanPassCode() {
    if (!(this.data.passCodeInfo && this.data.passCodeInfo.passCode)) {
      wx.showToast({ title: '暂无口令', icon: 'none' });
      return;
    }
    var that = this;
    wx.showModal({
      title: '确认禁用',
      content: '口令将立即失效。',
      success: function(mr) {
        if (!mr.confirm) return;
        drawApi.banPassCode(that.data.passCodeInfo.passCode).then(function(res) {
          if (res.code === 0) {
            wx.showToast({ title: '已禁用', icon: 'success' });
            that.setPassCodeInfo(null);
            that.setData({ showPassCodeModal: false });
          } else {
            wx.showToast({ title: res.msg || '失败', icon: 'none' });
          }
        });
      }
    });
  },

  closePassCodeModal() {
    this.setData({ showPassCodeModal: false });
  },

  // 图片兜底
  onCoverError() {
    this.setData({ 'drawInfo.drawCover': '/images/default-draw.png' });
  },

  onPrizeImgError(e) {
    var i = e.currentTarget.dataset.index;
    if (this.data.isEditing) {
      this.setData({ ['editForm.prizes[' + i + '].prizeCover']: '/images/default-draw.png' });
    } else {
      this.setData({ ['prizes[' + i + '].prizeCover']: '/images/default-draw.png' });
    }
  },

  // 分享
  onShareAppMessage() {
    var info = this.data.drawInfo;
    return {
      title: info ? info.title : '抽签详情',
      path: '/pages/drawDetail/drawDetail?drawId=' + this.data.drawId
    };
  }
});
