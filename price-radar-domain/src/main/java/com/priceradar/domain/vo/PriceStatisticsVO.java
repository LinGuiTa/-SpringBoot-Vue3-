package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PriceStatisticsVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private BigDecimal minPrice;

    private BigDecimal maxPrice;

    private BigDecimal avgPrice;

    private BigDecimal currentPrice;

    private BigDecimal priceRange;

    private String trend;

    private String trendDescription;
}
