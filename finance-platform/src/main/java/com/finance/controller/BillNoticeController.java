package com.finance.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.finance.entity.BillNotice;
import com.finance.service.BillNoticeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/bill")
@CrossOrigin // 💡 放行前端网页跨域
public class BillNoticeController {

    @Autowired
    private BillNoticeService billNoticeService;

    /**
     * 新增还款提醒项
     */
    @PostMapping("/add")
    public Map<String, Object> addNotice(@RequestBody BillNotice notice) {
        Map<String, Object> result = new HashMap<>();
        if (notice.getIsReminded() == null) {
            notice.setIsReminded(0);
        }
        boolean saved = billNoticeService.save(notice);
        result.put("code", saved ? 200 : 500);
        result.put("msg", saved ? "账单提醒设置成功！" : "设置失败");
        return result;
    }

    /**
     * 查询个人的全部提醒明细
     */
    @GetMapping("/list")
    public Map<String, Object> getList(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<BillNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillNotice::getUserId, userId).orderByAsc(BillNotice::getDueDate);
        result.put("code", 200);
        result.put("data", billNoticeService.list(wrapper));
        return result;
    }

    /**
     * 智能雷达扫描：检查 3 天内即将到期的待缴账单
     */
    @GetMapping("/check-remind")
    public Map<String, Object> checkRemind(@RequestParam Integer userId) {
        Map<String, Object> result = new HashMap<>();
        LambdaQueryWrapper<BillNotice> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(BillNotice::getUserId, userId).eq(BillNotice::getIsReminded, 0);
        List<BillNotice> allNotices = billNoticeService.list(wrapper);
        List<String> notifications = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (BillNotice notice : allNotices) {
            try {
                LocalDate dueDate = LocalDate.parse(notice.getDueDate());
                if (!dueDate.isAfter(today.plusDays(3))) {
                    notifications.add("🚨 【到期提醒】您的账单「" + notice.getTitle() + "」将于 " + notice.getDueDate() + " 到期，金额：" + notice.getAmount() + "元！");
                    notice.setIsReminded(1); // 标记为已完成前台弹窗通知
                    billNoticeService.updateById(notice);
                }
            } catch (Exception e) {
                // 日期解析异常则忽略
            }
        }
        result.put("code", 200);
        result.put("notifications", notifications);
        return result;
    }
}