// pages/order-detail/order-detail.js
const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    order: null,
    loading: true,
    error: null
  },

  onLoad(options) {
    const orderId = options.id
    if (orderId) {
      this.loadOrderDetail(orderId)
    } else {
      this.setData({
        loading: false,
        error: '订单ID不存在'
      })
    }
  },

  // 加载订单详情
  async loadOrderDetail(orderId) {
    try {
      this.setData({ 
        loading: true,
        error: null
      })
      
      const response = await api.getOrder(orderId)
      
      if (response.success) {
        const order = {
          ...response.data,
          order_time_formatted: this.formatDateTime(response.data.order_time),
          items: response.data.items.map(item => ({
            ...item,
            item_total: (item.price * item.quantity).toFixed(2)
          }))
        }
        
        this.setData({
          order: order,
          loading: false
        })
      } else {
        throw new Error(response.message || '获取订单详情失败')
      }
    } catch (error) {
      console.error('加载订单详情失败:', error)
      this.setData({
        loading: false,
        error: '加载失败，请重试'
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

  // 重试加载
  retry() {
    const pages = getCurrentPages()
    const currentPage = pages[pages.length - 1]
    const orderId = currentPage.options.id
    
    if (orderId) {
      this.loadOrderDetail(orderId)
    }
  },

  // 返回上一页
  goBack() {
    wx.navigateBack()
  },

  // 再来一单
  reorder() {
    if (!this.data.order || !this.data.order.items) {
      wx.showToast({
        title: '订单信息不完整',
        icon: 'error'
      })
      return
    }

    // 将订单中的商品添加到购物车
    this.data.order.items.forEach(item => {
      const product = {
        id: item.product_id,
        name: item.product_name,
        price: item.price,
        description: '',
        image_url: ''
      }
      
      app.addToCart(product, item.quantity)
    })

    wx.showToast({
      title: '已添加到购物车',
      icon: 'success'
    })

    // 跳转到购物车页面
    setTimeout(() => {
      wx.switchTab({
        url: '/pages/cart/cart'
      })
    }, 1500)
  }
})

