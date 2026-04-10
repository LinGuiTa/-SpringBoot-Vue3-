package com.priceradar.service;

import com.priceradar.domain.vo.FavoriteVO;

import java.util.List;

public interface FavoriteService {
    void addFavorite(Long userId, Long productId);
    void removeFavorite(Long userId, Long productId);
    List<FavoriteVO> getFavorites(Long userId);
    boolean isFavorited(Long userId, Long productId);
}
