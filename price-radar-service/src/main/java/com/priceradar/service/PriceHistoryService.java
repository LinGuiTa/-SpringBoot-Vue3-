package com.priceradar.service;

import com.priceradar.domain.vo.PriceHistoryVO;
import com.priceradar.domain.vo.PriceStatisticsVO;

import java.util.List;
import java.util.Map;

public interface PriceHistoryService {
    List<PriceHistoryVO> getHistory(Long productId, Long platformId, Integer days);
    PriceStatisticsVO getStatistics(Long productId, Long platformId);
    Map<String, String> getTrend(Long productId);
    Map<String, Object> getBestBuyTime(Long productId);
}
