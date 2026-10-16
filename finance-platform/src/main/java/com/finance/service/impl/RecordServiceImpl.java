package com.finance.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.finance.entity.Account;
import com.finance.entity.Record;
import com.finance.mapper.RecordMapper;
import com.finance.service.AccountService;
import com.finance.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class RecordServiceImpl extends ServiceImpl<RecordMapper, Record> implements RecordService {

    @Autowired
    private AccountService accountService;

    @Override
    @Transactional(rollbackFor = Exception.class) // 💡 开启事务：确保“记账”和“扣款”要么同时成功，要么同时失败！
    public boolean save(Record entity) {
        // 1. 先把账目记录存入 record 表
        boolean recordSaved = super.save(entity);
        if (!recordSaved) {
            return false;
        }

        // 2. 检查有没有关联资产账户，如果没有就不扣款
        if (entity.getAccountId() == null) {
            return true;
        }

        // 3. 核心联动：获取对应的资产账户
        Account account = accountService.getById(entity.getAccountId());
        if (account == null) {
            throw new RuntimeException("绑定的资产账户不存在！");
        }

        // 4. 根据记账类型计算新余额
        BigDecimal currentBalance = account.getBalance();
        if ("EXPENSE".equals(entity.getType())) {
            // 支出：旧余额 - 记账金额
            account.setBalance(currentBalance.subtract(entity.getAmount()));
        } else if ("INCOME".equals(entity.getType())) {
            // 收入：旧余额 + 记账金额
            account.setBalance(currentBalance.add(entity.getAmount()));
        }

        // 5. 更新账户表里的余额数据
        return accountService.updateById(account);
    }
}