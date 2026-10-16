package com.finance.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.finance.entity.User;

// 继承 IService 是 MyBatis-Plus 的绝招，能让 Service 自动拥有大批高级单表操作方法
public interface UserService extends IService<User> {
    // 我们可以定义一个专用的登录业务
    User login(String account, String password);
}