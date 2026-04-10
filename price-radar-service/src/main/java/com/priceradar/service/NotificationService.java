package com.priceradar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.domain.entity.Notification;

public interface NotificationService {
    IPage<Notification> getNotifications(Long userId, Integer page, Integer size);
    void markRead(Long userId, Long notificationId);
    void markAllRead(Long userId);
    long getUnreadCount(Long userId);
}
