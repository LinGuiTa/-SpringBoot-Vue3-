package com.priceradar.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("t_price_alert")
public class PriceAlert implements Serializable {

    private static final long serialVersionUID = 1L;

    @TableId(type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long productId;

    private Long platformId;

    private BigDecimal targetPrice;

    private String alertType;

    private String status;

    private LocalDateTime triggeredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
