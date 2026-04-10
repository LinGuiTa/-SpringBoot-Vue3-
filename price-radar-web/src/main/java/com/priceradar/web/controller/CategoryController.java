package com.priceradar.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.common.result.PageResult;
import com.priceradar.common.result.Result;
import com.priceradar.domain.entity.Category;
import com.priceradar.domain.entity.Product;
import com.priceradar.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public Result<List<Category>> getCategories() {
        return Result.success(categoryService.getCategoryTree());
    }

    @GetMapping("/{id}/products")
    public Result<PageResult<Product>> getProductsByCategory(
            @PathVariable Long id,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<Product> iPage = categoryService.getProductsByCategory(id, page, size);
        return Result.success(PageResult.of(iPage));
    }
}
