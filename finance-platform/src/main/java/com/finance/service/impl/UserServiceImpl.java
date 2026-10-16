package com.finance.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.entity.User;
import com.finance.mapper.UserMapper;
import com.finance.service.UserService;
import org.springframework.stereotype.Service;

@Service // 别忘了这个注解，告诉 Spring 容器接管这个业务类
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {

    @Override
    public User login(String account, String password) {
        // 核心逻辑：用户输入账号（可能是手机号或邮箱），去数据库查匹配的记录
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, account).or().eq(User::getEmail, account);

        User user = this.getOne(wrapper);

        // 验证密码是否正确
        if (user != null && user.getPassword().equals(password)) {
            return user; // 登录成功，返回用户信息
        }
        return null; // 登录失败
    }
}