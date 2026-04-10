package com.priceradar.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonProperty;
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

    /**
     * Business status: ACTIVE/TRIGGERED/CANCELLED...
     *
     * NOTE: We intentionally do NOT name this field "status" to avoid triggering
     * MyBatis-Plus global logic-delete config (logic-delete-field=status).
     */
    @TableField("status")
    @JsonProperty("status")
    private String alertStatus;

    private LocalDateTime triggeredAt;

    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdAt;
}
