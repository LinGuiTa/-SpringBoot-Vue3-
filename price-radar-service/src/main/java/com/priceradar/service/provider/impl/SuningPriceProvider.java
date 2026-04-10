package com.priceradar.service.provider.impl;

import com.priceradar.service.provider.PriceData;
import com.priceradar.service.provider.PriceProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class SuningPriceProvider implements PriceProvider {

    private static final Random RANDOM = new Random();

    @Override
    public Long getPlatformId() {
        return 4L;
    }

    @Override
    public PriceData fetchPrice(String barcode, BigDecimal basePrice) {
        PriceData data = new PriceData();
        data.setPlatformId(4L);
        double factor = 0.99 + RANDOM.nextDouble() * 0.04;
        BigDecimal price = basePrice.multiply(BigDecimal.valueOf(factor)).setScale(2, RoundingMode.HALF_UP);
        data.setPrice(price);
        data.setOriginalPrice(basePrice.multiply(BigDecimal.valueOf(1.12)).setScale(2, RoundingMode.HALF_UP));
        String[] discounts = {"满299减30", "限时特价", "会员专享价", ""};
        data.setDiscountInfo(discounts[RANDOM.nextInt(discounts.length)]);
        data.setStockStatus(1);
        data.setUrl("https://search.suning.com/" + barcode + "/");
        return data;
    }
}
