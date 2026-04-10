package com.priceradar.service;

import com.priceradar.domain.dto.CreateAlertDTO;
import com.priceradar.domain.entity.PriceAlert;

import java.math.BigDecimal;
import java.util.List;

public interface AlertService {
    void createAlert(Long userId, CreateAlertDTO dto);
    void deleteAlert(Long userId, Long alertId);
    List<PriceAlert> getAlerts(Long userId);
    void updateAlert(Long userId, Long alertId, BigDecimal targetPrice);
    void checkAndTriggerAlerts();
}
