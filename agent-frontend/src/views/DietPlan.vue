<template>
  <div class="chat-container">
    <!-- 移动端遮罩层 -->
    <div class="mobile-overlay" :class="{ show: showMobileSidebar }" @click="showMobileSidebar = false"></div>
    
    <!-- 左侧聊天历史 -->
    <div class="sidebar" :class="{ 'mobile-open': showMobileSidebar }">
      <div class="sidebar-header">
        <h3>AI 饮食规划</h3>
        <el-button type="primary" :icon="Plus" @click="createNewChat">新建对话</el-button>
      </div>
      <div class="chat-list">
        <div 
          v-for="chat in chatList" 
          :key="chat.conversationId"
          :class="['chat-item', { active: chat.conversationId === currentChatId }]"
        >
          <div class="chat-item-content" @click="switchChat(chat.conversationId)">
            <el-icon><ChatDotRound /></el-icon>
            <span class="chat-title">{{ getChatItemTitle(chat.conversationId) }}</span>
          </div>
          <el-icon 
            class="delete-icon" 
            @click.stop="handleDeleteChat(chat.conversationId)"
          >
            <Delete />
          </el-icon>
        </div>
      </div>
      <div class="sidebar-footer">
        <el-button text @click="goBack">
          <el-icon><ArrowLeft /></el-icon>
          返回主页
        </el-button>
      </div>
    </div>

    <!-- 右侧聊天区域 -->
    <div class="chat-main">
      <div class="chat-header">
        <el-button 
          class="mobile-menu-btn" 
          :icon="Menu" 
          circle 
          @click="showMobileSidebar = true"
        />
        <h2>{{ getCurrentChatTitle() }}</h2>
        <el-button 
          type="primary" 
          :icon="Plus" 
          @click="createNewChat" 
          class="mobile-new-chat-btn"
          circle
        />
      </div>

      <div class="messages-container" ref="messagesContainerRef">
        <div v-if="messages.length === 0" class="empty-state">
          <el-icon :size="60" color="#ccc"><ChatDotRound /></el-icon>
          <p>开始您的 AI 饮食规划咨询</p>
        </div>
        
        <div v-for="(msg, index) in messages" :key="index" :class="['message-item', msg.role]">
          <div class="message-avatar">
            <el-icon v-if="msg.role === 'user'" :size="24"><User /></el-icon>
            <el-icon v-else :size="24"><Avatar /></el-icon>
          </div>
          <div class="message-content">
            <div 
              v-if="msg.role === 'user'" 
              class="message-text"
            >{{ msg.content }}</div>
            <div 
              v-else 
              class="message-text markdown-body" 
              v-html="renderMarkdown(msg.content)"
            ></div>
          </div>
        </div>

      </div>

      <div class="input-container">
        <el-input
          v-model="inputMessage"
          type="textarea"
          :rows="3"
          placeholder="请输入您的饮食问题..."
          @keydown.enter.exact.prevent="sendMessage"
          :disabled="isLoading"
        />
        <el-button 
          type="primary" 
          :icon="Position" 
          @click="sendMessage"
          :loading="isLoading"
          :disabled="!inputMessage.trim()"
        >
          发送
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, ChatDotRound, ArrowLeft, User, Avatar, Position, Delete, Menu } from '@element-plus/icons-vue'
import { getDietPlanSSEUrl } from '@/api/chat'
import { fetchSSE } from '@/utils/sse'
import { renderMarkdown } from '@/utils/markdown'
import { 
  createConversation, 
  getUserConversationsByAiType, 
  deleteConversation 
} from '@/api/conversation'

const router = useRouter()
const messagesContainerRef = ref(null)
const inputMessage = ref('')
const isLoading = ref(false)
const currentChatId = ref('')
const messages = ref([])
const chatList = ref([])
const userInfo = ref(null)
const userId = ref(null)
const showMobileSidebar = ref(false)
let cancelSSE = null

const AI_TYPE = 'diet_plan' // AI类型常量

const getConversationStorageKey = () => `chat_conversations_${userId.value}_${AI_TYPE}`

const loadLocalChatList = () => {
  if (!userId.value) {
    return []
  }
  try {
    const stored = localStorage.getItem(getConversationStorageKey())
    return stored ? JSON.parse(stored) : []
  } catch (e) {
    console.error('加载本地会话失败:', e)
    return []
  }
}

const saveLocalChatList = () => {
  if (!userId.value) {
    return
  }
  try {
    localStorage.setItem(getConversationStorageKey(), JSON.stringify(chatList.value))
  } catch (e) {
    console.error('保存本地会话失败:', e)
  }
}

const buildLocalConversation = () => {
  const randomPart = Math.random().toString(36).slice(2, 10)
  return {
    conversationId: `local-${AI_TYPE}-${Date.now()}-${randomPart}`,
    aiType: AI_TYPE,
    title: `饮食规划 ${chatList.value.length + 1}`,
    description: '新建对话'
  }
}

const applyNewChat = (newChat) => {
  chatList.value.unshift(newChat)
  currentChatId.value = newChat.conversationId
  messages.value = []
  localStorage.removeItem(`chat_messages_${newChat.conversationId}`)
  saveLocalChatList()
}

const applyDeletedChat = async (conversationId) => {
  chatList.value = chatList.value.filter(c => c.conversationId !== conversationId)
  localStorage.removeItem(`chat_messages_${conversationId}`)
  saveLocalChatList()
  if (currentChatId.value === conversationId) {
    if (chatList.value.length > 0) {
      currentChatId.value = chatList.value[0].conversationId
      loadChatMessages(currentChatId.value)
    } else {
      currentChatId.value = ''
      messages.value = []
      await createNewChat()
    }
  }
}

// 获取用户信息
const loadUserInfo = () => {
  const stored = localStorage.getItem('userInfo')
  if (stored) {
    try {
      userInfo.value = JSON.parse(stored)
      userId.value = userInfo.value.id
    } catch (e) {
      console.error('解析用户信息失败:', e)
      ElMessage.error('获取用户信息失败，请重新登录')
      router.push('/login')
    }
  } else {
    ElMessage.error('请先登录')
    router.push('/login')
  }
}

// 初始化
onMounted(async () => {
  loadUserInfo()
  if (userId.value) {
    await loadChatHistory()
    // 如果没有会话，自动创建一个
    if (chatList.value.length === 0) {
      await createNewChat()
    } else {
      // 加载第一个会话
      switchChat(chatList.value[0].conversationId)
    }
  }
})

// 创建新对话
const createNewChat = async () => {
  if (!userId.value) {
    ElMessage.error('请先登录')
    return
  }

  try {
    const res = await createConversation(userId.value, {
      aiType: AI_TYPE,
      title: `饮食规划 ${chatList.value.length + 1}`,
      description: '新建对话'
    })

    if ((res.code === 200 || res.code === 0) && res.data) {
      applyNewChat(res.data)
      ElMessage.success('创建新对话成功')
      return
    }
  } catch (error) {
    console.warn('创建后端会话失败，已切换为本地会话模式:', error)
  }

  const newChat = buildLocalConversation()
  applyNewChat(newChat)
  ElMessage.success('创建新对话成功')
}

// 加载聊天历史
const loadChatHistory = async () => {
  if (!userId.value) return
  
  try {
    const res = await getUserConversationsByAiType(userId.value, AI_TYPE)
    if ((res.code === 200 || res.code === 0) && Array.isArray(res.data) && res.data.length > 0) {
      chatList.value = res.data
      saveLocalChatList()
      return
    }
  } catch (e) {
    console.warn('加载后端会话失败，已切换为本地会话模式:', e)
  }

  chatList.value = loadLocalChatList()
}

// 切换对话
const switchChat = (conversationId) => {
  if (isLoading.value) {
    ElMessage.warning('请等待当前消息发送完成')
    return
  }
  
  // 保存当前对话的消息
  saveCurrentChatMessages()
  
  // 切换到新对话
  currentChatId.value = conversationId
  
  // 从本地缓存加载消息
  loadChatMessages(conversationId)
  
  // 移动端关闭侧边栏
  showMobileSidebar.value = false
}

// 获取当前对话标题（标题栏显示）
const getCurrentChatTitle = () => {
  // 如果有消息，使用第一条用户消息的前15个字作为标题
  if (messages.value.length > 0) {
    const firstUserMsg = messages.value.find(msg => msg.role === 'user')
    if (firstUserMsg && firstUserMsg.content) {
      const title = firstUserMsg.content.trim()
      return title.length > 15 ? title.substring(0, 15) + '...' : title
    }
  }
  // 否则使用会话的默认标题
  const chat = chatList.value.find(c => c.conversationId === currentChatId.value)
  return chat ? chat.title : '新对话'
}

// 获取聊天项标题（侧边栏列表显示）
const getChatItemTitle = (conversationId) => {
  // 从本地缓存获取该对话的消息
  try {
    const stored = localStorage.getItem(`chat_messages_${conversationId}`)
    if (stored) {
      const msgs = JSON.parse(stored)
      const firstUserMsg = msgs.find(msg => msg.role === 'user')
      if (firstUserMsg && firstUserMsg.content) {
        const title = firstUserMsg.content.trim()
        return title.length > 12 ? title.substring(0, 12) + '...' : title
      }
    }
  } catch (e) {
    console.error('获取对话标题失败:', e)
  }
  // 否则使用默认标题
  const chat = chatList.value.find(c => c.conversationId === conversationId)
  return chat ? chat.title : '新对话'
}

// 保存当前对话的消息到本地缓存
const saveCurrentChatMessages = () => {
  if (currentChatId.value && messages.value.length > 0) {
    try {
      localStorage.setItem(
        `chat_messages_${currentChatId.value}`, 
        JSON.stringify(messages.value)
      )
    } catch (e) {
      console.error('保存消息失败:', e)
    }
  }
}

// 从本地缓存加载消息
const loadChatMessages = (conversationId) => {
  try {
    const stored = localStorage.getItem(`chat_messages_${conversationId}`)
    if (stored) {
      messages.value = JSON.parse(stored)
    } else {
      messages.value = []
    }
  } catch (e) {
    console.error('加载消息失败:', e)
    messages.value = []
  }
  
  nextTick(() => {
    scrollToBottom()
  })
}

// 删除会话
const handleDeleteChat = async (conversationId) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这个对话吗？删除后无法恢复。',
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    try {
      const res = await deleteConversation(userId.value, conversationId)
      if (res.code === 200 || res.code === 0) {
        await applyDeletedChat(conversationId)
        ElMessage.success('删除成功')
        return
      }
    } catch (error) {
      console.warn('删除后端会话失败，已切换为本地删除:', error)
    }

    await applyDeletedChat(conversationId)
    ElMessage.success('删除成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除会话失败:', error)
      ElMessage.error('删除会话失败')
    }
  }
}

// 发送消息
const sendMessage = async () => {
  if (!inputMessage.value.trim() || isLoading.value) return

  const userMessage = inputMessage.value.trim()
  inputMessage.value = ''

  // 添加用户消息
  messages.value.push({
    role: 'user',
    content: userMessage
  })

  scrollToBottom()
  isLoading.value = true

  // 准备接收 AI 消息 - 先不添加消息，等收到第一个字符时再添加
  let aiMessageIndex = -1

  try {
    const url = getDietPlanSSEUrl(userMessage, currentChatId.value)
    
    cancelSSE = await fetchSSE(
      url,
      (data) => {
        // 接收消息片段
        if (aiMessageIndex === -1) {
          // 第一次收到数据时才创建消息
          aiMessageIndex = messages.value.length
          messages.value.push({
            role: 'assistant',
            content: data
          })
        } else {
          // 后续追加内容
          messages.value[aiMessageIndex].content += data
        }
        scrollToBottom()
      },
      (error) => {
        console.error('SSE 错误:', error)
        ElMessage.error('消息发送失败')
        isLoading.value = false
        // 如果还没有创建消息就出错了，创建一个错误提示
        if (aiMessageIndex === -1) {
          messages.value.push({
            role: 'assistant',
            content: '抱歉，消息发送失败，请重试。'
          })
        }
      },
      () => {
        // 完成
        isLoading.value = false
        saveCurrentChatMessages()
        scrollToBottom()
      }
    )
  } catch (error) {
    console.error('发送消息失败:', error)
    ElMessage.error('消息发送失败')
    isLoading.value = false
  }
}

// 滚动到底部
const scrollToBottom = () => {
  nextTick(() => {
    if (messagesContainerRef.value) {
      messagesContainerRef.value.scrollTop = messagesContainerRef.value.scrollHeight
    }
  })
}

// 返回主页
const goBack = () => {
  if (isLoading.value) {
    ElMessage.warning('请等待当前消息发送完成')
    return
  }
  saveCurrentChatMessages()
  showMobileSidebar.value = false
  router.push('/home')
}
</script>

<style scoped>
.chat-container {
  display: flex;
  width: 100%;
  height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
}

.sidebar {
  width: 280px;
  background: linear-gradient(180deg, #1e8449 0%, #27ae60 100%);
  color: white;
  display: flex;
  flex-direction: column;
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.1);
  position: relative;
  z-index: 10;
}

.sidebar::after {
  content: '';
  position: absolute;
  top: 0;
  right: 0;
  width: 1px;
  height: 100%;
  background: linear-gradient(180deg, transparent 0%, rgba(255, 255, 255, 0.1) 50%, transparent 100%);
}

.sidebar-header {
  padding: 25px 20px;
  border-bottom: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-header h3 {
  margin: 0 0 18px 0;
  font-size: 20px;
  font-weight: 600;
  letter-spacing: 0.5px;
}

.sidebar-header .el-button {
  width: 100%;
  height: 42px;
  font-weight: 600;
  border-radius: 10px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
}

.sidebar-header .el-button:hover {
  background: rgba(255, 255, 255, 0.25);
  transform: translateY(-2px);
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.chat-list::-webkit-scrollbar {
  width: 6px;
}

.chat-list::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.2);
  border-radius: 3px;
}

.chat-item {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
  padding: 14px 12px;
  margin-bottom: 6px;
  border-radius: 10px;
  transition: all 0.3s ease;
  background: rgba(255, 255, 255, 0.05);
}

.chat-item:hover {
  background: rgba(255, 255, 255, 0.15);
  transform: translateX(5px);
}

.chat-item.active {
  background: linear-gradient(90deg, rgba(39, 174, 96, 0.4), rgba(30, 132, 73, 0.4));
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.chat-item-content {
  display: flex;
  align-items: center;
  gap: 12px;
  flex: 1;
  cursor: pointer;
  overflow: hidden;
}

.chat-title {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 500;
}

.delete-icon {
  cursor: pointer;
  opacity: 0;
  transition: all 0.3s ease;
  color: #ff6b6b;
  padding: 4px;
  border-radius: 6px;
}

.chat-item:hover .delete-icon {
  opacity: 1;
}

.delete-icon:hover {
  background: rgba(255, 107, 107, 0.2);
  color: #ff4d4d;
}

.sidebar-footer {
  padding: 18px;
  border-top: 1px solid rgba(255, 255, 255, 0.15);
  background: rgba(255, 255, 255, 0.05);
}

.sidebar-footer .el-button {
  width: 100%;
  color: white;
  font-weight: 500;
  transition: all 0.3s ease;
}

.sidebar-footer .el-button:hover {
  background: rgba(255, 255, 255, 0.1);
  transform: translateX(-5px);
}

.chat-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  background: white;
}

.chat-header {
  padding: 25px 35px;
  border-bottom: 1px solid #e8eef5;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.05);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chat-header h2 {
  margin: 0;
  font-size: 22px;
  font-weight: 600;
  color: #2c3e50;
  background: linear-gradient(135deg, #1e8449 0%, #27ae60 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.mobile-new-chat-btn {
  display: none;
}

.mobile-menu-btn {
  display: none;
}

.mobile-overlay {
  display: none;
}

.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 30px 35px;
  background: linear-gradient(135deg, #fafbfc 0%, #f1f4f8 100%);
}

.messages-container::-webkit-scrollbar {
  width: 8px;
}

.messages-container::-webkit-scrollbar-thumb {
  background: linear-gradient(135deg, #1e8449 0%, #27ae60 100%);
  border-radius: 4px;
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #999;
  gap: 20px;
  animation: fadeIn 0.5s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.empty-state p {
  font-size: 17px;
  font-weight: 500;
}

.message-item {
  display: flex;
  gap: 14px;
  margin-bottom: 24px;
  animation: messageSlideIn 0.4s ease-out;
}

@keyframes messageSlideIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.message-item.user {
  flex-direction: row-reverse;
}

.message-avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.message-item.user .message-avatar {
  background: linear-gradient(135deg, #1e8449 0%, #27ae60 100%);
  color: white;
}

.message-item.assistant .message-avatar {
  background: linear-gradient(135deg, #fa709a 0%, #fee140 100%);
  color: white;
}

.message-content {
  max-width: 70%;
}

.message-text {
  padding: 14px 18px;
  border-radius: 16px;
  line-height: 1.7;
  word-wrap: break-word;
  white-space: pre-wrap;
  font-size: 15px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
}

.message-item.user .message-text {
  background: linear-gradient(135deg, #1e8449 0%, #27ae60 100%);
  color: white;
  border-radius: 16px 16px 4px 16px;
}

.message-item.assistant .message-text {
  background: white;
  color: #333;
  border: 1px solid #e8eef5;
  border-radius: 16px 16px 16px 4px;
}

/* Markdown 样式 */
.markdown-body {
  line-height: 1.7;
}

.markdown-body :deep(h1),
.markdown-body :deep(h2),
.markdown-body :deep(h3) {
  margin: 16px 0 12px 0;
  font-weight: 600;
  line-height: 1.4;
}

.markdown-body :deep(h1) {
  font-size: 1.5em;
  border-bottom: 2px solid #e8eef5;
  padding-bottom: 8px;
}

.markdown-body :deep(h2) {
  font-size: 1.3em;
}

.markdown-body :deep(h3) {
  font-size: 1.1em;
}

.markdown-body :deep(p) {
  margin: 8px 0;
}

.markdown-body :deep(code.inline-code) {
  background: #f6f8fa;
  padding: 2px 6px;
  border-radius: 4px;
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #e83e8c;
}

.markdown-body :deep(pre) {
  background: #f6f8fa;
  border-radius: 8px;
  padding: 12px;
  margin: 12px 0;
  overflow-x: auto;
}

.markdown-body :deep(pre code) {
  font-family: 'Consolas', 'Monaco', 'Courier New', monospace;
  font-size: 0.9em;
  color: #24292e;
  background: transparent;
  padding: 0;
}

.markdown-body :deep(ul),
.markdown-body :deep(ol) {
  margin: 12px 0;
  padding-left: 24px;
}

.markdown-body :deep(li) {
  margin: 4px 0;
}

.markdown-body :deep(strong) {
  font-weight: 600;
  color: #1a1a1a;
}

.markdown-body :deep(em) {
  font-style: italic;
}

.markdown-body :deep(del) {
  text-decoration: line-through;
  opacity: 0.7;
}

.markdown-body :deep(a) {
  color: #27ae60;
  text-decoration: none;
  border-bottom: 1px solid transparent;
  transition: border-color 0.2s;
}

.markdown-body :deep(a:hover) {
  border-bottom-color: #27ae60;
}

.markdown-body :deep(hr) {
  border: none;
  border-top: 2px solid #e8eef5;
  margin: 16px 0;
}

.markdown-body :deep(br) {
  display: block;
  content: "";
  margin: 4px 0;
}

.markdown-body :deep(img.markdown-image) {
  max-width: 100%;
  height: auto;
  border-radius: 8px;
  margin: 12px 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  cursor: pointer;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.markdown-body :deep(img.markdown-image:hover) {
  transform: scale(1.02);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

.input-container {
  padding: 25px 35px;
  border-top: 1px solid #e8eef5;
  background: linear-gradient(135deg, #ffffff 0%, #f8f9fa 100%);
  display: flex;
  gap: 12px;
  align-items: flex-end;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.05);
}

.input-container .el-input :deep(.el-textarea__inner) {
  border-radius: 14px;
  border: 2px solid #e8eef5;
  padding: 14px 16px;
  font-size: 15px;
  transition: all 0.3s ease;
  resize: none;
}

.input-container .el-input :deep(.el-textarea__inner):focus {
  border-color: #27ae60;
  box-shadow: 0 0 0 3px rgba(39, 174, 96, 0.1);
}

.input-container .el-button {
  height: 50px;
  min-width: 100px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 14px;
  background: linear-gradient(135deg, #1e8449 0%, #27ae60 100%);
  border: none;
  box-shadow: 0 6px 18px rgba(39, 174, 96, 0.3);
  transition: all 0.3s ease;
}

.input-container .el-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 22px rgba(39, 174, 96, 0.4);
}

/* 移动端适配 - 抽屉式侧边栏 */
@media screen and (max-width: 768px) {
  .chat-container {
    flex-direction: row;
    position: relative;
  }

  /* 遮罩层 */
  .mobile-overlay {
    display: block;
    position: fixed;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    background: rgba(0, 0, 0, 0.5);
    z-index: 998;
    opacity: 0;
    visibility: hidden;
    transition: all 0.3s ease;
  }

  .mobile-overlay.show {
    opacity: 1;
    visibility: visible;
  }

  /* 侧边栏抽屉 */
  .sidebar {
    position: fixed;
    left: -280px;
    top: 0;
    width: 280px;
    height: 100vh;
    z-index: 999;
    transition: left 0.3s ease;
    box-shadow: 2px 0 8px rgba(0, 0, 0, 0.15);
  }

  .sidebar.mobile-open {
    left: 0;
  }

  .sidebar::after {
    display: none;
  }

  .chat-list {
    flex: 1;
    overflow-y: auto;
    padding: 10px;
  }

  .chat-item {
    flex-direction: row;
    margin-bottom: 5px;
  }

  /* 聊天主区域全屏 */
  .chat-main {
    width: 100%;
    flex: 1;
  }

  .chat-header {
    padding: 12px 15px;
    display: flex;
    align-items: center;
    gap: 12px;
  }

  .mobile-menu-btn {
    display: flex;
    flex-shrink: 0;
  }

  .chat-header h2 {
    font-size: 17px;
    flex: 1;
    overflow: hidden;
    text-overflow: ellipsis;
    white-space: nowrap;
    margin: 0;
  }

  .mobile-new-chat-btn {
    display: flex;
    flex-shrink: 0;
  }

  .messages-container {
    padding: 15px 20px;
  }

  .message-item {
    gap: 10px;
    margin-bottom: 18px;
  }

  .message-avatar {
    width: 36px;
    height: 36px;
  }

  .message-content {
    max-width: 80%;
  }

  .message-text {
    padding: 10px 14px;
    font-size: 14px;
  }

  .input-container {
    padding: 15px 20px;
  }

  .input-container .el-textarea :deep(.el-textarea__inner) {
    font-size: 14px;
    padding: 10px 12px;
  }

  .input-container .el-button {
    height: 45px;
    min-width: 80px;
    font-size: 14px;
  }
}

@media screen and (max-width: 480px) {
  .chat-header h2 {
    font-size: 15px;
  }

  .message-content {
    max-width: 85%;
  }

  .input-container .el-button {
    min-width: 60px;
  }

  .input-container .el-button span {
    display: none;
  }
}
</style>
