package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.priceradar.domain.entity.Notification;
import com.priceradar.mapper.NotificationMapper;
import com.priceradar.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationServiceImpl implements NotificationService {

    @Autowired
    private NotificationMapper notificationMapper;

    @Override
    public IPage<Notification> getNotifications(Long userId, Integer page, Integer size) {
        return notificationMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at"));
    }

    @Override
    public void markRead(Long userId, Long notificationId) {
        Notification update = new Notification();
        update.setId(notificationId);
        update.setIsRead(true);
        notificationMapper.update(update,
                new QueryWrapper<Notification>()
                        .eq("id", notificationId)
                        .eq("user_id", userId));
    }

    @Override
    public void markAllRead(Long userId) {
        Notification update = new Notification();
        update.setIsRead(true);
        notificationMapper.update(update,
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", false));
    }

    @Override
    public long getUnreadCount(Long userId) {
        return notificationMapper.selectCount(
                new QueryWrapper<Notification>()
                        .eq("user_id", userId)
                        .eq("is_read", false));
    }
}
