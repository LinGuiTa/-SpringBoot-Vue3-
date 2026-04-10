package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.priceradar.domain.entity.Category;
import com.priceradar.domain.entity.Product;
import com.priceradar.mapper.CategoryMapper;
import com.priceradar.mapper.ProductMapper;
import com.priceradar.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public List<Category> getCategoryTree() {
        return categoryMapper.selectList(
                new QueryWrapper<Category>()
                        .eq("status", 1)
                        .orderByAsc("level", "sort_order")
        );
    }

    @Override
    public IPage<Product> getProductsByCategory(Long categoryId, Integer page, Integer size) {
        return productMapper.selectPage(
                new Page<>(page, size),
                new QueryWrapper<Product>()
                        .eq("category_id", categoryId)
                        .eq("status", 1)
        );
    }
}
