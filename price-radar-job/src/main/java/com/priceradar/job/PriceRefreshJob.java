package com.priceradar.job;

import com.priceradar.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class PriceRefreshJob {

    @Autowired
    private PriceService priceService;

    @Scheduled(fixedDelay = 1800000)
    public void refreshHotProducts() {
        priceService.refreshAllHotProducts();
    }
}
