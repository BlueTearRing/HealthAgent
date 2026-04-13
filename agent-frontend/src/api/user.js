import request from '@/utils/request'

/**
 * 用户注册
 * @param {Object} data - 注册信息
 */
export function userRegister(data) {
  return request({
    url: '/user/register',
    method: 'post',
    data
  })
}

/**
 * 用户登录
 * @param {Object} data - 登录信息
 */
export function userLogin(data) {
  return request({
    url: '/user/login',
    method: 'post',
    data
  })
}

/**
 * 用户注销
 */
export function userLogout() {
  return request({
    url: '/user/logout',
    method: 'post'
  })
}

/**
 * 获取当前登录用户
 */
export function getLoginUser() {
  return request({
    url: '/user/get/login',
    method: 'get'
  })
}

