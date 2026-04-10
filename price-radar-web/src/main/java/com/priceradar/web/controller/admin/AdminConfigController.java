package com.priceradar.web.controller.admin;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.priceradar.common.result.Result;
import com.priceradar.domain.entity.SystemConfig;
import com.priceradar.mapper.SystemConfigMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/config")
@PreAuthorize("hasRole('ADMIN')")
public class AdminConfigController {

    @Autowired
    private SystemConfigMapper systemConfigMapper;

    @GetMapping
    public Result<List<SystemConfig>> list() {
        return Result.success(systemConfigMapper.selectList(null));
    }

    @PutMapping("/{key}")
    public Result<Void> update(@PathVariable String key, @RequestParam String value) {
        SystemConfig config = systemConfigMapper.selectOne(
                new QueryWrapper<SystemConfig>().eq("config_key", key));
        if (config != null) {
            config.setConfigValue(value);
            systemConfigMapper.updateById(config);
        }
        return Result.success();
    }
}
