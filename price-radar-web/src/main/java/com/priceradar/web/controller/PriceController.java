package com.priceradar.web.controller;

import com.priceradar.common.result.Result;
import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.vo.PriceCompareVO;
import com.priceradar.domain.vo.PriceHistoryVO;
import com.priceradar.domain.vo.PriceStatisticsVO;
import com.priceradar.service.PriceHistoryService;
import com.priceradar.service.PriceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/prices")
public class PriceController {

    @Autowired
    private PriceService priceService;

    @Autowired
    private PriceHistoryService priceHistoryService;

    @GetMapping("/compare/{productId}")
    public Result<List<PriceCompareVO>> compare(@PathVariable Long productId) {
        return Result.success(priceService.comparePrices(productId));
    }

    @GetMapping("/lowest/{productId}")
    public Result<PriceCompareVO> lowest(@PathVariable Long productId) {
        return Result.success(priceService.getLowestPrice(productId));
    }

    @GetMapping("/platforms")
    public Result<List<Platform>> platforms() {
        return Result.success(priceService.getAllPlatforms());
    }

    @PostMapping("/refresh/{productId}")
    public Result<Void> refresh(@PathVariable Long productId) {
        priceService.refreshPrices(productId);
        return Result.success();
    }

    @GetMapping("/history/{productId}")
    public Result<List<PriceHistoryVO>> history(
            @PathVariable Long productId,
            @RequestParam(required = false) Long platformId,
            @RequestParam(defaultValue = "30") Integer days) {
        return Result.success(priceHistoryService.getHistory(productId, platformId, days));
    }

    @GetMapping("/history/{productId}/statistics")
    public Result<PriceStatisticsVO> statistics(
            @PathVariable Long productId,
            @RequestParam(required = false) Long platformId) {
        return Result.success(priceHistoryService.getStatistics(productId, platformId));
    }

    @GetMapping("/history/{productId}/trend")
    public Result<Map<String, String>> trend(@PathVariable Long productId) {
        return Result.success(priceHistoryService.getTrend(productId));
    }

    @GetMapping("/history/{productId}/best-time")
    public Result<Map<String, Object>> bestTime(@PathVariable Long productId) {
        return Result.success(priceHistoryService.getBestBuyTime(productId));
    }
}
