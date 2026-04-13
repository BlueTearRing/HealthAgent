<template>
  <div class="home-container">
    <div class="header">
      <div class="header-left">
        <div class="logo-wrapper">
          <div class="logo-icon">
            <el-icon :size="28"><Sunny /></el-icon>
          </div>
          <h1 class="logo">AI 健康平台</h1>
        </div>
      </div>

      <div class="header-right">
        <template v-if="userInfo">
          <el-dropdown trigger="click" @command="handleCommand" class="user-dropdown">
            <div class="user-profile">
              <span class="welcome-text">欢迎，</span>
              <span class="user-name">{{ userInfo.userName || userInfo.userAccount }}</span>
              <el-icon class="dropdown-icon"><ArrowDown /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="logout" class="logout-item">
                  <el-icon><SwitchButton /></el-icon>
                  <span>退出登录</span>
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button type="primary" size="large" class="login-btn" @click="$router.push('/login')">
            <el-icon><User /></el-icon>
            <span>登录 / 注册</span>
          </el-button>
        </template>
      </div>
    </div>
    
    <div class="content">
      <h2 class="subtitle">请选择一个 AI 应用</h2>
      <div class="app-cards">
        <div class="app-card" @click="goToApp('diet-plan')">
          <div class="card-icon">
            <el-icon :size="60"><Apple /></el-icon>
          </div>
          <h3 class="card-title">AI 饮食规划</h3>
          <p class="card-desc">智能分析您的饮食需求，为您提供个性化的饮食方案和营养建议</p>
          <el-button type="primary" class="card-btn">进入应用</el-button>
        </div>
        
        <div class="app-card" @click="goToApp('medical-chat')">
          <div class="card-icon">
            <el-icon :size="60"><FirstAidKit /></el-icon>
          </div>
          <h3 class="card-title">AI 医疗问答</h3>
          <p class="card-desc">专业的医疗健康咨询服务，随时解答您的健康疑问</p>
          <el-button type="primary" class="card-btn">进入应用</el-button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Apple, 
  FirstAidKit, 
  Sunny, 
  User, 
  ArrowDown, 
  SwitchButton 
} from '@element-plus/icons-vue'
import { userLogout } from '@/api/user'

const router = useRouter()
const userInfo = ref(null)

onMounted(() => {
  const storedUser = localStorage.getItem('userInfo')
  if (storedUser) {
    try {
      userInfo.value = JSON.parse(storedUser)
    } catch (e) {
      console.error('解析用户信息失败:', e)
    }
  }
})

const goToApp = (path) => {
  // 检查是否登录
  if (!userInfo.value) {
    ElMessageBox.confirm('使用 AI 应用需要先登录，是否前往登录？', '提示', {
      confirmButtonText: '去登录',
      cancelButtonText: '取消',
      type: 'info'
    }).then(() => {
      router.push('/login')
    }).catch(() => {})
  } else {
    router.push(`/${path}`)
  }
}

const handleLogout = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出登录吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    localStorage.removeItem('userInfo')
    userInfo.value = null
    ElMessage.success('退出登录成功')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('退出登录失败:', error)
    }
  }
}

const handleCommand = (command) => {
  if (command === 'logout') {
    handleLogout()
  }
}
</script>

<style scoped>
.home-container {
  width: 100%;
  min-height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: auto;
  position: relative;
}

.home-container::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: 
    radial-gradient(circle at 20% 50%, rgba(255, 255, 255, 0.1) 0%, transparent 50%),
    radial-gradient(circle at 80% 80%, rgba(255, 255, 255, 0.1) 0%, transparent 50%);
  pointer-events: none;
}

.header {
  position: relative;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 50px;
  background: rgba(255, 255, 255, 0.15);
  backdrop-filter: blur(20px);
  border-bottom: 1px solid rgba(255, 255, 255, 0.2);
  z-index: 10;
  animation: slideDown 0.5s ease-out;
}

@keyframes slideDown {
  from {
    opacity: 0;
    transform: translateY(-20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.header-left {
  display: flex;
  align-items: center;
  gap: 40px;
}

.logo-wrapper {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  width: 45px;
  height: 45px;
  background: rgba(255, 255, 255, 0.25);
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
}

.logo-icon:hover {
  transform: rotate(180deg);
  background: rgba(255, 255, 255, 0.35);
}

.logo {
  font-size: 28px;
  font-weight: bold;
  color: white;
  text-shadow: 0 2px 10px rgba(0, 0, 0, 0.2);
  letter-spacing: 1px;
  margin: 0;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.user-dropdown {
  cursor: pointer;
}

.user-profile {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 8px 16px;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50px;
  backdrop-filter: blur(10px);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.user-profile:hover {
  background: rgba(255, 255, 255, 0.3);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.15);
}

.user-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: 2px solid rgba(255, 255, 255, 0.5);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.user-name {
  color: white;
  font-size: 15px;
  font-weight: 600;
  text-shadow: 0 1px 3px rgba(0, 0, 0, 0.2);
  max-width: 150px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.dropdown-icon {
  color: white;
  font-size: 14px;
  transition: transform 0.3s ease;
}

.user-profile:hover .dropdown-icon {
  transform: translateY(2px);
}

.login-btn {
  height: 44px;
  padding: 0 28px;
  font-size: 15px;
  font-weight: 600;
  border-radius: 50px;
  background: rgba(255, 255, 255, 0.25);
  border: 1px solid rgba(255, 255, 255, 0.4);
  color: white;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 15px rgba(0, 0, 0, 0.15);
  transition: all 0.3s ease;
}

.login-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: translateY(-2px);
  box-shadow: 0 6px 20px rgba(0, 0, 0, 0.2);
}

.login-btn .el-icon {
  margin-right: 6px;
}

/* 下拉菜单样式 */
:deep(.el-dropdown-menu) {
  padding: 8px;
  border-radius: 12px;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  border: 1px solid rgba(0, 0, 0, 0.05);
}

:deep(.el-dropdown-menu__item) {
  border-radius: 8px;
  padding: 10px 16px;
  margin: 2px 0;
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 14px;
  transition: all 0.2s ease;
}

:deep(.el-dropdown-menu__item:hover) {
  background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
}

:deep(.el-dropdown-menu__item .el-icon) {
  font-size: 16px;
}

:deep(.user-info-item) {
  cursor: default;
  background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
  margin-bottom: 4px;
}

:deep(.user-info-item:hover) {
  background: linear-gradient(135deg, #667eea10 0%, #764ba210 100%);
}

.user-email {
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

:deep(.logout-item) {
  color: #f56c6c;
  margin-top: 4px;
}

:deep(.logout-item:hover) {
  background: #fef0f0;
}

.content {
  position: relative;
  max-width: 1200px;
  margin: 80px auto;
  padding: 0 40px;
  z-index: 1;
}

.subtitle {
  text-align: center;
  font-size: 42px;
  font-weight: bold;
  color: white;
  margin-bottom: 70px;
  text-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
  animation: fadeInUp 0.8s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.app-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(380px, 1fr));
  gap: 50px;
  justify-items: center;
  padding-bottom: 60px;
}

.app-card {
  width: 100%;
  max-width: 420px;
  padding: 50px 35px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 0 15px 40px rgba(0, 0, 0, 0.25);
  text-align: center;
  cursor: pointer;
  transition: all 0.4s cubic-bezier(0.175, 0.885, 0.32, 1.275);
  position: relative;
  overflow: hidden;
  animation: cardFadeIn 0.8s ease-out;
  animation-fill-mode: both;
}

.app-card:nth-child(1) {
  animation-delay: 0.2s;
}

.app-card:nth-child(2) {
  animation-delay: 0.4s;
}

@keyframes cardFadeIn {
  from {
    opacity: 0;
    transform: translateY(30px) scale(0.9);
  }
  to {
    opacity: 1;
    transform: translateY(0) scale(1);
  }
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  height: 5px;
  background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
  transform: scaleX(0);
  transition: transform 0.4s ease;
}

.app-card:hover::before {
  transform: scaleX(1);
}

.app-card:hover {
  transform: translateY(-15px) scale(1.03);
  box-shadow: 0 25px 60px rgba(0, 0, 0, 0.35);
}

.card-icon {
  display: inline-block;
  width: 90px;
  height: 90px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin: 0 auto 25px;
  box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
  transition: all 0.4s ease;
}

.app-card:hover .card-icon {
  transform: scale(1.1) rotate(5deg);
  box-shadow: 0 15px 40px rgba(102, 126, 234, 0.6);
}

.card-title {
  font-size: 26px;
  font-weight: bold;
  color: #333;
  margin-bottom: 18px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.card-desc {
  font-size: 15px;
  color: #666;
  line-height: 1.8;
  margin-bottom: 35px;
  min-height: 70px;
}

.card-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.card-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 28px rgba(102, 126, 234, 0.5);
}

.welcome-text {
  color: rgba(255, 255, 255, 0.9);
  font-size: 15px;
  font-weight: 500;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .header {
    padding: 15px 20px;
  }

  .logo-wrapper {
    gap: 8px;
  }

  .logo-icon {
    width: 38px;
    height: 38px;
  }

  .logo-icon :deep(.el-icon) {
    font-size: 22px !important;
  }

  .logo {
    font-size: 20px;
  }

  .user-profile {
    padding: 6px 12px;
    gap: 8px;
  }

  .welcome-text {
    display: none;
  }

  .user-name {
    font-size: 14px;
    max-width: 100px;
  }

  .login-btn {
    height: 38px;
    padding: 0 20px;
    font-size: 14px;
  }

  .login-btn span {
    display: none;
  }

  .login-btn .el-icon {
    margin-right: 0;
  }

  .content {
    margin: 40px auto;
    padding: 0 20px;
  }

  .subtitle {
    font-size: 28px;
    margin-bottom: 40px;
    letter-spacing: 1px;
  }

  .app-cards {
    grid-template-columns: 1fr;
    gap: 30px;
    padding-bottom: 40px;
  }

  .app-card {
    max-width: 100%;
    padding: 35px 25px;
  }

  .card-icon {
    width: 70px;
    height: 70px;
  }

  .card-icon :deep(.el-icon) {
    font-size: 48px !important;
  }

  .card-title {
    font-size: 22px;
  }

  .card-desc {
    font-size: 14px;
    min-height: auto;
  }

  .card-btn {
    height: 45px;
  }

  /* 移动端禁用悬停效果 */
  .app-card:hover {
    transform: none;
    box-shadow: 0 15px 40px rgba(0, 0, 0, 0.25);
  }

  .app-card:hover .card-icon {
    transform: none;
    box-shadow: 0 10px 30px rgba(102, 126, 234, 0.4);
  }

  .logo-icon:hover {
    transform: none;
  }
}

@media screen and (max-width: 480px) {
  .header {
    padding: 12px 15px;
  }

  .logo {
    font-size: 18px;
  }

  .subtitle {
    font-size: 24px;
    margin-bottom: 30px;
  }

  .app-card {
    padding: 30px 20px;
  }
}
</style>

