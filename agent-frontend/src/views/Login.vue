<template>
  <div class="login-container">
    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    <div class="login-box">
      <div class="logo-section">
        <div class="logo-icon">
          <el-icon :size="48"><Sunny /></el-icon>
        </div>
        <h1 class="title">AI 健康平台</h1>
        <p class="subtitle">智能健康管理，从这里开始</p>
      </div>
      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" class="login-form">
        <el-form-item prop="accountName">
          <el-input
            v-model="loginForm.accountName"
            placeholder="请输入账号"
            prefix-icon="User"
            size="large"
            class="custom-input"
          />
        </el-form-item>
        <el-form-item prop="userPassword">
          <el-input
            v-model="loginForm.userPassword"
            type="password"
            placeholder="请输入密码"
            prefix-icon="Lock"
            size="large"
            class="custom-input"
            @keyup.enter="handleLogin"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            type="primary"
            size="large"
            :loading="loading"
            @click="handleLogin"
            class="login-btn"
          >
            <span v-if="!loading">立即登录</span>
            <span v-else>登录中...</span>
          </el-button>
        </el-form-item>
        <div class="footer-links">
          <router-link to="/register" class="link">
            <span>还没有账号？</span>
            <span class="link-highlight">立即注册</span>
          </router-link>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Sunny } from '@element-plus/icons-vue'
import { userLogin } from '@/api/user'

const router = useRouter()
const loginFormRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  accountName: '',
  userPassword: ''
})

const rules = {
  accountName: [
    { required: true, message: '请输入账号', trigger: 'blur' },
    { min: 4, message: '账号长度不能少于4位', trigger: 'blur' }
  ],
  userPassword: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ]
}

const handleLogin = async () => {
  if (!loginFormRef.value) return
  
  await loginFormRef.value.validate(async (valid) => {
    if (valid) {
      loading.value = true
      try {
        const res = await userLogin(loginForm)
        if (res.code === 200 || res.code === 0) {
          ElMessage.success('登录成功')
          // 保存用户信息到本地存储
          localStorage.setItem('userInfo', JSON.stringify(res.data))
          // 跳转到主页
          router.push('/home')
        }
      } catch (error) {
        console.error('登录失败:', error)
      } finally {
        loading.value = false
      }
    }
  })
}
</script>

<style scoped>
.login-container {
  position: relative;
  width: 100%;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

.background-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: 100px;
  animation-delay: 3s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: -50px;
  animation-delay: 6s;
}

@keyframes float {
  0%, 100% {
    transform: translate(0, 0) scale(1);
  }
  50% {
    transform: translate(30px, -30px) scale(1.1);
  }
}

.login-box {
  position: relative;
  width: 440px;
  padding: 50px 45px;
  background: rgba(255, 255, 255, 0.98);
  border-radius: 24px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  backdrop-filter: blur(10px);
  animation: slideUp 0.6s ease-out;
  z-index: 1;
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-section {
  text-align: center;
  margin-bottom: 35px;
}

.logo-icon {
  display: inline-block;
  width: 80px;
  height: 80px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-bottom: 20px;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.4);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

.title {
  font-size: 32px;
  font-weight: bold;
  color: #333;
  margin: 0 0 8px 0;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.subtitle {
  font-size: 15px;
  color: #666;
  margin: 0;
}

.login-form {
  margin-top: 25px;
}

.custom-input :deep(.el-input__wrapper) {
  border-radius: 12px;
  padding: 12px 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.custom-input :deep(.el-input__wrapper):hover {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.2);
}

.custom-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.login-btn {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
  box-shadow: 0 8px 20px rgba(102, 126, 234, 0.3);
  transition: all 0.3s ease;
}

.login-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 12px 24px rgba(102, 126, 234, 0.4);
}

.login-btn:active {
  transform: translateY(0);
}

.footer-links {
  text-align: center;
  margin-top: 25px;
}

.link {
  color: #666;
  text-decoration: none;
  font-size: 14px;
  transition: all 0.3s ease;
}

.link-highlight {
  color: #667eea;
  font-weight: 600;
  margin-left: 5px;
}

.link:hover .link-highlight {
  color: #764ba2;
  text-decoration: underline;
}

/* 移动端适配 */
@media screen and (max-width: 768px) {
  .login-box {
    width: 90%;
    max-width: 400px;
    padding: 40px 30px;
  }

  .logo-icon {
    width: 70px;
    height: 70px;
  }

  .logo-icon :deep(.el-icon) {
    font-size: 42px !important;
  }

  .title {
    font-size: 28px;
  }

  .subtitle {
    font-size: 14px;
  }

  .login-btn {
    height: 44px;
  }
}

@media screen and (max-width: 480px) {
  .login-box {
    width: 95%;
    padding: 35px 25px;
  }

  .logo-icon {
    width: 65px;
    height: 65px;
  }

  .title {
    font-size: 26px;
  }

  .subtitle {
    font-size: 13px;
  }
}
</style>

