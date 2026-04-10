package com.priceradar.web.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.common.result.PageResult;
import com.priceradar.common.result.Result;
import com.priceradar.domain.vo.ProductVO;
import com.priceradar.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @GetMapping("/barcode/{barcode}")
    public Result<ProductVO> getByBarcode(@PathVariable String barcode) {
        return Result.success(productService.getByBarcode(barcode));
    }

    @GetMapping("/{id}")
    public Result<ProductVO> getById(@PathVariable Long id) {
        return Result.success(productService.getById(id));
    }

    @GetMapping("/search")
    public Result<PageResult<ProductVO>> search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProductVO> iPage = productService.search(keyword, categoryId, page, size);
        return Result.success(PageResult.of(iPage));
    }

    @GetMapping("/hot")
    public Result<List<ProductVO>> getHot(
            @RequestParam(defaultValue = "10") Integer limit) {
        return Result.success(productService.getHotProducts(limit));
    }
}
