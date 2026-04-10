package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.entity.PriceLatest;
import com.priceradar.domain.entity.PriceRecord;
import com.priceradar.domain.entity.Product;
import com.priceradar.domain.vo.PriceCompareVO;
import com.priceradar.mapper.PlatformMapper;
import com.priceradar.mapper.PriceLatestMapper;
import com.priceradar.mapper.PriceRecordMapper;
import com.priceradar.mapper.ProductMapper;
import com.priceradar.service.PriceService;
import com.priceradar.service.provider.PriceData;
import com.priceradar.service.provider.PriceProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class PriceServiceImpl implements PriceService {

    private static final String CACHE_KEY_PREFIX = "price:compare:";

    @Autowired
    private List<PriceProvider> priceProviders;

    @Autowired
    private PlatformMapper platformMapper;

    @Autowired
    private PriceRecordMapper priceRecordMapper;

    @Autowired
    private PriceLatestMapper priceLatestMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Override
    public List<PriceCompareVO> comparePrices(Long productId) {
        String cacheKey = CACHE_KEY_PREFIX + productId;
        String cached = redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            try {
                return objectMapper.readValue(cached, new TypeReference<List<PriceCompareVO>>() {});
            } catch (Exception ignored) {
            }
        }

        fetchAndSavePrices(productId);

        List<PriceLatest> latestList = priceLatestMapper.selectList(
                new QueryWrapper<PriceLatest>().eq("product_id", productId));

        List<Platform> platforms = platformMapper.selectList(
                new QueryWrapper<Platform>().eq("status", 1));
        Map<Long, Platform> platformMap = platforms.stream()
                .collect(Collectors.toMap(Platform::getId, p -> p));

        List<PriceCompareVO> voList = new ArrayList<>();
        for (PriceLatest latest : latestList) {
            Platform platform = platformMap.get(latest.getPlatformId());
            if (platform == null) {
                continue;
            }
            PriceCompareVO vo = new PriceCompareVO();
            vo.setPlatformId(latest.getPlatformId());
            vo.setPlatformName(platform.getName());
            vo.setPlatformLogoUrl(platform.getLogoUrl());
            vo.setPrice(latest.getPrice());
            vo.setOriginalPrice(latest.getOriginalPrice());
            vo.setDiscountInfo(latest.getDiscountInfo());
            vo.setStockStatus(latest.getStockStatus());
            vo.setUrl(latest.getUrl());
            vo.setIsLowest(false);
            voList.add(vo);
        }

        if (!voList.isEmpty()) {
            BigDecimal minPrice = voList.stream()
                    .map(PriceCompareVO::getPrice)
                    .min(Comparator.naturalOrder())
                    .orElse(null);
            if (minPrice != null) {
                for (PriceCompareVO vo : voList) {
                    if (minPrice.compareTo(vo.getPrice()) == 0) {
                        vo.setIsLowest(true);
                        break;
                    }
                }
            }
        }

        voList.sort(Comparator.comparing(PriceCompareVO::getPrice));

        try {
            String json = objectMapper.writeValueAsString(voList);
            redisTemplate.opsForValue().set(cacheKey, json, 1800, TimeUnit.SECONDS);
        } catch (Exception ignored) {
        }

        return voList;
    }

    private void fetchAndSavePrices(Long productId) {
        Product product = productMapper.selectById(productId);
        String barcode = product != null && product.getBarcode() != null ? product.getBarcode() : String.valueOf(productId);

        BigDecimal basePrice;
        List<PriceLatest> existingList = priceLatestMapper.selectList(
                new QueryWrapper<PriceLatest>().eq("product_id", productId).last("LIMIT 1"));
        if (existingList != null && !existingList.isEmpty()) {
            basePrice = existingList.get(0).getPrice();
        } else {
            basePrice = new BigDecimal("100");
        }

        final BigDecimal finalBasePrice = basePrice;
        final String finalBarcode = barcode;

        List<CompletableFuture<PriceData>> futures = priceProviders.stream()
                .map(provider -> CompletableFuture.supplyAsync(
                        () -> provider.fetchPrice(finalBarcode, finalBasePrice)))
                .collect(Collectors.toList());

        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        for (CompletableFuture<PriceData> future : futures) {
            try {
                PriceData data = future.get();
                if (data == null) {
                    continue;
                }

                PriceRecord record = new PriceRecord();
                record.setProductId(productId);
                record.setPlatformId(data.getPlatformId());
                record.setPrice(data.getPrice());
                record.setOriginalPrice(data.getOriginalPrice());
                record.setDiscountInfo(data.getDiscountInfo());
                record.setStockStatus(data.getStockStatus());
                record.setUrl(data.getUrl());
                record.setRecordedAt(LocalDateTime.now());
                priceRecordMapper.insert(record);

                PriceLatest existing = priceLatestMapper.selectOne(
                        new QueryWrapper<PriceLatest>()
                                .eq("product_id", productId)
                                .eq("platform_id", data.getPlatformId()));
                if (existing != null) {
                    existing.setPrice(data.getPrice());
                    existing.setOriginalPrice(data.getOriginalPrice());
                    existing.setDiscountInfo(data.getDiscountInfo());
                    existing.setStockStatus(data.getStockStatus());
                    existing.setUrl(data.getUrl());
                    existing.setUpdatedAt(LocalDateTime.now());
                    priceLatestMapper.updateById(existing);
                } else {
                    PriceLatest latest = new PriceLatest();
                    latest.setProductId(productId);
                    latest.setPlatformId(data.getPlatformId());
                    latest.setPrice(data.getPrice());
                    latest.setOriginalPrice(data.getOriginalPrice());
                    latest.setDiscountInfo(data.getDiscountInfo());
                    latest.setStockStatus(data.getStockStatus());
                    latest.setUrl(data.getUrl());
                    latest.setUpdatedAt(LocalDateTime.now());
                    priceLatestMapper.insert(latest);
                }
            } catch (Exception ignored) {
            }
        }
    }

    @Override
    public PriceCompareVO getLowestPrice(Long productId) {
        List<PriceLatest> latestList = priceLatestMapper.selectList(
                new QueryWrapper<PriceLatest>().eq("product_id", productId));
        if (latestList == null || latestList.isEmpty()) {
            return null;
        }

        PriceLatest lowest = latestList.stream()
                .min(Comparator.comparing(PriceLatest::getPrice))
                .orElse(null);
        if (lowest == null) {
            return null;
        }

        Platform platform = platformMapper.selectById(lowest.getPlatformId());
        PriceCompareVO vo = new PriceCompareVO();
        vo.setPlatformId(lowest.getPlatformId());
        vo.setPlatformName(platform != null ? platform.getName() : null);
        vo.setPlatformLogoUrl(platform != null ? platform.getLogoUrl() : null);
        vo.setPrice(lowest.getPrice());
        vo.setOriginalPrice(lowest.getOriginalPrice());
        vo.setDiscountInfo(lowest.getDiscountInfo());
        vo.setStockStatus(lowest.getStockStatus());
        vo.setUrl(lowest.getUrl());
        vo.setIsLowest(true);
        return vo;
    }

    @Override
    public List<Platform> getAllPlatforms() {
        return platformMapper.selectList(
                new QueryWrapper<Platform>()
                        .eq("status", 1)
                        .orderByAsc("sort_order"));
    }

    @Override
    public void refreshPrices(Long productId) {
        String cacheKey = CACHE_KEY_PREFIX + productId;
        redisTemplate.delete(cacheKey);
        fetchAndSavePrices(productId);
    }

    @Override
    public void refreshAllHotProducts() {
        List<Product> products = productMapper.selectList(
                new QueryWrapper<Product>()
                        .eq("status", 1)
                        .last("LIMIT 20"));
        for (Product product : products) {
            refreshPrices(product.getId());
        }
    }
}
