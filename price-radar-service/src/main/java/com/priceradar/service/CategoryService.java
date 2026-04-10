package com.priceradar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.domain.entity.Category;
import com.priceradar.domain.entity.Product;

import java.util.List;

public interface CategoryService {

    List<Category> getCategoryTree();

    IPage<Product> getProductsByCategory(Long categoryId, Integer page, Integer size);
}
