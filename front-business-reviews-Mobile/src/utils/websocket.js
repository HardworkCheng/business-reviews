/**
 * WebSocket管理器
 * 用于实时消息推送
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
  }
  
  /**
   * 连接WebSocket
   * @param {Number} userId - 用户ID
   * @param {String} token - 认证token
   */
  connect(userId, token) {
    // 验证参数，防止undefined值
    if (!userId || !token) {
      console.warn('WebSocket连接失败: userId或token为空', { userId, token: token ? '存在' : '不存在' })
      return
    }
    
    // 保存用户信息用于重连
    this.userId = userId
    this.token = token
    
    // #ifdef H5
    if (this.isConnecting || (this.ws && this.ws.readyState === WebSocket.OPEN)) {
      console.log('WebSocket已连接或正在连接中')
      return
    }
    // #endif
    
    // #ifndef H5
    if (this.isConnecting) {
      console.log('WebSocket正在连接中')
      return
    }
    // #endif
    
    this.isConnecting = true
    
    // WebSocket端点需要包含context-path前缀 /api
    const wsUrl = `ws://localhost:8080/api/ws?userId=${userId}&token=${encodeURIComponent(token)}`
    
    console.log('正在连接WebSocket:', wsUrl)
    
    // #ifdef H5
    this.ws = new WebSocket(wsUrl)
    
    this.ws.onopen = () => {
      console.log('WebSocket连接成功')
      this.isConnecting = false
      this.startHeartbeat()
    }
    
    this.ws.onmessage = (event) => {
      try {
        const message = JSON.parse(event.data)
        console.log('收到WebSocket消息:', message)
        this.messageHandlers.forEach(handler => handler(message))
      } catch (e) {
        console.error('解析WebSocket消息失败:', e)
      }
    }
    
    this.ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      this.isConnecting = false
    }
    
    this.ws.onclose = () => {
      console.log('WebSocket连接关闭')
      this.isConnecting = false
      this.stopHeartbeat()
      this.reconnect()
    }
    // #endif
    
    // #ifndef H5
    uni.connectSocket({
      url: wsUrl,
      success: () => {
        console.log('WebSocket连接请求已发送')
      },
      fail: (err) => {
        console.error('WebSocket连接失败:', err)
        this.isConnecting = false
      }
    })
    
    uni.onSocketOpen(() => {
      console.log('WebSocket连接成功')
      this.isConnecting = false
      this.startHeartbeat()
    })
    
    uni.onSocketMessage((res) => {
      try {
        const message = JSON.parse(res.data)
        console.log('收到WebSocket消息:', message)
        this.messageHandlers.forEach(handler => handler(message))
      } catch (e) {
        console.error('解析WebSocket消息失败:', e)
      }
    })
    
    uni.onSocketError((error) => {
      console.error('WebSocket错误:', error)
      this.isConnecting = false
    })
    
    uni.onSocketClose(() => {
      console.log('WebSocket连接关闭')
      this.isConnecting = false
      this.stopHeartbeat()
      this.reconnect()
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
    
    // 如果没有保存的用户信息，不进行重连
    if (!this.userId || !this.token) {
      console.log('没有用户信息，不进行重连')
      return
    }
    
    this.reconnectTimer = setTimeout(() => {
      console.log('尝试重新连接WebSocket')
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
  }
}

export default new WebSocketManager()
