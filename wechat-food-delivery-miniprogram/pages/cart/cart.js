// pages/cart/cart.js
const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    cartItems: [],
    totalAmount: 0
  },

  onLoad() {
    // 设置购物车更新回调
    app.cartUpdateCallback = () => {
      this.loadCartData()
    }
  },

  onShow() {
    this.loadCartData()
  },

  // 加载购物车数据
  loadCartData() {
    const cart = app.globalData.cart
    const cartItems = cart.map(item => ({
      ...item,
      totalPrice: (item.price * item.quantity).toFixed(2)
    }))
    
    const totalAmount = app.getCartTotalAmount().toFixed(2)
    
    this.setData({
      cartItems: cartItems,
      totalAmount: totalAmount
    })
  },

  // 增加数量
  increaseQuantity(e) {
    const productId = e.currentTarget.dataset.id
    const cartItem = app.globalData.cart.find(item => item.id === productId)
    
    if (cartItem) {
      app.updateCartItem(productId, cartItem.quantity + 1)
    }
  },

  // 减少数量
  decreaseQuantity(e) {
    const productId = e.currentTarget.dataset.id
    const cartItem = app.globalData.cart.find(item => item.id === productId)
    
    if (cartItem && cartItem.quantity > 0) {
      app.updateCartItem(productId, cartItem.quantity - 1)
    }
  },

  // 清空购物车
  clearCart() {
    wx.showModal({
      title: '确认清空',
      content: '确定要清空购物车吗？',
      success: (res) => {
        if (res.confirm) {
          app.clearCart()
          wx.showToast({
            title: '购物车已清空',
            icon: 'success'
          })
        }
      }
    })
  },

  // 去结算
  async checkout() {
    if (this.data.cartItems.length === 0) {
      wx.showToast({
        title: '购物车是空的',
        icon: 'error'
      })
      return
    }

    try {
      wx.showLoading({
        title: '提交订单中...'
      })

      // 准备订单数据
      const items = this.data.cartItems.map(item => ({
        product_id: item.id,
        quantity: item.quantity
      }))

      // 提交订单
      const response = await api.createOrder(items)
      
      wx.hideLoading()

      if (response.success) {
        // 清空购物车
        app.clearCart()
        
        wx.showToast({
          title: '订单提交成功',
          icon: 'success'
        })

        // 跳转到订单详情页
        setTimeout(() => {
          wx.navigateTo({
            url: `/pages/order-detail/order-detail?id=${response.data.id}`
          })
        }, 1500)
      } else {
        throw new Error(response.message || '订单提交失败')
      }
    } catch (error) {
      wx.hideLoading()
      console.error('提交订单失败:', error)
      wx.showToast({
        title: '提交失败',
        icon: 'error'
      })
    }
  },

  // 跳转到首页
  goToIndex() {
    wx.switchTab({
      url: '/pages/index/index'
    })
  }
})

