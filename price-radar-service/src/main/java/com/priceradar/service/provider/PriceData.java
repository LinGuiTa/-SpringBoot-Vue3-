package com.priceradar.service.provider;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class PriceData {
    private Long platformId;
    private BigDecimal price;
    private BigDecimal originalPrice;
    private String discountInfo;
    private Integer stockStatus;
    private String url;
}
