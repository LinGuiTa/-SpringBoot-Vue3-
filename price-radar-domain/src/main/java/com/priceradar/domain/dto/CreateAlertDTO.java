package com.priceradar.domain.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class CreateAlertDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull(message = "商品ID不能为空")
    private Long productId;

    private Long platformId;

    @NotNull(message = "目标价格不能为空")
    private BigDecimal targetPrice;

    private String alertType = "ONCE";
}
