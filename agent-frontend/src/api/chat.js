/**
 * 获取饮食规划 AI 的 SSE 地址
 * @param {string} message - 消息内容
 * @param {string} chatId - 聊天室 ID
 */
export function getDietPlanSSEUrl(message, chatId) {
  const baseUrl = 'http://localhost:8123/api'
  const params = new URLSearchParams({
    message: message,
    chatId: chatId
  })
  return `${baseUrl}/diet_plan_agent?${params.toString()}`
}

/**
 * 获取医疗问答 AI 的 SSE 地址
 * @param {string} message - 消息内容
 * @param {string} chatId - 聊天室 ID
 */
export function getMedicalChatSSEUrl(message, chatId) {
  const baseUrl = 'http://localhost:8123/api'
  const params = new URLSearchParams({
    message: message,
    chatId: chatId
  })
  return `${baseUrl}/medical_manage_agent?${params.toString()}`
}

