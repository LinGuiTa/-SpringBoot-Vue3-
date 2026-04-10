package com.priceradar.service.provider;

import java.math.BigDecimal;

public interface PriceProvider {
    Long getPlatformId();
    PriceData fetchPrice(String barcode, BigDecimal basePrice);
}
