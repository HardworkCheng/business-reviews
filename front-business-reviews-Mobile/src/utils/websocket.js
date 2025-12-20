/**
 * WebSocket管理器
 * 用于实时消息推送
 * 只在聊天页面使用，避免全局连接
 */

class WebSocketManager {
  constructor() {
    this.ws = null
    this.reconnectTimer = null
    this.heartbeatTimer = null
    this.messageHandlers = []
    this.isConnecting = false
    this.userId = null
    this.token = null
    this.reconnectAttempts = 0
    this.maxReconnectAttempts = 3 // 最大重连次数
    this.enabled = false // 是否启用WebSocket（只在聊天页面启用）
    this.debugMode = false // 调试模式，控制日志输出
  }
  
  /**
   * 启用WebSocket（进入聊天页面时调用）
   */
  enable() {
    this.enabled = true
    this.reconnectAttempts = 0
  }
  
  /**
   * 禁用WebSocket（离开聊天页面时调用）
   */
  disable() {
    this.enabled = false
    this.disconnect()
  }
  
  /**
   * 连接WebSocket
   * @param {Number} userId - 用户ID
   * @param {String} token - 认证token
   */
  connect(userId, token) {
    // 如果未启用，不进行连接
    if (!this.enabled) {
      if (this.debugMode) console.log('WebSocket未启用，跳过连接')
      return
    }
    
    // 验证参数，防止undefined值
    if (!userId || !token) {
      if (this.debugMode) console.warn('WebSocket连接失败: userId或token为空')
      return
    }
    
    // 保存用户信息用于重连
    this.userId = userId
    this.token = token
    
    // #ifdef H5
    if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
      if (this.debugMode) console.log('WebSocket已连接或正在连接中')
      return
    }
    // #endif
    
    // #ifndef H5
    if (this.isConnecting) {
      if (this.debugMode) console.log('WebSocket正在连接中')
      return
    }
    // #endif
    
    this.isConnecting = true
    
    // 根据环境构建WebSocket URL
    let wsUrl
    // #ifdef H5
    // H5环境下，使用当前页面的host来构建WebSocket URL
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    const host = window.location.host
    // 如果是开发环境（通过vite代理），直接连接后端
    if (host.includes('localhost') || host.includes('127.0.0.1')) {
      wsUrl = `ws://localhost:8080/api/ws?userId=${userId}&token=${encodeURIComponent(token)}`
    } else {
      // 生产环境或其他情况
      wsUrl = `${protocol}//${host}/api/ws?userId=${userId}&token=${encodeURIComponent(token)}`
    }
    // #endif
    
    // #ifndef H5
    // 非H5环境（小程序、App）
    wsUrl = `ws://localhost:8080/api/ws?userId=${userId}&token=${encodeURIComponent(token)}`
    // #endif
    
    if (this.debugMode) console.log('正在连接WebSocket:', wsUrl)
    
    // #ifdef H5
    try {
      this.ws = new WebSocket(wsUrl)
      
      this.ws.onopen = () => {
        if (this.debugMode) console.log('WebSocket连接成功')
        this.isConnecting = false
        this.reconnectAttempts = 0 // 连接成功，重置重连次数
        this.startHeartbeat()
      }
      
      this.ws.onmessage = (event) => {
        try {
          const message = JSON.parse(event.data)
          if (this.debugMode) console.log('收到WebSocket消息:', message)
          this.messageHandlers.forEach(handler => handler(message))
        } catch (e) {
          if (this.debugMode) console.error('解析WebSocket消息失败:', e)
        }
      }
      
      this.ws.onerror = () => {
        // 静默处理错误，不输出到控制台
        this.isConnecting = false
      }
      
      this.ws.onclose = () => {
        this.isConnecting = false
        this.stopHeartbeat()
        // 只有在启用状态下才尝试重连
        if (this.enabled) {
          this.reconnect()
        }
      }
    } catch (e) {
      this.isConnecting = false
      if (this.debugMode) console.error('WebSocket创建失败:', e)
    }
    // #endif
    
    // #ifndef H5
    uni.connectSocket({
      url: wsUrl,
      success: () => {
        if (this.debugMode) console.log('WebSocket连接请求已发送')
      },
      fail: (err) => {
        if (this.debugMode) console.error('WebSocket连接失败:', err)
        this.isConnecting = false
      }
    })
    
    uni.onSocketOpen(() => {
      if (this.debugMode) console.log('WebSocket连接成功')
      this.isConnecting = false
      this.reconnectAttempts = 0
      this.startHeartbeat()
    })
    
    uni.onSocketMessage((res) => {
      try {
        const message = JSON.parse(res.data)
        if (this.debugMode) console.log('收到WebSocket消息:', message)
        this.messageHandlers.forEach(handler => handler(message))
      } catch (e) {
        if (this.debugMode) console.error('解析WebSocket消息失败:', e)
      }
    })
    
    uni.onSocketError(() => {
      // 静默处理错误
      this.isConnecting = false
    })
    
    uni.onSocketClose(() => {
      this.isConnecting = false
      this.stopHeartbeat()
      if (this.enabled) {
        this.reconnect()
      }
    })
    // #endif
  }
  
  /**
   * 发送消息
   * @param {Object} message - 消息对象
   */
  send(message) {
    const data = JSON.stringify(message)
    
    // #ifdef H5
    if (this.ws && this.ws.readyState === WebSocket.OPEN) {
      this.ws.send(data)
    } else {
      console.warn('WebSocket未连接，无法发送消息')
    }
    // #endif
    
    // #ifndef H5
    uni.sendSocketMessage({
      data: data,
      fail: (err) => {
        console.warn('WebSocket发送消息失败:', err)
      }
    })
    // #endif
  }
  
  /**
   * 注册消息处理器
   * @param {Function} handler - 消息处理函数
   */
  onMessage(handler) {
    this.messageHandlers.push(handler)
  }
  
  /**
   * 移除消息处理器
   * @param {Function} handler - 消息处理函数
   */
  offMessage(handler) {
    const index = this.messageHandlers.indexOf(handler)
    if (index > -1) {
      this.messageHandlers.splice(index, 1)
    }
  }
  
  /**
   * 开始心跳
   */
  startHeartbeat() {
    this.heartbeatTimer = setInterval(() => {
      this.send({ type: 'heartbeat', timestamp: Date.now() })
    }, 30000) // 每30秒发送一次心跳
  }
  
  /**
   * 停止心跳
   */
  stopHeartbeat() {
    if (this.heartbeatTimer) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }
  
  /**
   * 重新连接
   */
  reconnect() {
    if (this.reconnectTimer) return
    
    // 如果未启用，不进行重连
    if (!this.enabled) {
      if (this.debugMode) console.log('WebSocket未启用，不进行重连')
      return
    }
    
    // 如果没有保存的用户信息，不进行重连
    if (!this.userId || !this.token) {
      if (this.debugMode) console.log('没有用户信息，不进行重连')
      return
    }
    
    // 检查重连次数
    if (this.reconnectAttempts >= this.maxReconnectAttempts) {
      if (this.debugMode) console.log('WebSocket重连次数已达上限，停止重连')
      return
    }
    
    this.reconnectAttempts++
    
    this.reconnectTimer = setTimeout(() => {
      if (this.debugMode) console.log(`尝试重新连接WebSocket (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)
      this.connect(this.userId, this.token)
      this.reconnectTimer = null
    }, 5000) // 5秒后重连
  }
  
  /**
   * 断开连接
   */
  disconnect() {
    // #ifdef H5
    if (this.ws) {
      this.ws.close()
      this.ws = null
    }
    // #endif
    
    // #ifndef H5
    uni.closeSocket()
    // #endif
    
    this.stopHeartbeat()
    if (this.reconnectTimer) {
      clearTimeout(this.reconnectTimer)
      this.reconnectTimer = null
    }
    this.messageHandlers = []
    this.isConnecting = false
    this.userId = null
    this.token = null
    this.reconnectAttempts = 0
  }
  
  /**
   * 检查是否已连接
   */
  isConnected() {
    // #ifdef H5
    return this.ws && this.ws.readyState === WebSocket.OPEN
    // #endif
    
    // #ifndef H5
    return false // uni-app需要通过其他方式检查
    // #endif
  }
}

export default new WebSocketManager()
