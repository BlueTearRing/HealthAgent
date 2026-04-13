package com.liagent.aiagent.controller;

import com.liagent.aiagent.common.Result;
import com.liagent.aiagent.entity.User;
import com.liagent.aiagent.entity.dto.UserLoginRequest;
import com.liagent.aiagent.entity.dto.UserRegisterRequest;
import com.liagent.aiagent.entity.dto.UserUpdateRequest;
import com.liagent.aiagent.entity.vo.UserVO;
import com.liagent.aiagent.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import jakarta.annotation.Resource;

/**
 * 用户接口
 */
@RestController
@RequestMapping("/user")
@Slf4j
@Tag(name = "用户接口")
public class UserController {

    @Resource
    private UserService userService;

    /**
     * 用户注册
     *
     * @param userRegisterRequest 用户注册请求
     * @return 新用户 id
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册")
    public Result<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        if (userRegisterRequest == null) {
            return Result.error(200, "请求参数为空");
        }
        try {
            long result = userService.userRegister(userRegisterRequest);
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户注册失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 用户登录
     *
     * @param userLoginRequest 用户登录请求
     * @param request          请求
     * @return 脱敏后的用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<UserVO> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return Result.error(200, "请求参数为空");
        }
        try {
            UserVO userVO = userService.userLogin(userLoginRequest, request);
            return Result.success(userVO);
        } catch (Exception e) {
            log.error("用户登录失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 用户注销
     *
     * @param request 请求
     * @return 是否成功
     */
    @PostMapping("/logout")
    @Operation(summary = "用户注销")
    public Result<Boolean> userLogout(HttpServletRequest request) {
        if (request == null) {
            return Result.error(200, "请求参数为空");
        }
        try {
            boolean result = userService.userLogout(request);
            return Result.success(result);
        } catch (Exception e) {
            log.error("用户注销失败", e);
            return Result.error(400, e.getMessage());
        }
    }

    /**
     * 获取当前登录用户
     *
     * @param request 请求
     * @return 当前登录用户信息
     */
    @GetMapping("/get/login")
    @Operation(summary = "获取当前登录用户")
    public Result<UserVO> getLoginUser(HttpServletRequest request) {
        try {
            User user = userService.getLoginUser(request);
            return Result.success(userService.getUserVO(user));
        } catch (Exception e) {
            log.error("获取当前登录用户失败", e);
            return Result.error(500, e.getMessage());
        }
    }
}
