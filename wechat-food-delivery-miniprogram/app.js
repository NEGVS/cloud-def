// app.js
App({
  globalData: {
    baseUrl: 'http://localhost:5000/api', // 后端API地址
    cart: [], // 购物车数据
    userInfo: null
  },

  onLaunch() {
    // 展示本地存储能力
    const logs = wx.getStorageSync('logs') || []
    logs.unshift(Date.now())
    wx.setStorageSync('logs', logs)

    // 登录
    wx.login({
      success: res => {
        // 发送 res.code 到后台换取 openId, sessionKey, unionId
        console.log('登录成功', res.code)
      }
    })

    // 获取用户信息
    wx.getSetting({
      success: res => {
        if (res.authSetting['scope.userInfo']) {
          // 已经授权，可以直接调用 getUserInfo 获取头像昵称，不会弹框
          wx.getUserInfo({
            success: res => {
              // 可以将 res 发送给后台解码出 unionId
              this.globalData.userInfo = res.userInfo

              // 由于 getUserInfo 是网络请求，可能会在 Page.onLoad 之后才返回
              // 所以此处加入 callback 以防止这种情况
              if (this.userInfoReadyCallback) {
                this.userInfoReadyCallback(res)
              }
            }
          })
        }
      }
    })

    // 从本地存储加载购物车数据
    this.loadCartFromStorage()
  },

  // 加载购物车数据
  loadCartFromStorage() {
    try {
      const cart = wx.getStorageSync('cart')
      if (cart) {
        this.globalData.cart = cart
      }
    } catch (e) {
      console.error('加载购物车数据失败', e)
    }
  },

  // 保存购物车数据到本地存储
  saveCartToStorage() {
    try {
      wx.setStorageSync('cart', this.globalData.cart)
    } catch (e) {
      console.error('保存购物车数据失败', e)
    }
  },

  // 添加商品到购物车
  addToCart(product, quantity = 1) {
    const existingItem = this.globalData.cart.find(item => item.id === product.id)
    
    if (existingItem) {
      existingItem.quantity += quantity
    } else {
      this.globalData.cart.push({
        ...product,
        quantity: quantity
      })
    }
    
    this.saveCartToStorage()
    
    // 通知页面更新
    if (this.cartUpdateCallback) {
      this.cartUpdateCallback()
    }
  },

  // 更新购物车商品数量
  updateCartItem(productId, quantity) {
    const itemIndex = this.globalData.cart.findIndex(item => item.id === productId)
    
    if (itemIndex !== -1) {
      if (quantity <= 0) {
        this.globalData.cart.splice(itemIndex, 1)
      } else {
        this.globalData.cart[itemIndex].quantity = quantity
      }
      
      this.saveCartToStorage()
      
      // 通知页面更新
      if (this.cartUpdateCallback) {
        this.cartUpdateCallback()
      }
    }
  },

  // 清空购物车
  clearCart() {
    this.globalData.cart = []
    this.saveCartToStorage()
    
    // 通知页面更新
    if (this.cartUpdateCallback) {
      this.cartUpdateCallback()
    }
  },

  // 获取购物车总数量
  getCartTotalQuantity() {
    return this.globalData.cart.reduce((total, item) => total + item.quantity, 0)
  },

  // 获取购物车总金额
  getCartTotalAmount() {
    return this.globalData.cart.reduce((total, item) => total + (item.price * item.quantity), 0)
  }
})

