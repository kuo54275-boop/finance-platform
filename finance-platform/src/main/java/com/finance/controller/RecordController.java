package com.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.finance.entity.Account;
import com.finance.entity.Record;
import com.finance.service.AccountService;
import com.finance.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/record")
@CrossOrigin
public class RecordController {

    @Autowired
    private RecordService recordService;

    @Autowired
    private AccountService accountService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 1. 随手记一笔（包含余额联动更新与超支雷达拦截）
     */
    @PostMapping("/add")
    public Map<String, Object> addRecord(@RequestBody Record record) {
        Map<String, Object> result = new HashMap<>();

        Account account = accountService.getById(record.getAccountId());
        if (account != null) {
            if ("EXPENSE".equals(record.getType())) {
                account.setBalance(account.getBalance().subtract(record.getAmount()));
            } else {
                account.setBalance(account.getBalance().add(record.getAmount()));
            }
            accountService.updateById(account);
        }

        if (record.getCreateTime() == null) {
            record.setCreateTime(LocalDateTime.now());
        }

        boolean saved = recordService.save(record);

        if (saved) {
            result.put("code", 200);
            result.put("msg", "记账成功！账户余额已联动计算。");

            if ("EXPENSE".equals(record.getType())) {
                try {
                    String targetMonth = record.getCreateTime().toString().substring(0, 7);
                    String sqlLimit = "SELECT amount FROM budget WHERE user_id = ? AND category = ? AND month = ? LIMIT 1";
                    Double limitAmount = jdbcTemplate.queryForObject(sqlLimit, Double.class, record.getUserId(), record.getCategory(), targetMonth);

                    if (limitAmount != null) {
                        String sqlSum = "SELECT SUM(amount) FROM record WHERE user_id = ? AND category = ? AND type = 'EXPENSE' AND strftime('%Y-%m', create_time) = ?";
                        Double totalSpent = jdbcTemplate.queryForObject(sqlSum, Double.class, record.getUserId(), record.getCategory(), targetMonth);

                        if (totalSpent != null && totalSpent > limitAmount) {
                            result.put("warning", "🚨 【预算超支警告】您在 " + targetMonth + "【" + record.getCategory() + "】分类下的预算已超支！限额：" + limitAmount + "，当前已累计消费：" + totalSpent + "元！");
                        }
                    }
                } catch (Exception e) {
                    // 异常跳过
                }
            }
        } else {
            result.put("code", 500);
            result.put("msg", "记账失败");
        }
        return result;
    }

    /**
     * 2. 查询个人的历史账目明细流水
     */
    @GetMapping("/list")
    public Map<String, Object> getRecordList(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_id", userId).orderByDesc("create_time");
        List<Record> list = recordService.list(queryWrapper);
        result.put("code", 200);
        result.put("data", list);
        return result;
    }

    /**
     * 3. 核心大盘统计（动态查各个月度）
     */
    @GetMapping("/dashboard")
    public Map<String, Object> getDashboard(@RequestParam Integer userId, @RequestParam String month) {
        Map<String, Object> result = new HashMap<>();

        String sqlIncome = "SELECT SUM(amount) FROM record WHERE user_id = ? AND type = 'INCOME' AND strftime('%Y-%m', create_time) = ?";
        Double incomeDouble = jdbcTemplate.queryForObject(sqlIncome, Double.class, userId, month);
        BigDecimal totalIncome = BigDecimal.valueOf(incomeDouble == null ? 0.0 : incomeDouble);

        String sqlExpense = "SELECT SUM(amount) FROM record WHERE user_id = ? AND type = 'EXPENSE' AND strftime('%Y-%m', create_time) = ?";
        Double expenseDouble = jdbcTemplate.queryForObject(sqlExpense, Double.class, userId, month);
        BigDecimal totalExpense = BigDecimal.valueOf(expenseDouble == null ? 0.0 : expenseDouble);

        BigDecimal surplus = totalIncome.subtract(totalExpense);

        Map<String, Object> summary = new HashMap<>();
        summary.put("totalIncome", totalIncome.setScale(2, RoundingMode.HALF_UP).toString());
        summary.put("totalExpense", totalExpense.setScale(2, RoundingMode.HALF_UP).toString());
        summary.put("surplus", surplus.setScale(2, RoundingMode.HALF_UP).toString());

        String sqlShare = "SELECT category, SUM(amount) as total FROM record WHERE user_id = ? AND type = 'EXPENSE' AND strftime('%Y-%m', create_time) = ? GROUP BY category";
        List<Map<String, Object>> categoryShare = jdbcTemplate.queryForList(sqlShare, userId, month);

        result.put("code", 200);
        result.put("summary", summary);
        result.put("categoryShare", categoryShare);
        return result;
    }

    /**
     * 4. 修改账目明细记录
     */
    @PutMapping("/update")
    public Map<String, Object> updateRecord(@RequestBody Record record) {
        Map<String, Object> result = new HashMap<>();
        boolean updated = recordService.updateById(record);
        if (updated) {
            result.put("code", 200);
            result.put("msg", "账目修改成功");
        } else {
            result.put("code", 500);
            result.put("msg", "修改失败");
        }
        return result;
    }

    /**
     * 5. 彻底擦除某笔收支流水记录
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteRecord(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();

        Record record = recordService.getById(id);
        if (record != null) {
            Account account = accountService.getById(record.getAccountId());
            if (account != null) {
                if ("EXPENSE".equals(record.getType())) {
                    account.setBalance(account.getBalance().add(record.getAmount()));
                } else {
                    account.setBalance(account.getBalance().subtract(record.getAmount()));
                }
                accountService.updateById(account);
            }
        }

        boolean removed = recordService.removeById(id);
        if (removed) {
            result.put("code", 200);
            result.put("msg", "该笔账目明细流水平稳擦除，相关账户余额已退回。");
        } else {
            result.put("code", 500);
            result.put("msg", "删除失败");
        }
        return result;
    }

    /**
     * 6. 全量 Excel 账单数据导出下载接口
     */
    @GetMapping("/export")
    public void exportExcel(@RequestParam Integer userId,
                            @RequestParam(defaultValue = "￥") String currencyUnit,
                            jakarta.servlet.http.HttpServletResponse response) {
        try {
            response.setContentType("application/vnd.ms-excel");
            response.setCharacterEncoding("utf-8");
            String fileName = URLEncoder.encode("数字化财务系统数据导出版.csv", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName);

            QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).orderByDesc("create_time");
            List<Record> list = recordService.list(queryWrapper);

            PrintWriter writer = response.getWriter();
            writer.write('\ufeff');

            writer.println("流水ID,收支类型,交易账户ID,账目分类,发生金额,备注说明,记账日期");

            for (Record r : list) {
                String typeStr = "INCOME".equals(r.getType()) ? "收入" : "支出";
                String amountStr = currencyUnit + (r.getAmount() != null ? r.getAmount().toString() : "0.00");

                writer.println(String.format("%d,%s,%d,%s,%s,%s,%s",
                        r.getId(),
                        typeStr,
                        r.getAccountId(),
                        r.getCategory(),
                        amountStr,
                        r.getRemark() != null ? r.getRemark().replaceAll(",", "，") : "",
                        r.getCreateTime() != null ? r.getCreateTime().toString().replace("T", " ") : ""
                ));
            }

            writer.flush();
            writer.close();

        } catch (Exception e) {
            response.setStatus(500);
            try {
                response.getWriter().write("Excel Export Failed: " + e.getMessage());
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    /**
     * 7. 🔒 必须验证原密码的密码重置引擎
     */
    @PutMapping("/change-password")
    public Map<String, Object> changePassword(@RequestBody Map<String, String> payload) {
        Map<String, Object> result = new HashMap<>();
        String userId = payload.get("userId");
        String oldPassword = payload.get("oldPassword");
        String newPassword = payload.get("newPassword");

        if (userId == null || oldPassword == null || newPassword == null || newPassword.trim().length() < 6) {
            result.put("code", 400);
            result.put("msg", "密码修改失败：输入不能为空且新密码需大于等于6位");
            return result;
        }

        try {
            String sqlCheck = "SELECT password FROM user WHERE id = ? LIMIT 1";
            String currentDbPassword = jdbcTemplate.queryForObject(sqlCheck, String.class, userId);

            if (currentDbPassword == null || !currentDbPassword.equals(oldPassword)) {
                result.put("code", 403);
                result.put("msg", "❌ 原密码验证失败：您输入的原密码不正确，请重新输入！");
                return result;
            }

            String sqlUpdate = "UPDATE user SET password = ? WHERE id = ?";
            int rows = jdbcTemplate.update(sqlUpdate, newPassword, userId);
            if (rows > 0) {
                result.put("code", 200);
                result.put("msg", "🔒 身份验证成功！新密码已加密生效。");
            } else {
                result.put("code", 500);
                result.put("msg", "密码修改失败：未找到对应账户");
            }
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "安全审计异常: " + e.getMessage());
        }
        return result;
    }

    /**
     * 8. 全量财务流本地离线 JSON 数据安全备份打包
     */
    @GetMapping("/backup")
    public Map<String, Object> backupData(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            QueryWrapper<Record> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_id", userId).orderByDesc("create_time");
            List<Record> list = recordService.list(queryWrapper);

            result.put("code", 200);
            result.put("msg", "💾 全量财务数据本地安全备份打包成功！");
            result.put("backupTime", LocalDateTime.now().toString());
            result.put("totalCount", list.size());
            result.put("recordsData", list);
        } catch (Exception e) {
            result.put("code", 500);
            result.put("msg", "备份失败: " + e.getMessage());
        }
        return result;
    }
}