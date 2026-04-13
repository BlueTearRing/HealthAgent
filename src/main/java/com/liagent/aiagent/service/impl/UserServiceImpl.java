package com.liagent.aiagent.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.digest.DigestUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.liagent.aiagent.entity.User;
import com.liagent.aiagent.entity.dto.UserLoginRequest;
import com.liagent.aiagent.entity.dto.UserRegisterRequest;
import com.liagent.aiagent.entity.dto.UserUpdateRequest;
import com.liagent.aiagent.entity.vo.UserVO;
import com.liagent.aiagent.mapper.UserMapper;
import com.liagent.aiagent.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import static com.liagent.aiagent.constant.UserConstant.USER_LOGIN_STATE;

/**
 * 用户服务实现
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    /**
     * 盐值，混淆密码
     */
    public static final String SALT = "aiagent";

    @Override
    public long userRegister(UserRegisterRequest userRegisterRequest) {
        String accountName = userRegisterRequest.getAccountName();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        
        // 校验
        if (StrUtil.hasBlank(accountName, userPassword, checkPassword)) {
            throw new RuntimeException("参数为空");
        }
        if (accountName.length() < 4) {
            throw new RuntimeException("用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new RuntimeException("用户密码过短");
        }
        if (!userPassword.equals(checkPassword)) {
            throw new RuntimeException("两次输入的密码不一致");
        }
        
        // 检查账号是否重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_name", accountName);
        long count = this.baseMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new RuntimeException("账号重复");
        }
        
        // 加密
        String encryptPassword = DigestUtil.md5Hex(SALT + userPassword);
        
        // 插入数据
        User user = new User();
        user.setAccountName(accountName);
        user.setUserPassword(encryptPassword);
        user.setUserName("用户" + accountName);
        user.setUserRole("user");
        
        boolean saveResult = this.save(user);
        if (!saveResult) {
            throw new RuntimeException("注册失败，数据库错误");
        }
        return user.getId();
    }

    @Override
    public UserVO userLogin(UserLoginRequest userLoginRequest, HttpServletRequest request) {
        String accountName = userLoginRequest.getAccountName();
        String userPassword = userLoginRequest.getUserPassword();
        
        // 校验
        if (StrUtil.hasBlank(accountName, userPassword)) {
            throw new RuntimeException("参数为空");
        }
        if (accountName.length() < 4) {
            throw new RuntimeException("账号错误");
        }
        if (userPassword.length() < 8) {
            throw new RuntimeException("密码错误");
        }
        
        // 加密
        String encryptPassword = DigestUtil.md5Hex(SALT + userPassword);
        
        // 查询用户是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("account_name", accountName);
        queryWrapper.eq("user_password", encryptPassword);
        User user = this.baseMapper.selectOne(queryWrapper);
        
        // 用户不存在
        if (user == null) {
            log.info("user login failed, accountName cannot match userPassword");
            throw new RuntimeException("用户不存在或密码错误");
        }
        
        // 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, user);
        return this.getLoginUserVO(user);
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        // 先判断是否已登录
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null || currentUser.getId() == null) {
            throw new RuntimeException("未登录");
        }
        
        // 从数据库查询（追求性能的话可以注释，直接走缓存）
        long userId = currentUser.getId();
        currentUser = this.getById(userId);
        if (currentUser == null) {
            throw new RuntimeException("未登录");
        }
        return currentUser;
    }

    @Override
    public UserVO getLoginUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }

    @Override
    public boolean userLogout(HttpServletRequest request) {
        if (request.getSession().getAttribute(USER_LOGIN_STATE) == null) {
            throw new RuntimeException("未登录");
        }
        // 移除登录态
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return true;
    }




    @Override
    public UserVO getUserVO(User user) {
        if (user == null) {
            return null;
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return userVO;
    }
}
