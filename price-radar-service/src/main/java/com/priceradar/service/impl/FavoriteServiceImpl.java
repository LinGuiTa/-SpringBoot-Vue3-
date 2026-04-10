package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.priceradar.domain.entity.Platform;
import com.priceradar.domain.entity.PriceLatest;
import com.priceradar.domain.entity.Product;
import com.priceradar.domain.entity.UserFavorite;
import com.priceradar.domain.vo.FavoriteVO;
import com.priceradar.mapper.PlatformMapper;
import com.priceradar.mapper.PriceLatestMapper;
import com.priceradar.mapper.ProductMapper;
import com.priceradar.mapper.UserFavoriteMapper;
import com.priceradar.service.FavoriteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class FavoriteServiceImpl implements FavoriteService {

    @Autowired
    private UserFavoriteMapper userFavoriteMapper;

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private PriceLatestMapper priceLatestMapper;

    @Autowired
    private PlatformMapper platformMapper;

    @Override
    public void addFavorite(Long userId, Long productId) {
        UserFavorite existing = userFavoriteMapper.selectOne(
                new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("product_id", productId));
        if (existing != null) {
            return;
        }
        UserFavorite favorite = new UserFavorite();
        favorite.setUserId(userId);
        favorite.setProductId(productId);
        userFavoriteMapper.insert(favorite);
    }

    @Override
    public void removeFavorite(Long userId, Long productId) {
        userFavoriteMapper.delete(
                new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("product_id", productId));
    }

    @Override
    public List<FavoriteVO> getFavorites(Long userId) {
        List<UserFavorite> favorites = userFavoriteMapper.selectList(
                new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .orderByDesc("created_at"));
        List<FavoriteVO> result = new ArrayList<>();
        for (UserFavorite favorite : favorites) {
            Product product = productMapper.selectById(favorite.getProductId());
            if (product == null) {
                continue;
            }
            FavoriteVO vo = new FavoriteVO();
            vo.setFavoriteId(favorite.getId());
            vo.setProductId(favorite.getProductId());
            vo.setProductName(product.getName());
            vo.setProductImage(product.getImageUrl());
            vo.setBrand(product.getBrand());
            vo.setFavoritedAt(favorite.getCreatedAt());

            List<PriceLatest> priceList = priceLatestMapper.selectList(
                    new QueryWrapper<PriceLatest>().eq("product_id", favorite.getProductId()));
            if (priceList != null && !priceList.isEmpty()) {
                PriceLatest lowest = priceList.stream()
                        .min(Comparator.comparing(PriceLatest::getPrice))
                        .orElse(null);
                if (lowest != null) {
                    vo.setLowestPrice(lowest.getPrice());
                    Platform platform = platformMapper.selectById(lowest.getPlatformId());
                    if (platform != null) {
                        vo.setLowestPlatformName(platform.getName());
                    }
                }
            }
            result.add(vo);
        }
        return result;
    }

    @Override
    public boolean isFavorited(Long userId, Long productId) {
        return userFavoriteMapper.selectCount(
                new QueryWrapper<UserFavorite>()
                        .eq("user_id", userId)
                        .eq("product_id", productId)) > 0;
    }
}
