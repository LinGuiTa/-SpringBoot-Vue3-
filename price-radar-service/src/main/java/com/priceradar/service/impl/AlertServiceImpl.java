package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.priceradar.common.exception.BusinessException;
import com.priceradar.domain.dto.CreateAlertDTO;
import com.priceradar.domain.entity.Notification;
import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.entity.PriceAlert;
import com.priceradar.domain.entity.PriceLatest;
import com.priceradar.domain.entity.Product;
import com.priceradar.mapper.NotificationMapper;
import com.priceradar.mapper.PlatformMapper;
import com.priceradar.mapper.PriceAlertMapper;
import com.priceradar.mapper.PriceLatestMapper;
import com.priceradar.mapper.ProductMapper;
import com.priceradar.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;

@Service
public class AlertServiceImpl implements AlertService {

    @Autowired
    private PriceAlertMapper priceAlertMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private PriceLatestMapper priceLatestMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PlatformMapper platformMapper;

    @Override
    public void createAlert(Long userId, CreateAlertDTO dto) {
        PriceAlert alert = new PriceAlert();
        alert.setUserId(userId);
        alert.setProductId(dto.getProductId());
        alert.setPlatformId(dto.getPlatformId());
        alert.setTargetPrice(dto.getTargetPrice());
        alert.setAlertType(dto.getAlertType() != null ? dto.getAlertType() : "ONCE");
        alert.setAlertStatus("ACTIVE");
        priceAlertMapper.insert(alert);
    }

    @Override
    public void deleteAlert(Long userId, Long alertId) {
        PriceAlert alert = priceAlertMapper.selectById(alertId);
        if (alert == null || !alert.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此提醒");
        }
        priceAlertMapper.deleteById(alertId);
    }

    @Override
    public List<PriceAlert> getAlerts(Long userId) {
        return priceAlertMapper.selectList(
                new QueryWrapper<PriceAlert>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at"));
    }

    @Override
    public void updateAlert(Long userId, Long alertId, BigDecimal targetPrice) {
        PriceAlert alert = priceAlertMapper.selectById(alertId);
        if (alert == null || !alert.getUserId().equals(userId)) {
            throw new BusinessException(403, "无权操作此提醒");
        }
        PriceAlert update = new PriceAlert();
        update.setId(alertId);
        update.setTargetPrice(targetPrice);
        priceAlertMapper.updateById(update);
    }

    @Override
    public void checkAndTriggerAlerts() {
        List<PriceAlert> activeAlerts = priceAlertMapper.selectList(
                new QueryWrapper<PriceAlert>().eq("status", "ACTIVE"));
        for (PriceAlert alert : activeAlerts) {
            try {
                PriceLatest lowest;
                if (alert.getPlatformId() != null) {
                    lowest = priceLatestMapper.selectOne(
                            new QueryWrapper<PriceLatest>()
                                    .eq("product_id", alert.getProductId())
                                    .eq("platform_id", alert.getPlatformId()));
                } else {
                    List<PriceLatest> priceList = priceLatestMapper.selectList(
                            new QueryWrapper<PriceLatest>().eq("product_id", alert.getProductId()));
                    if (priceList == null || priceList.isEmpty()) {
                        continue;
                    }
                    lowest = priceList.stream()
                            .min(Comparator.comparing(PriceLatest::getPrice))
                            .orElse(null);
                }
                if (lowest == null) {
                    continue;
                }
                if (lowest.getPrice().compareTo(alert.getTargetPrice()) <= 0) {
                    Product product = productMapper.selectById(alert.getProductId());
                    String productName = product != null ? product.getName() : "未知商品";
                    Platform platform = platformMapper.selectById(lowest.getPlatformId());
                    String platformName = platform != null ? platform.getName() : "未知平台";

                    Notification notification = new Notification();
                    notification.setUserId(alert.getUserId());
                    notification.setType("PRICE_DROP");
                    notification.setTitle("商品降价提醒");
                    notification.setContent("您关注的" + productName + "在" + platformName
                            + "降至" + lowest.getPrice() + "元，已达到您设置的目标价"
                            + alert.getTargetPrice() + "元");
                    notification.setProductId(alert.getProductId());
                    notification.setIsRead(false);
                    notificationMapper.insert(notification);

                    if ("ONCE".equals(alert.getAlertType())) {
                        PriceAlert update = new PriceAlert();
                        update.setId(alert.getId());
                        update.setAlertStatus("TRIGGERED");
                        update.setTriggeredAt(LocalDateTime.now());
                        priceAlertMapper.updateById(update);
                    }
                }
            } catch (Exception ignored) {
            }
        }
    }
}
