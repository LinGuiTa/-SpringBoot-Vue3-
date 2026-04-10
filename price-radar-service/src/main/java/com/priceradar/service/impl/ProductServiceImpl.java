package com.priceradar.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.priceradar.common.exception.BusinessException;
import com.priceradar.common.result.ResultCode;
import com.priceradar.domain.entity.Category;
import com.priceradar.domain.entity.Product;
import com.priceradar.domain.vo.ProductVO;
import com.priceradar.mapper.CategoryMapper;
import com.priceradar.mapper.ProductMapper;
import com.priceradar.service.ProductService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Override
    public ProductVO getByBarcode(String barcode) {
        Product product = productMapper.selectOne(
                new QueryWrapper<Product>()
                        .eq("barcode", barcode)
                        .eq("status", 1)
        );
        if (product == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return toProductVO(product);
    }

    @Override
    public ProductVO getById(Long id) {
        Product product = productMapper.selectById(id);
        if (product == null || product.getStatus() == 0) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        return toProductVO(product);
    }

    @Override
    public IPage<ProductVO> search(String keyword, Long categoryId, Integer page, Integer size) {
        QueryWrapper<Product> wrapper = new QueryWrapper<>();
        if (keyword != null && !keyword.isEmpty()) {
            wrapper.and(w -> w.like("name", keyword).or().like("brand", keyword));
        }
        if (categoryId != null) {
            wrapper.eq("category_id", categoryId);
        }
        wrapper.eq("status", 1);

        IPage<Product> productPage = productMapper.selectPage(new Page<>(page, size), wrapper);

        Page<ProductVO> voPage = new Page<>(productPage.getCurrent(), productPage.getSize(), productPage.getTotal());
        List<ProductVO> voList = productPage.getRecords().stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());
        voPage.setRecords(voList);
        return voPage;
    }

    @Override
    public List<ProductVO> getHotProducts(Integer limit) {
        IPage<Product> page = productMapper.selectPage(
                new Page<>(1, limit),
                new QueryWrapper<Product>()
                        .eq("status", 1)
                        .orderByDesc("created_at")
        );
        return page.getRecords().stream()
                .map(this::toProductVO)
                .collect(Collectors.toList());
    }

    @Override
    public Product saveProduct(Product product) {
        product.setStatus(1);
        product.setCreatedAt(LocalDateTime.now());
        productMapper.insert(product);
        return product;
    }

    @Override
    public Product updateProduct(Long id, Product product) {
        Product existing = productMapper.selectById(id);
        if (existing == null) {
            throw new BusinessException(ResultCode.PRODUCT_NOT_FOUND);
        }
        if (product.getBarcode() != null) {
            existing.setBarcode(product.getBarcode());
        }
        if (product.getName() != null) {
            existing.setName(product.getName());
        }
        if (product.getBrand() != null) {
            existing.setBrand(product.getBrand());
        }
        if (product.getCategoryId() != null) {
            existing.setCategoryId(product.getCategoryId());
        }
        if (product.getImageUrl() != null) {
            existing.setImageUrl(product.getImageUrl());
        }
        if (product.getDescription() != null) {
            existing.setDescription(product.getDescription());
        }
        if (product.getSpec() != null) {
            existing.setSpec(product.getSpec());
        }
        if (product.getWeight() != null) {
            existing.setWeight(product.getWeight());
        }
        if (product.getUnit() != null) {
            existing.setUnit(product.getUnit());
        }
        if (product.getStatus() != null) {
            existing.setStatus(product.getStatus());
        }
        productMapper.updateById(existing);
        return existing;
    }

    private ProductVO toProductVO(Product product) {
        ProductVO vo = new ProductVO();
        BeanUtils.copyProperties(product, vo);
        if (product.getCategoryId() != null) {
            Category category = categoryMapper.selectById(product.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        return vo;
    }
}
