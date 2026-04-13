/**
 * 创建 SSE 连接
 * @param {string} url - SSE 接口地址
 * @param {function} onMessage - 接收消息的回调函数
 * @param {function} onError - 错误处理回调函数
 * @param {function} onComplete - 完成回调函数
 */
export function createSSEConnection(url, onMessage, onError, onComplete) {
  const eventSource = new EventSource(url)
  
  eventSource.onmessage = (event) => {
    if (event.data) {
      onMessage && onMessage(event.data)
    }
  }
  
  eventSource.onerror = (error) => {
    console.error('SSE 连接错误:', error)
    eventSource.close()
    onError && onError(error)
  }
  
  // 监听完成事件（某些实现可能需要自定义事件）
  eventSource.addEventListener('complete', () => {
    eventSource.close()
    onComplete && onComplete()
  })
  
  // 返回关闭函数
  return () => {
    eventSource.close()
  }
}

/**
 * 使用 fetch 方式处理 SSE（更灵活）
 * @param {string} url - SSE 接口地址
 * @param {function} onMessage - 接收消息的回调函数
 * @param {function} onError - 错误处理回调函数
 * @param {function} onComplete - 完成回调函数
 */
export async function fetchSSE(url, onMessage, onError, onComplete) {
  let controller = new AbortController()
  
  try {
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include', // 携带 cookie
      signal: controller.signal
    })
    
    if (!response.ok) {
      throw new Error(`HTTP error! status: ${response.status}`)
    }
    
    const reader = response.body.getReader()
    const decoder = new TextDecoder()
    let buffer = '' // 用于存储不完整的数据
    
    while (true) {
      const { done, value } = await reader.read()
      
      if (done) {
        // 处理缓冲区剩余数据
        if (buffer.trim()) {
          processSSEMessage(buffer, onMessage)
        }
        onComplete && onComplete()
        break
      }
      
      // 解码并添加到缓冲区
      buffer += decoder.decode(value, { stream: true })
      
      // SSE 使用双换行符(\n\n)来分隔完整的消息
      const messages = buffer.split('\n\n')
      
      // 保留最后一个可能不完整的消息在缓冲区
      buffer = messages.pop() || ''
      
      // 处理完整的消息
      for (const message of messages) {
        if (message.trim()) {
          processSSEMessage(message, onMessage)
        }
      }
    }
  } catch (error) {
    console.error('SSE 连接错误:', error)
    onError && onError(error)
  }
  
  // 返回取消函数
  return () => {
    controller.abort()
  }
}

/**
 * 处理单个 SSE 消息
 * @param {string} message - SSE 消息内容
 * @param {function} onMessage - 消息回调
 */
function processSSEMessage(message, onMessage) {
  const lines = message.split('\n')
  let data = ''
  
  for (const line of lines) {
    if (line.startsWith('data:')) {
      // 提取 data: 后的内容，保留前导空格但去掉 'data:' 后的第一个空格
      const content = line.substring(5)
      // 如果 data: 后有空格，去掉第一个空格
      data += (content.startsWith(' ') ? content.substring(1) : content) + '\n'
    } else if (line.startsWith(':')) {
      // 忽略注释
      continue
    } else if (line.trim()) {
      // 处理不带 data: 前缀的消息（直接内容）
      data += line
    }
  }
  
  // 去掉末尾添加的额外换行符
  if (data.endsWith('\n')) {
    data = data.substring(0, data.length - 1)
  }
  
  if (data) {
    onMessage && onMessage(data)
  }
}

