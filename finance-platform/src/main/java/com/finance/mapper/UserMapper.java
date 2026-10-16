package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper // 告诉 Spring Boot 这是一个 MyBatis 的 Mapper 接口
public interface UserMapper extends BaseMapper<User> {
    // 继承了 BaseMapper<User> 后，你就自动拥有了 insert, delete, update, selectById 等所有基础方法！
}