package com.finance.controller;

import com.finance.entity.Budget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/budget")
@CrossOrigin // 放行前端跨域请求
public class BudgetController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 1. 设置/更新分类预算限额
     */
    @PostMapping("/set")
    public Map<String, Object> setBudget(@RequestBody Budget budget) {
        Map<String, Object> result = new HashMap<>();

        // 检查这个月该分类是否已经设置过预算
        String checkSql = "SELECT id FROM budget WHERE user_id = ? AND category = ? AND month = ? LIMIT 1";
        List<Integer> ids = jdbcTemplate.queryForList(checkSql, Integer.class, budget.getUserId(), budget.getCategory(), budget.getMonth());

        if (!ids.isEmpty()) {
            // 已存在预算记录则执行更新
            String updateSql = "UPDATE budget SET amount = ? WHERE id = ?";
            jdbcTemplate.update(updateSql, budget.getAmount(), ids.get(0));
        } else {
            // 不存在预算记录则执行新插入
            String insertSql = "INSERT INTO budget (user_id, category, amount, month) VALUES (?, ?, ?, ?)";
            jdbcTemplate.update(insertSql, budget.getUserId(), budget.getCategory(), budget.getAmount(), budget.getMonth());
        }

        result.put("code", 200);
        result.put("msg", "分类预警限额配置成功！");
        return result;
    }

    /**
     * 2. 查看预算执行进度（动态计算已用与剩余）
     */
    @GetMapping("/progress")
    public Map<String, Object> getBudgetProgress(@RequestParam Integer userId, @RequestParam String month) {
        Map<String, Object> result = new HashMap<>();

        // 查出该用户当月设置的所有预算限额
        String sqlBudgets = "SELECT category, amount FROM budget WHERE user_id = ? AND month = ?";
        List<Map<String, Object>> budgetList = jdbcTemplate.queryForList(sqlBudgets, userId, month);

        List<Map<String, Object>> progressRows = new ArrayList<>();

        for (Map<String, Object> b : budgetList) {
            // 💡 修复：使用标准的 .get("字段名") 获取 Map 中的元素
            String category = (String) b.get("category");
            double limitAmount = ((Number) b.get("amount")).doubleValue();

            // 动态计算该分类下的当月总支出流水
            String sqlSpent = "SELECT SUM(amount) FROM record WHERE user_id = ? AND category = ? AND type = 'EXPENSE' AND strftime('%Y-%m', create_time) = ?";
            Double totalSpent = jdbcTemplate.queryForObject(sqlSpent, Double.class, userId, category, month);
            totalSpent = (totalSpent == null) ? 0.0 : totalSpent;

            double remaining = limitAmount - totalSpent;

            // 计算前端进度条百分比
            double percent = (totalSpent / limitAmount) * 100;
            if (percent > 100) percent = 100;

            Map<String, Object> row = new HashMap<>();
            // 💡 修复：Java 中的 Map 必须使用 .put("键", 值) 方法进行标准赋值
            row.put("category", category);
            row.put("limitAmount", limitAmount);
            row.put("spent", totalSpent);
            row.put("remaining", remaining);
            row.put("percent", Math.round(percent));

            progressRows.add(row);
        }

        result.put("code", 200);
        result.put("data", progressRows);
        return result;
    }
}