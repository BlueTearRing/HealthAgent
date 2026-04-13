import request from '@/utils/request'

/**
 * 创建新会话
 * @param {number} userId - 用户ID
 * @param {object} data - 会话信息 { aiType, title, description }
 */
export function createConversation(userId, data) {
  return request({
    url: `/conversation/create/${userId}`,
    method: 'post',
    data,
    skipErrorMessage: true
  })
}


/**
 * 根据AI类型获取用户会话
 * @param {number} userId - 用户ID
 * @param {string} aiType - AI类型（diet_plan/medical_management）
 */
export function getUserConversationsByAiType(userId, aiType) {
  return request({
    url: `/conversation/list/${userId}/${aiType}`,
    method: 'get',
    skipErrorMessage: true
  })
}


/**
 * 删除会话
 * @param {number} userId - 用户ID
 * @param {string} conversationId - 会话ID
 */
export function deleteConversation(userId, conversationId) {
  return request({
    url: `/conversation/${userId}/${conversationId}`,
    method: 'delete',
    skipErrorMessage: true
  })
}

