package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;

@Data
@TableName("account")
public class Account {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private String name;
    private String type;
    private BigDecimal balance; // 余额
}