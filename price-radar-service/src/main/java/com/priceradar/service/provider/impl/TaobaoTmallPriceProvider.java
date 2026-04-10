package com.priceradar.service.provider.impl;

import com.priceradar.service.provider.PriceData;
import com.priceradar.service.provider.PriceProvider;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

@Component
public class TaobaoTmallPriceProvider implements PriceProvider {

    private static final Random RANDOM = new Random();

    @Override
    public Long getPlatformId() {
        return 2L;
    }

    @Override
    public PriceData fetchPrice(String barcode, BigDecimal basePrice) {
        PriceData data = new PriceData();
        data.setPlatformId(2L);
        double factor = 0.95 + RANDOM.nextDouble() * 0.06;
        BigDecimal price = basePrice.multiply(BigDecimal.valueOf(factor)).setScale(2, RoundingMode.HALF_UP);
        data.setPrice(price);
        data.setOriginalPrice(basePrice.multiply(BigDecimal.valueOf(1.15)).setScale(2, RoundingMode.HALF_UP));
        String[] discounts = {"满299减30", "限时特价", "会员专享价", ""};
        data.setDiscountInfo(discounts[RANDOM.nextInt(discounts.length)]);
        data.setStockStatus(1);
        data.setUrl("https://s.taobao.com/search?q=" + barcode);
        return data;
    }
}
