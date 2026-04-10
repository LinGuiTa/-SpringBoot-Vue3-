package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PriceCompareVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long platformId;

    private String platformName;

    private String platformLogoUrl;

    private BigDecimal price;

    private BigDecimal originalPrice;

    private String discountInfo;

    private Integer stockStatus;

    private String url;

    private Boolean isLowest;
}
