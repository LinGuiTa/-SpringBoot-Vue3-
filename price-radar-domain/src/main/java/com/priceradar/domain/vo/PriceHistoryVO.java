package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

@Data
public class PriceHistoryVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String date;

    private Long platformId;

    private String platformName;

    private BigDecimal price;
}
