package com.priceradar.web.controller;

import com.priceradar.common.result.Result;
import com.priceradar.domain.dto.CreateAlertDTO;
import com.priceradar.domain.entity.PriceAlert;
import com.priceradar.service.AlertService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/alerts")
public class AlertController {

    @Autowired
    private AlertService alertService;

    @PostMapping
    public Result<Void> create(@RequestBody @Valid CreateAlertDTO dto, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        alertService.createAlert(userId, dto);
        return Result.success();
    }

    @DeleteMapping("/{alertId}")
    public Result<Void> delete(@PathVariable Long alertId, HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        alertService.deleteAlert(userId, alertId);
        return Result.success();
    }

    @GetMapping
    public Result<List<PriceAlert>> list(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return Result.success(alertService.getAlerts(userId));
    }

    @PutMapping("/{alertId}")
    public Result<Void> update(@PathVariable Long alertId,
                               @RequestParam BigDecimal targetPrice,
                               HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        alertService.updateAlert(userId, alertId, targetPrice);
        return Result.success();
    }
}
