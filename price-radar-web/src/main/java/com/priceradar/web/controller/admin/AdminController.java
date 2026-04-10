package com.priceradar.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.common.result.PageResult;
import com.priceradar.common.result.Result;
import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.entity.PriceLatest;
import com.priceradar.domain.entity.User;
import com.priceradar.domain.vo.AdminDashboardVO;
import com.priceradar.mapper.PlatformMapper;
import com.priceradar.mapper.PriceLatestMapper;
import com.priceradar.service.AdminService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private AdminService adminService;

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private PriceLatestMapper priceLatestMapper;

    @GetMapping("/dashboard")
    public Result<AdminDashboardVO> dashboard() {
        return Result.success(adminService.getDashboard());
    }

    @GetMapping("/users")
    public Result<PageResult<User>> users(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<User> iPage = adminService.getUsers(keyword, page, size);
        return Result.success(PageResult.of(iPage));
    }

    @PutMapping("/users/{id}/status")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        adminService.updateUserStatus(id, status);
        return Result.success();
    }

    @PutMapping("/users/{id}/role")
    public Result<Void> updateUserRole(@PathVariable Long id, @RequestParam String role) {
        adminService.updateUserRole(id, role);
        return Result.success();
    }

    @PostMapping("/prices/batch-refresh")
    public Result<Void> batchRefresh() {
        adminService.refreshAllPrices();
        return Result.success();
    }

    @GetMapping("/prices/monitor")
    public Result<Map<String, Object>> priceMonitor() {
        List<Platform> platforms = platformMapper.selectList(
                new QueryWrapper<Platform>().eq("status", 1).orderByAsc("sort_order"));
        Map<String, Object> data = new HashMap<>();
        data.put("platforms", platforms);
        Map<Long, Object> lastUpdateMap = new HashMap<>();
        for (Platform platform : platforms) {
            PriceLatest latest = priceLatestMapper.selectOne(
                    new QueryWrapper<PriceLatest>()
                            .eq("platform_id", platform.getId())
                            .orderByDesc("updated_at")
                            .last("LIMIT 1"));
            lastUpdateMap.put(platform.getId(), latest != null ? latest.getUpdatedAt() : null);
        }
        data.put("lastUpdateMap", lastUpdateMap);
        return Result.success(data);
    }
}
