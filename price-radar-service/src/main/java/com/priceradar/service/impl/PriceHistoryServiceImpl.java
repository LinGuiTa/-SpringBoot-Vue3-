package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.entity.PriceLatest;
import com.priceradar.domain.entity.PriceRecord;
import com.priceradar.domain.vo.PriceHistoryVO;
import com.priceradar.domain.vo.PriceStatisticsVO;
import com.priceradar.mapper.PlatformMapper;
import com.priceradar.mapper.PriceLatestMapper;
import com.priceradar.mapper.PriceRecordMapper;
import com.priceradar.service.PriceHistoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PriceHistoryServiceImpl implements PriceHistoryService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    @Autowired
    private PriceRecordMapper priceRecordMapper;

    @Autowired
    private PriceLatestMapper priceLatestMapper;

    @Autowired
    private PlatformMapper platformMapper;

    @Override
    public List<PriceHistoryVO> getHistory(Long productId, Long platformId, Integer days) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<PriceHistoryVO> result = new ArrayList<>();

        List<Platform> platforms;
        if (platformId != null) {
            Platform p = platformMapper.selectById(platformId);
            platforms = p != null ? Collections.singletonList(p) : Collections.emptyList();
        } else {
            platforms = platformMapper.selectList(new QueryWrapper<Platform>().eq("status", 1));
        }

        for (Platform platform : platforms) {
            List<Map<String, Object>> rows = priceRecordMapper.selectDailyMinPrice(
                    productId, platform.getId(), startDate);
            for (Map<String, Object> row : rows) {
                PriceHistoryVO vo = new PriceHistoryVO();
                Object statDate = row.get("stat_date");
                vo.setDate(statDate != null ? statDate.toString() : null);
                vo.setPlatformId(platform.getId());
                vo.setPlatformName(platform.getName());
                Object minPrice = row.get("min_price");
                if (minPrice instanceof BigDecimal) {
                    vo.setPrice((BigDecimal) minPrice);
                } else if (minPrice != null) {
                    vo.setPrice(new BigDecimal(minPrice.toString()));
                }
                result.add(vo);
            }
        }

        result.sort(Comparator.comparing(PriceHistoryVO::getDate, Comparator.nullsLast(Comparator.naturalOrder())));
        return result;
    }

    @Override
    public PriceStatisticsVO getStatistics(Long productId, Long platformId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        QueryWrapper<PriceRecord> wrapper = new QueryWrapper<PriceRecord>()
                .eq("product_id", productId)
                .ge("recorded_at", startDate);
        if (platformId != null) {
            wrapper.eq("platform_id", platformId);
        }

        List<PriceRecord> records = priceRecordMapper.selectList(wrapper);

        PriceStatisticsVO vo = new PriceStatisticsVO();

        if (records == null || records.isEmpty()) {
            vo.setMinPrice(BigDecimal.ZERO);
            vo.setMaxPrice(BigDecimal.ZERO);
            vo.setAvgPrice(BigDecimal.ZERO);
            vo.setPriceRange(BigDecimal.ZERO);
            vo.setCurrentPrice(BigDecimal.ZERO);
            vo.setTrend("STABLE");
            vo.setTrendDescription("暂无历史数据");
            return vo;
        }

        BigDecimal minPrice = records.stream().map(PriceRecord::getPrice).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal maxPrice = records.stream().map(PriceRecord::getPrice).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        BigDecimal sum = records.stream().map(PriceRecord::getPrice).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal avgPrice = sum.divide(BigDecimal.valueOf(records.size()), 2, RoundingMode.HALF_UP);

        vo.setMinPrice(minPrice);
        vo.setMaxPrice(maxPrice);
        vo.setAvgPrice(avgPrice);
        vo.setPriceRange(maxPrice.subtract(minPrice));

        QueryWrapper<PriceLatest> latestWrapper = new QueryWrapper<PriceLatest>().eq("product_id", productId);
        if (platformId != null) {
            latestWrapper.eq("platform_id", platformId);
        }
        List<PriceLatest> latestList = priceLatestMapper.selectList(latestWrapper);
        BigDecimal currentPrice = latestList.stream()
                .map(PriceLatest::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);
        vo.setCurrentPrice(currentPrice);

        Map<String, String> trend = getTrend(productId);
        vo.setTrend(trend.get("trend"));
        vo.setTrendDescription(trend.get("trendDescription"));

        return vo;
    }

    @Override
    public Map<String, String> getTrend(Long productId) {
        LocalDateTime startDate = LocalDateTime.now().minusDays(30);

        List<Platform> platforms = platformMapper.selectList(new QueryWrapper<Platform>().eq("status", 1));

        Map<String, BigDecimal> dailyMinMap = new TreeMap<>();
        for (Platform platform : platforms) {
            List<Map<String, Object>> rows = priceRecordMapper.selectDailyMinPrice(productId, platform.getId(), startDate);
            for (Map<String, Object> row : rows) {
                Object statDate = row.get("stat_date");
                Object minPrice = row.get("min_price");
                if (statDate == null || minPrice == null) {
                    continue;
                }
                String dateStr = statDate.toString();
                BigDecimal price = minPrice instanceof BigDecimal
                        ? (BigDecimal) minPrice
                        : new BigDecimal(minPrice.toString());
                dailyMinMap.merge(dateStr, price, BigDecimal::min);
            }
        }

        List<BigDecimal> dailyPrices = new ArrayList<>(dailyMinMap.values());

        Map<String, String> result = new HashMap<>();
        if (dailyPrices.size() < 2) {
            result.put("trend", "STABLE");
            result.put("trendDescription", "近期价格较为稳定");
            return result;
        }

        int mid = dailyPrices.size() / 2;
        List<BigDecimal> firstHalf = dailyPrices.subList(0, mid);
        List<BigDecimal> secondHalf = dailyPrices.subList(mid, dailyPrices.size());

        BigDecimal firstAvg = firstHalf.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(firstHalf.size()), 2, RoundingMode.HALF_UP);
        BigDecimal secondAvg = secondHalf.stream().reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(secondHalf.size()), 2, RoundingMode.HALF_UP);

        if (firstAvg.compareTo(BigDecimal.ZERO) == 0) {
            result.put("trend", "STABLE");
            result.put("trendDescription", "近期价格较为稳定");
            return result;
        }

        BigDecimal threshold97 = firstAvg.multiply(BigDecimal.valueOf(0.97));
        BigDecimal threshold103 = firstAvg.multiply(BigDecimal.valueOf(1.03));

        if (secondAvg.compareTo(threshold97) < 0) {
            result.put("trend", "FALLING");
            result.put("trendDescription", "近期价格呈下降趋势，建议等待低点购买");
        } else if (secondAvg.compareTo(threshold103) > 0) {
            result.put("trend", "RISING");
            result.put("trendDescription", "近期价格呈上涨趋势，建议尽早购买");
        } else {
            result.put("trend", "STABLE");
            result.put("trendDescription", "近期价格较为稳定");
        }
        return result;
    }

    @Override
    public Map<String, Object> getBestBuyTime(Long productId) {
        List<PriceRecord> allRecords = priceRecordMapper.selectList(
                new QueryWrapper<PriceRecord>().eq("product_id", productId));

        Map<String, Object> result = new HashMap<>();

        BigDecimal historyLowestPrice = BigDecimal.ZERO;
        String historyLowestDate = null;

        if (allRecords != null && !allRecords.isEmpty()) {
            PriceRecord lowestRecord = allRecords.stream()
                    .min(Comparator.comparing(PriceRecord::getPrice))
                    .orElse(null);
            if (lowestRecord != null) {
                historyLowestPrice = lowestRecord.getPrice();
                historyLowestDate = lowestRecord.getRecordedAt() != null
                        ? lowestRecord.getRecordedAt().format(DATE_FORMATTER)
                        : null;
            }
        }

        List<PriceLatest> latestList = priceLatestMapper.selectList(
                new QueryWrapper<PriceLatest>().eq("product_id", productId));
        BigDecimal currentLowestPrice = latestList.stream()
                .map(PriceLatest::getPrice)
                .min(Comparator.naturalOrder())
                .orElse(BigDecimal.ZERO);

        LocalDateTime startDate30 = LocalDateTime.now().minusDays(30);
        List<PriceRecord> records30 = priceRecordMapper.selectList(
                new QueryWrapper<PriceRecord>()
                        .eq("product_id", productId)
                        .ge("recorded_at", startDate30));

        BigDecimal min30 = BigDecimal.ZERO;
        BigDecimal max30 = BigDecimal.ZERO;
        if (records30 != null && !records30.isEmpty()) {
            min30 = records30.stream().map(PriceRecord::getPrice).min(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
            max30 = records30.stream().map(PriceRecord::getPrice).max(Comparator.naturalOrder()).orElse(BigDecimal.ZERO);
        }

        double position30d = 50.0;
        BigDecimal range = max30.subtract(min30);
        if (range.compareTo(BigDecimal.ZERO) > 0) {
            position30d = currentLowestPrice.subtract(min30)
                    .divide(range, 4, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .doubleValue();
        }

        String suggestion;
        if (position30d < 30) {
            suggestion = "当前价格处于近期低位，是较好的购买时机";
        } else if (position30d > 70) {
            suggestion = "当前价格处于近期高位，建议等待降价";
        } else {
            suggestion = "当前价格处于近期中位，可根据需求决定是否购买";
        }

        result.put("historyLowestPrice", historyLowestPrice);
        result.put("historyLowestDate", historyLowestDate);
        result.put("currentLowestPrice", currentLowestPrice);
        result.put("position30d", Math.round(position30d));
        result.put("suggestion", suggestion);

        return result;
    }
}
