package com.finance.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("record")
public class Record {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer userId;
    private Integer accountId; // 💡 新增这一行，对应刚刚追加的数据库字段
    private String type;
    private String category;
    private BigDecimal amount; // Java中处理货币金额用 BigDecimal 最精准
    private String remark;
    private LocalDateTime createTime;
}