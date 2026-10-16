package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@Data
@TableName("user")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String nickname;
    private String phone;
    private String email;
    private String password;
    private String role; // 💡 新增这一行：用于区分普通用户(user)与超级管理员(admin)
}