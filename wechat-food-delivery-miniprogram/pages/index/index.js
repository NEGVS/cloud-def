// pages/index/index.js
const api = require('../../utils/api')
const app = getApp()

Page({
  data: {
    products: [],
    loading: true,
    cartTotalQuantity: 0,
    cartTotalAmount: 0
  },

  onLoad() {
    this.loadProducts()
    this.updateCartInfo()
    
    // 设置购物车更新回调
    app.cartUpdateCallback = () => {
      this.updateCartInfo()
      this.updateProductCartQuantity()
    }
  },

  onShow() {
    this.updateCartInfo()
    this.updateProductCartQuantity()
  },

  // 加载商品列表
  async loadProducts() {
    try {
      this.setData({ loading: true })
      
      const response = await api.getProducts()
      if (response.success) {
        const products = response.data.map(product => ({
          ...product,
          cartQuantity: this.getProductCartQuantity(product.id)
        }))
        
        this.setData({
          products: products,
          loading: false
        })
      } else {
        throw new Error(response.message || '获取商品列表失败')
      }
    } catch (error) {
      console.error('加载商品失败:', error)
      this.setData({ loading: false })
      wx.showToast({
        title: '加载失败',
        icon: 'error'
      })
    }
  },

  // 获取商品在购物车中的数量
  getProductCartQuantity(productId) {
    const cartItem = app.globalData.cart.find(item => item.id === productId)
    return cartItem ? cartItem.quantity : 0
  },

  // 更新商品的购物车数量显示
  updateProductCartQuantity() {
    const products = this.data.products.map(product => ({
      ...product,
      cartQuantity: this.getProductCartQuantity(product.id)
    }))
    
    this.setData({ products })
  },

  // 更新购物车信息
  updateCartInfo() {
    this.setData({
      cartTotalQuantity: app.getCartTotalQuantity(),
      cartTotalAmount: app.getCartTotalAmount().toFixed(2)
    })
  },

  // 添加到购物车
  addToCart(e) {
    const productId = e.currentTarget.dataset.id
    const product = this.data.products.find(p => p.id === productId)
    
    if (product) {
      app.addToCart(product, 1)
      
      wx.showToast({
        title: '已添加到购物车',
        icon: 'success',
        duration: 1000
      })
    }
  },

  // 增加数量
  increaseQuantity(e) {
    const productId = e.currentTarget.dataset.id
    const product = this.data.products.find(p => p.id === productId)
    
    if (product) {
      app.addToCart(product, 1)
    }
  },

  // 减少数量
  decreaseQuantity(e) {
    const productId = e.currentTarget.dataset.id
    const currentQuantity = this.getProductCartQuantity(productId)
    
    if (currentQuantity > 0) {
      app.updateCartItem(productId, currentQuantity - 1)
    }
  },

  // 跳转到购物车页面
  goToCart() {
    wx.switchTab({
      url: '/pages/cart/cart'
    })
  }
})

