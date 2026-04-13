import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建 axios 实例
const request = axios.create({
  baseURL: 'http://localhost:8123/api',
  timeout: 30000,
  withCredentials: true // 允许携带 cookie
})

// 请求拦截器
request.interceptors.request.use(
  config => {
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果返回的状态码不是 200，则判断为错误
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.msg || '请求失败')
      return Promise.reject(new Error(res.msg || '请求失败'))
    }
    
    return res
  },
  error => {
    console.error('响应错误:', error)
    if (!error.config?.skipErrorMessage) {
      ElMessage.error(error.msg || '网络错误')
    }
    return Promise.reject(error)
  }
)

export default request

