package com.priceradar.job;

import com.priceradar.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AlertCheckJob {

    @Autowired
    private AlertService alertService;

    @Scheduled(fixedDelay = 1800000)
    public void checkAlerts() {
        alertService.checkAndTriggerAlerts();
    }
}
