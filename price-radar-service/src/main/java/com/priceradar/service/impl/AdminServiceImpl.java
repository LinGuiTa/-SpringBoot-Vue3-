package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.priceradar.domain.entity.Notification;
import com.priceradar.domain.entity.PriceAlert;
import com.priceradar.domain.entity.Product;
import com.priceradar.domain.entity.User;
import com.priceradar.domain.vo.AdminDashboardVO;
import com.priceradar.mapper.NotificationMapper;
import com.priceradar.mapper.PriceAlertMapper;
import com.priceradar.mapper.ProductMapper;
import com.priceradar.mapper.UserMapper;
import com.priceradar.service.AdminService;
import com.priceradar.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AdminServiceImpl implements AdminService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PriceAlertMapper priceAlertMapper;

    @Autowired
    private NotificationMapper notificationMapper;

    @Autowired
    private PriceService priceService;

    @Override
    public AdminDashboardVO getDashboard() {
        AdminDashboardVO vo = new AdminDashboardVO();
        vo.setTotalUsers(userMapper.selectCount(new QueryWrapper<User>().eq("status", 1)));
        vo.setTotalProducts(productMapper.selectCount(new QueryWrapper<Product>().eq("status", 1)));
        vo.setActiveAlerts(priceAlertMapper.selectCount(new QueryWrapper<PriceAlert>().eq("status", "ACTIVE")));
        vo.setUnreadNotifications(notificationMapper.selectCount(new QueryWrapper<Notification>().eq("is_read", false)));
        return vo;
    }

    @Override
    public IPage<User> getUsers(String keyword, Integer page, Integer size) {
        QueryWrapper<User> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("username", keyword).or().like("email", keyword));
        }
        return userMapper.selectPage(new Page<>(page, size), wrapper);
    }

    @Override
    public void updateUserStatus(Long userId, Integer status) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setStatus(status);
            userMapper.updateById(user);
        }
    }

    @Override
    public void updateUserRole(Long userId, String role) {
        User user = userMapper.selectById(userId);
        if (user != null) {
            user.setRole(role);
            userMapper.updateById(user);
        }
    }

    @Override
    public void refreshAllPrices() {
        priceService.refreshAllHotProducts();
    }
}
