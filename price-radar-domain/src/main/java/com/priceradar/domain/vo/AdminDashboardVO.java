package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class AdminDashboardVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long totalUsers;
    private Long totalProducts;
    private Long activeAlerts;
    private Long unreadNotifications;
}
