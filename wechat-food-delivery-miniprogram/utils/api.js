// utils/api.js
const app = getApp()

// 封装请求函数
function request(url, options = {}) {
  return new Promise((resolve, reject) => {
    wx.request({
      url: `${app.globalData.baseUrl}${url}`,
      method: options.method || 'GET',
      data: options.data || {},
      header: {
        'Content-Type': 'application/json',
        ...options.header
      },
      success: (res) => {
        if (res.statusCode === 200) {
          resolve(res.data)
        } else {
          reject(new Error(`请求失败: ${res.statusCode}`))
        }
      },
      fail: (err) => {
        reject(err)
      }
    })
  })
}

// API接口
const api = {
  // 获取商品列表
  getProducts() {
    return request('/products')
  },

  // 获取单个商品详情
  getProduct(id) {
    return request(`/products/${id}`)
  },

  // 获取购物车
  getCart() {
    return request('/cart')
  },

  // 添加到购物车
  addToCart(productId, quantity = 1) {
    return request('/cart/add', {
      method: 'POST',
      data: {
        product_id: productId,
        quantity: quantity
      }
    })
  },

  // 更新购物车
  updateCart(productId, quantity) {
    return request('/cart/update', {
      method: 'POST',
      data: {
        product_id: productId,
        quantity: quantity
      }
    })
  },

  // 清空购物车
  clearCart() {
    return request('/cart/clear', {
      method: 'POST'
    })
  },

  // 提交订单
  createOrder(items) {
    return request('/orders', {
      method: 'POST',
      data: {
        items: items
      }
    })
  },

  // 获取订单列表
  getOrders() {
    return request('/orders')
  },

  // 获取订单详情
  getOrder(id) {
    return request(`/orders/${id}`)
  }
}

module.exports = api

