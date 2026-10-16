package com.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.BillNotice;
import com.finance.entity.Record;
import com.finance.service.BillNoticeService;
import com.finance.service.RecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bill")
@CrossOrigin // 💡 彻底放行前端跨域
public class BillController {

    @Autowired
    private BillNoticeService billNoticeService; // 💡 严格对齐真实的 BillNoticeService

    @Autowired
    private RecordService recordService;

    /**
     * 1. 设定/加入账单监测库
     */
    @PostMapping("/add")
    public Map<String, Object> addBill(@RequestBody BillNotice bill) {
        Map<String, Object> result = new HashMap<>();
        bill.setIsReminded(0); // 兜底给个初始状态
        boolean saved = billNoticeService.save(bill);
        result.put("code", saved ? 200 : 500);
        result.put("msg", saved ? "账单提醒任务加入成功！" : "添加失败");
        return result;
    }

    /**
     * 2. 查询并智能同步近三个月的房租账单状态
     */
    @GetMapping("/list")
    public Map<String, Object> getBillList(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();

        // 2.1 获取用户手动创建的所有提醒
        LambdaQueryWrapper<BillNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillNotice::getUserId, userId);
        wrapper.orderByAsc(BillNotice::getDueDate);
        List<BillNotice> list = billNoticeService.list(wrapper);

        // 2.2 自动动态生成/同步近三个月的房租账单
        LocalDate today = LocalDate.now();
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (int i = -2; i <= 0; i++) {
            LocalDate targetMonthDate = today.plusMonths(i);
            String targetDueDateStr = LocalDate.of(targetMonthDate.getYear(), targetMonthDate.getMonth(), 3).format(dtf);

            String currentTargetDate = targetDueDateStr;
            boolean exists = list.stream().anyMatch(b -> "房租缴费".equals(b.getTitle()) && currentTargetDate.equals(b.getDueDate()));
            if (!exists) {
                BillNotice autoRentBill = new BillNotice();
                autoRentBill.setUserId(userId);
                autoRentBill.setTitle("房租缴费");
                autoRentBill.setAmount(new java.math.BigDecimal("1000.00"));
                autoRentBill.setDueDate(targetDueDateStr);
                autoRentBill.setIsReminded(0);
                billNoticeService.save(autoRentBill);
            }
        }

        // 2.3 重新拉取最新的完整列表并校验状态
        list = billNoticeService.list(wrapper);
        List<Map<String, Object>> enrichedList = new ArrayList<>();

        for (BillNotice b : list) {
            Map<String, Object> map = new HashMap<>();
            map.put("id", b.getId());
            map.put("title", b.getTitle());
            map.put("amount", b.getAmount());
            map.put("dueDate", b.getDueDate());

            // 💡 智能优化：支持流水备注模糊匹配账单标题
            LambdaQueryWrapper<Record> recordWrapper = new LambdaQueryWrapper<>();
            recordWrapper.eq(Record::getUserId, userId)
                    .and(qr -> qr.like(Record::getRemark, b.getTitle())
                            .or()
                            .like(Record::getRemark, "房租"));

            // 💡 完美修正：long count 接收结果，绝不会类型冲突
            long count = recordService.count(recordWrapper);

            if (count > 0) {
                map.put("status", "已缴费");
            } else {
                LocalDate dueDate = LocalDate.parse(b.getDueDate());
                if (today.isAfter(dueDate)) {
                    map.put("status", "已超期");
                } else {
                    map.put("status", "待缴费");
                }
            }
            enrichedList.add(map);
        }

        result.put("code", 200);
        result.put("data", enrichedList);
        return result;
    }

    /**
     * 3. 删除/取消账单提醒任务
     */
    @DeleteMapping("/delete/{id}")
    public Map<String, Object> deleteBill(@PathVariable Integer id) {
        Map<String, Object> result = new HashMap<>();
        boolean removed = billNoticeService.removeById(id);
        result.put("code", removed ? 200 : 500);
        result.put("msg", removed ? "账单提醒取消成功！" : "删除失败");
        return result;
    }
}