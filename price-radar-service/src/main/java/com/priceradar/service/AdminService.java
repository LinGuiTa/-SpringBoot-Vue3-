package com.priceradar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.domain.entity.User;
import com.priceradar.domain.vo.AdminDashboardVO;

public interface AdminService {
    AdminDashboardVO getDashboard();
    IPage<User> getUsers(String keyword, Integer page, Integer size);
    void updateUserStatus(Long userId, Integer status);
    void updateUserRole(Long userId, String role);
    void refreshAllPrices();
}
