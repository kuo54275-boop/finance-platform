package com.finance.controller;

import com.finance.entity.User;
import com.finance.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
@CrossOrigin // 💡 放行前端网页跨域调用
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 统一注册接口（默认赋予普通用户 user 身份）
     */
    @PostMapping("/register")
    public Map<String, Object> register(@RequestBody User user) {
        Map<String, Object> result = new HashMap<>();
        if ((user.getPhone() == null || user.getPhone().isEmpty())
                && (user.getEmail() == null || user.getEmail().isEmpty())) {
            result.put("code", 400);
            result.put("msg", "手机号或邮箱不能同时为空！");
            return result;
        }
        if (user.getRole() == null || user.getRole().isEmpty()) {
            user.setRole("user"); // 默认注册为普通用户
        }
        boolean saved = userService.save(user);
        if (saved) {
            result.put("code", 200);
            result.put("msg", "注册成功！");
        } else {
            result.put("code", 500);
            result.put("msg", "注册失败。");
        }
        return result;
    }

    /**
     * 用户与管理员统一登录接口
     */
    @PostMapping("/login")
    public Map<String, Object> login(@RequestParam String account, @RequestParam String password) {
        Map<String, Object> result = new HashMap<>();
        User user = userService.login(account, password);
        if (user != null) {
            result.put("code", 200);
            result.put("msg", "登录成功！欢迎回来");
            result.put("data", user); // 前端通过返回的 user.role 判定进入用户端还是管理员端
        } else {
            result.put("code", 400);
            result.put("msg", "账号或密码错误！");
        }
        return result;
    }
}