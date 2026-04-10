package com.priceradar.service;

import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.vo.PriceCompareVO;

import java.util.List;

public interface PriceService {
    List<PriceCompareVO> comparePrices(Long productId);
    PriceCompareVO getLowestPrice(Long productId);
    List<Platform> getAllPlatforms();
    void refreshPrices(Long productId);
    void refreshAllHotProducts();
}
