package com.finance.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.finance.entity.Account;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AccountMapper extends BaseMapper<Account> {
}