package com.priceradar.service.provider.impl;

import com.priceradar.service.provider.PriceData;
import com.priceradar.service.provider.PriceProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class PinduoduoPriceProvider implements PriceProvider {

    private static final Random RANDOM = new Random();

    @Override
    public Long getPlatformId() {
        return 3L;
    }

    @Override
    public PriceData fetchPrice(String barcode, BigDecimal basePrice) {
        PriceData data = new PriceData();
        data.setPlatformId(3L);
        double factor = 0.92 + RANDOM.nextDouble() * 0.07;
        BigDecimal price = basePrice.multiply(BigDecimal.valueOf(factor)).setScale(2, RoundingMode.HALF_UP);
        data.setPrice(price);
        data.setOriginalPrice(basePrice.multiply(BigDecimal.valueOf(1.05)).setScale(2, RoundingMode.HALF_UP));
        String[] discounts = {"拼团价", "百亿补贴", "限时秒杀", ""};
        data.setDiscountInfo(discounts[RANDOM.nextInt(discounts.length)]);
        data.setStockStatus(1);
        data.setUrl("https://mobile.yangkeduo.com/search_result.html?search_key=" + barcode);
        return data;
    }
}
