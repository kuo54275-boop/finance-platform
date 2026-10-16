package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("budget")
public class Budget {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String category;
    private BigDecimal amount;
    private String month; // 格式如：2026-07
}