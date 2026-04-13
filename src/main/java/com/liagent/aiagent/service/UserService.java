package com.liagent.aiagent.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.liagent.aiagent.entity.User;
import com.liagent.aiagent.entity.dto.UserLoginRequest;
import com.liagent.aiagent.entity.dto.UserRegisterRequest;
import com.liagent.aiagent.entity.dto.UserUpdateRequest;
import com.liagent.aiagent.entity.vo.UserVO;

import jakarta.servlet.http.HttpServletRequest;

/**
 * 用户服务
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户 id
     */
    long userRegister(UserRegisterRequest userRegisterRequest);

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求
     * @return 脱敏后的用户信息
     */
    UserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request);

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * 获取脱敏的已登录用户信息
     *
     * @return 脱敏用户信息
     */
    UserVO getLoginUserVO(User user);

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否成功
     */
    boolean userLogout(HttpServletRequest request);


    /**
     * 用户脱敏
     *
     * @param user 用户
     * @return 脱敏用户信息
     */
    UserVO getUserVO(User user);
}
