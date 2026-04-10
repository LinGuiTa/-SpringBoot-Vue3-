package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class FavoriteVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long favoriteId;
    private Long productId;
    private String productName;
    private String productImage;
    private String brand;
    private BigDecimal lowestPrice;
    private String lowestPlatformName;
    private LocalDateTime favoritedAt;
}
