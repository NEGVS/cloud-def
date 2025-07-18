// pages/order/order.js
const api = require('../../utils/api')

Page({
  data: {
    orders: [],
    loading: true
  },

  onLoad() {
    this.loadOrders()
  },

  onShow() {
    this.loadOrders()
  },

  // 加载订单列表
  async loadOrders() {
    try {
      this.setData({ loading: true })
      
      const response = await api.getOrders()
      
      if (response.success) {
        const orders = response.data.map(order => ({
          ...order,
          order_time_formatted: this.formatDateTime(order.order_time)
        }))
        
        this.setData({
          orders: orders,
          loading: false
        })
      } else {
        throw new Error(response.message || '获取订单列表失败')
      }
    } catch (error) {
      console.error('加载订单失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'error'
      })
    }
  },

  // 格式化日期时间
  formatDateTime(dateTimeString) {
    const date = new Date(dateTimeString)
    const year = date.getFullYear()
    const month = String(date.getMonth() + 1).padStart(2, '0')
    const day = String(date.getDate()).padStart(2, '0')
    const hours = String(date.getHours()).padStart(2, '0')
    const minutes = String(date.getMinutes()).padStart(2, '0')
    
    return `${year}-${month}-${day} ${hours}:${minutes}`
  },

  // 跳转到订单详情页
  goToOrderDetail(e) {
    const orderId = e.currentTarget.dataset.id
    wx.navigateTo({
      url: `/pages/order-detail/order-detail?id=${orderId}`
    })
  },

  // 跳转到首页
  goToIndex() {
    wx.switchTab({
      url: '/pages/index/index'
    })
  },

  // 下拉刷新
  onPullDownRefresh() {
    this.loadOrders().then(() => {
      wx.stopPullDownRefresh()
    })
  }
})

