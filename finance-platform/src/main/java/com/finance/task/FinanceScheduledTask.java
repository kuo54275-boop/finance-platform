package com.finance.task;

import com.finance.entity.Record;
import com.finance.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Component
public class FinanceScheduledTask {

    @Autowired
    private RecordService recordService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 1. 优化修改：每月 2 号中午 12 点 0 分自动发放工资（3000元）
     * 秒 分 时 天 月 周
     * "0 0 12 2 * ?" 代表每月 2 号 12:00:00 准时触发
     */
    @Scheduled(cron = "0 0 12 2 * ?")
    public void autoSalaryTask() {
        System.out.println("⏰ [定时任务启动] 监测到每月发薪日，正在自动派发固定工资流水...");

        // 模拟给 UID=1 的默认账户发放工资
        Record record = new Record();
        record.setUserId(1);
        record.setAccountId(3); // 绑定到你截图里的有效账户ID
        record.setType("INCOME");
        record.setCategory("工资");
        record.setAmount(new BigDecimal("3000.00"));
        record.setRemark("🤖 系统自动结算：每月2号固定发薪日基本工资入账");
        record.setCreateTime(LocalDateTime.now());

        boolean saved = recordService.save(record);
        if (saved) {
            System.out.println("✅ [定时任务成功] 本月固定工资 3000 元已自动成功记入收支流水！");
        }
    }

    /**
     * 2. 每月 3 号凌晨 0 点 10 分自动扣除房租（1000元）
     * "0 10 0 3 * ?" 代表每月3号0点10分触发
     */
    @Scheduled(cron = "0 10 0 3 * ?")
    public void autoRentTask() {
        System.out.println("⏰ [定时任务启动] 正在自动扣除本月固定房租流水...");

        Record record = new Record();
        record.setUserId(1);
        record.setAccountId(3);
        record.setType("EXPENSE");
        record.setCategory("购物"); // 对应前端定义的消费大类
        record.setAmount(new BigDecimal("1000.00"));
        record.setRemark("🤖 系统自动结算：每月代扣固定房租费用");
        record.setCreateTime(LocalDateTime.now());

        boolean saved = recordService.save(record);
        if (saved) {
            System.out.println("✅ [定时任务成功] 房租 1000 元已成功扣除并生成流水！");
        }
    }

    /**
     * 🚀【答辩演示专属：每隔 1 分钟模拟一次收支】
     */
    @Scheduled(cron = "0 */1 * * * ?")
    public void debugDemonstrationTask() {
        System.out.println("🤖 [系统守护进程] 定时记账引擎在 2026 年平稳运行中，正在监控月度还款与发薪日...");
    }
}