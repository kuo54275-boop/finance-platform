package com.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.Account;
import com.finance.entity.Record;
import com.finance.service.AccountService;
import com.finance.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/account")
@CrossOrigin
public class AccountController {

    @Autowired
    private AccountService accountService;

    @Autowired
    private RecordService recordService;

    /**
     * 1. 获取用户绑定的所有资金账户列表
     */
    @GetMapping("/list")
    public Map<String, Object> getAccountList(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<Account> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Account::getUserId, userId);
        List<Account> list = accountService.list(wrapper);

        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    /**
     * 2. 增设/绑定新的资产账户
     */
    @PostMapping("/add")
    public Map<String, Object> addAccount(@RequestBody Account account) {
        Map<String, Object> result = new HashMap<>();
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        boolean saved = accountService.save(account);
        result.put("code", saved ? 200 : 500);
        result.put("msg", saved ? "资产账户绑定成功" : "绑定失败");
        return result;
    }

    /**
     * 3. 核心修复：清算并注销账户（包含自动清算流水划拨）
     */
    @DeleteMapping("/delete")
    public Map<String, Object> deleteAccount(@RequestParam Integer accountId,
                                             @RequestParam(required = false) Integer targetAccountId) {
        Map<String, Object> result = new HashMap<>();

        // 3.1 查出待注销的账户信息
        Account currentAccount = accountService.getById(accountId);
        if (currentAccount == null) {
            result.put("code", 404);
            result.put("msg", "待注销账户不存在");
            return result;
        }

        BigDecimal balanceToTransfer = currentAccount.getBalance() != null ? currentAccount.getBalance() : BigDecimal.ZERO;

        // 3.2 如果账户有余额，且指定了合法的接收账户，执行资金自动划拨清算
        if (balanceToTransfer.compareTo(BigDecimal.ZERO) > 0 && targetAccountId != null) {

            if (accountId.equals(targetAccountId)) {
                result.put("code", 400);
                result.put("msg", "无法将资金清算转入账户自身");
                return result;
            }

            Account targetAccount = accountService.getById(targetAccountId);
            if (targetAccount != null) {
                // 💡 核心修复一：为目标接收账户生成一笔完美的【收入(INCOME)】流水，让明细账目红绿分明
                Record incomingRecord = new Record();
                incomingRecord.setUserId(currentAccount.getUserId());
                incomingRecord.setAccountId(targetAccountId); // 关联接收资产的账户
                incomingRecord.setType("INCOME");             // 💡 彻底修复：改成 INCOME 收入
                incomingRecord.setCategory("账户清算");
                incomingRecord.setAmount(balanceToTransfer);
                incomingRecord.setRemark("🤖 销户资产自动清算：账户[" + currentAccount.getName() + "]注销，资金全额转入[" + targetAccount.getName() + "]");
                recordService.save(incomingRecord);

                // 💡 核心修复二：实时更新接收账户的数据库余额，防止钱“凭空消失”
                targetAccount.setBalance(targetAccount.getBalance().add(balanceToTransfer));
                accountService.updateById(targetAccount);
            }
        }

        // 3.3 安全抹去原资产账户
        boolean removed = accountService.removeById(accountId);

        result.put("code", removed ? 200 : 500);
        result.put("msg", removed ? "账户资产清算完成，销户成功！" : "销户失败");
        return result;
    }
}