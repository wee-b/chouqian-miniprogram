// pages/publish/publish.js
import drawApi from '../../utils/apis/draw.js';
import auth from '../../utils/auth.js';

var TYPE_NAMES = ['', '一等奖', '二等奖', '三等奖', '四等奖', '五等奖', '六等奖'];
var GIVEAWAY_NAMES = ['', '快递邮寄', '联系发布者', '填写信息', '其他'];

Page({
  data: {
    editMode: false,
    drawId: null,
    loading: false,

    // 表单
    title: '',
    drawCover: '',
    description: '',
    hasPrize: 0,
    drawingWay: 0,
    deadlineDate: '',
    deadlineTime: '',
    minPerson: '',
    perCodeNum: 5,

    // 奖品
    prizes: [],
    addingPrize: false,
    editingPrizeIndex: -1,
    prizeForm: { prizeName: '', prizeCover: '', prizeType: 1, amount: 1, giveaway: 4 },

    // 奖品类型/发放方式显示名
    prizeTypeName: '一等奖',
    prizeGiveawayName: '其他',

    submitting: false
  },

  onLoad: function(options) {
    var drawId = options.drawId;
    if (drawId) {
      this.setData({ editMode: true, drawId: drawId });
      this.loadDrawData(drawId);
    }
  },

  onShow: function() {
    var app = getApp();
    var editDrawId = app.globalData.editDrawId;
    if (editDrawId) {
      app.globalData.editDrawId = null;
      this.setData({ editMode: true, drawId: editDrawId });
      this.resetForm();
      this.loadDrawData(editDrawId);
      return;
    }
    if (!this.data.editMode && !auth.isLogin()) {
      wx.navigateTo({ url: '/pages/login/login' });
    }
  },

  resetForm: function() {
    this.setData({
      title: '',
      drawCover: '',
      description: '',
      hasPrize: 0,
      drawingWay: 0,
      deadlineDate: '',
      deadlineTime: '',
      minPerson: '',
      perCodeNum: 5,
      prizes: []
    });
  },

  loadDrawData: function(drawId) {
    var that = this;
    this.setData({ loading: true });

    Promise.all([
      drawApi.getDetailDraw(drawId),
      drawApi.getPrizesByDrawId(drawId)
    ]).then(function(arr) {
      var drawRes = arr[0];
      var prizeRes = arr[1];

      if (drawRes.code === 0 && drawRes.data) {
        var info = drawRes.data;
        var dd = that.parseDeadline(info.joinDeadline);

        that.setData({
          title: info.title || '',
          drawCover: info.drawCover || '',
          description: info.description || '',
          hasPrize: (info.hasPrize != null) ? info.hasPrize : 0,
          drawingWay: (info.drawingWay != null) ? info.drawingWay : 0,
          deadlineDate: dd.date,
          deadlineTime: dd.time,
          minPerson: info.minPerson ? String(info.minPerson) : '',
          perCodeNum: info.perCodeNum || 5
        });
      }

      if (prizeRes.code === 0) {
        that.setData({ prizes: prizeRes.data || [] });
      }
    }).catch(function(e) {
      console.error('加载失败', e);
      wx.showToast({ title: '加载失败', icon: 'none' });
    }).finally(function() {
      that.setData({ loading: false });
    });
  },

  parseDeadline: function(d) {
    if (!d) return { date: '', time: '' };
    var s = '';
    if (Array.isArray(d)) {
      var a = d;
      var pad = function(n) { return n < 10 ? '0' + n : '' + n; };
      s = a[0] + '-' + pad(a[1]) + '-' + pad(a[2]) + 'T' + pad(a[3]) + ':' + pad(a[4]);
    } else {
      s = String(d);
    }
    var parts = s.split('T');
    return {
      date: parts[0] || '',
      time: (parts[1] || '').substring(0, 5)
    };
  },

  // ===== 表单输入 =====
  onTitleInput: function(e) { this.setData({ title: e.detail.value }); },
  onDescInput: function(e) { this.setData({ description: e.detail.value }); },

  // 选择封面图
  onChooseCover: function() {
    var that = this;
    wx.chooseMedia({
      count: 1,
      mediaType: ['image'],
      sourceType: ['album', 'camera'],
      success: function(res) {
        var path = res.tempFiles[0].tempFilePath;
        that.setData({ drawCover: path });
      }
    });
  },

  // 选择奖品封面图
  onChoosePrizeCover: function() {
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
  onMinPersonInput: function(e) { this.setData({ minPerson: e.detail.value }); },
  onPerCodeInput: function(e) { this.setData({ perCodeNum: parseInt(e.detail.value) || 5 }); },
  onPerCodeStep: function(e) {
    var d = parseInt(e.currentTarget.dataset.delta);
    var v = (this.data.perCodeNum || 5) + d;
    if (v < 1) v = 1;
    if (v > 50) v = 50;
    this.setData({ perCodeNum: v });
  },

  onHasPrizeChange: function(e) {
    var val = parseInt(e.currentTarget.dataset.value);
    this.setData({ hasPrize: val });
    if (val === 0) this.setData({ prizes: [] });
  },

  onDrawingWayChange: function(e) {
    this.setData({ drawingWay: parseInt(e.currentTarget.dataset.value) });
  },

  onDeadlineDateChange: function(e) {
    this.setData({ deadlineDate: e.detail.value });
  },

  onDeadlineTimeChange: function(e) {
    this.setData({ deadlineTime: e.detail.value });
  },

  // ===== 奖品 =====
  showAddPrize: function() {
    this.setData({
      addingPrize: true,
      editingPrizeIndex: -1,
      prizeForm: { prizeName: '', prizeCover: '', prizeType: 1, amount: 1, giveaway: 4 },
      prizeTypeName: '一等奖',
      prizeGiveawayName: '其他'
    });
  },

  hidePrizeForm: function() {
    this.setData({ addingPrize: false, editingPrizeIndex: -1 });
  },

  onPrizeNameInput: function(e) { this.setData({ 'prizeForm.prizeName': e.detail.value }); },
  onPrizeCoverInput: function(e) { this.setData({ 'prizeForm.prizeCover': e.detail.value }); },
  onPrizeAmountInput: function(e) { this.setData({ 'prizeForm.amount': parseInt(e.detail.value) || 1 }); },

  onPrizeTypePrev: function() {
    var t = this.data.prizeForm.prizeType;
    if (t > 1) {
      t--;
      this.setData({ 'prizeForm.prizeType': t, prizeTypeName: TYPE_NAMES[t] });
    }
  },

  onPrizeTypeNext: function() {
    var t = this.data.prizeForm.prizeType;
    if (t < 6) {
      t++;
      this.setData({ 'prizeForm.prizeType': t, prizeTypeName: TYPE_NAMES[t] });
    }
  },

  onGiveawayPrev: function() {
    var g = this.data.prizeForm.giveaway;
    if (g > 1) {
      g--;
      this.setData({ 'prizeForm.giveaway': g, prizeGiveawayName: GIVEAWAY_NAMES[g] });
    }
  },

  onGiveawayNext: function() {
    var g = this.data.prizeForm.giveaway;
    if (g < 4) {
      g++;
      this.setData({ 'prizeForm.giveaway': g, prizeGiveawayName: GIVEAWAY_NAMES[g] });
    }
  },

  savePrize: function() {
    var form = this.data.prizeForm;
    if (!form.prizeName.trim()) {
      wx.showToast({ title: '请输入奖品名称', icon: 'none' });
      return;
    }
    var prizes = this.data.prizes.slice();
    if (this.data.editingPrizeIndex >= 0) {
      prizes[this.data.editingPrizeIndex] = {
        prizeName: form.prizeName,
        prizeCover: form.prizeCover,
        prizeType: form.prizeType,
        amount: form.amount,
        giveaway: form.giveaway
      };
    } else {
      prizes.push({
        prizeName: form.prizeName,
        prizeCover: form.prizeCover,
        prizeType: form.prizeType,
        amount: form.amount,
        giveaway: form.giveaway
      });
    }
    this.setData({ prizes: prizes, addingPrize: false, editingPrizeIndex: -1 });
  },

  editPrize: function(e) {
    var i = e.currentTarget.dataset.index;
    var p = this.data.prizes[i];
    this.setData({
      addingPrize: true,
      editingPrizeIndex: i,
      prizeForm: {
        prizeName: p.prizeName,
        prizeCover: p.prizeCover,
        prizeType: p.prizeType || 1,
        amount: p.amount || 1,
        giveaway: p.giveaway || 4
      },
      prizeTypeName: TYPE_NAMES[p.prizeType] || '一等奖',
      prizeGiveawayName: GIVEAWAY_NAMES[p.giveaway] || '其他'
    });
  },

  removePrize: function(e) {
    var i = e.currentTarget.dataset.index;
    var prizes = this.data.prizes.slice();
    prizes.splice(i, 1);
    this.setData({ prizes: prizes });
  },

  syncPrizes: function(drawId) {
    if (this.data.hasPrize === 0 || this.data.prizes.length === 0) return;
    var prizes = this.data.prizes.map(function(p) {
      return {
        drawId: parseInt(drawId),
        prizeName: p.prizeName,
        prizeCover: p.prizeCover,
        prizeType: p.prizeType || 1,
        amount: p.amount || 1,
        giveaway: p.giveaway || 4
      };
    });
    return drawApi.batchAddPrize(prizes).catch(function(e) {
      console.error('同步奖品失败', e);
    });
  },

  // ===== 提交 =====
  handleSubmitDraft: function() {
    this.doSubmit({ status: 0 }, '保存草稿');
  },

  handleSubmitPublish: function() {
    this.doSubmit({ status: 1 }, '保存并发布');
  },

  handleSubmit: function() {
    this.doSubmit({}, this.data.editMode ? '修改成功' : '创建成功');
  },

  doSubmit: function(extraParams, successMsg) {
    var that = this;
    if (this.data.submitting) return;

    if (!this.data.title.trim()) {
      wx.showToast({ title: '请输入抽签标题', icon: 'none' });
      return;
    }
    if (!this.data.deadlineDate) {
      wx.showToast({ title: '请选择截止日期', icon: 'none' });
      return;
    }

    var joinDeadline = this.data.deadlineDate + 'T' + (this.data.deadlineTime || '23:59') + ':00';

    var params = Object.assign({
      title: this.data.title.trim(),
      drawCover: this.data.drawCover.trim(),
      description: this.data.description.trim(),
      hasPrize: this.data.hasPrize,
      drawingWay: this.data.drawingWay,
      joinDeadline: joinDeadline,
      minPerson: this.data.minPerson ? parseInt(this.data.minPerson) : 0,
      perCodeNum: this.data.perCodeNum || 5
    }, extraParams);

    if (this.data.editMode) {
      params.drawId = parseInt(this.data.drawId);
    }

    this.setData({ submitting: true });
    wx.showLoading({ title: '提交中...', mask: true });

    var api = this.data.editMode ? drawApi.updateDraw : drawApi.createDraw;
    api(params).then(function(res) {
      wx.hideLoading();
      if (res.code === 0) {
        var savedDrawId = (res.data && res.data.drawId) || that.data.drawId;
        var p = Promise.resolve();
        if (that.data.hasPrize === 1 && savedDrawId) {
          p = that.syncPrizes(savedDrawId);
        }
        p.then(function() {
          wx.showToast({ title: successMsg, icon: 'success' });
          setTimeout(function() { wx.navigateBack(); }, 1500);
        });
      } else {
        wx.showToast({ title: res.msg || '提交失败', icon: 'none' });
      }
      that.setData({ submitting: false });
    }).catch(function() {
      wx.hideLoading();
      that.setData({ submitting: false });
    });
  }
});
