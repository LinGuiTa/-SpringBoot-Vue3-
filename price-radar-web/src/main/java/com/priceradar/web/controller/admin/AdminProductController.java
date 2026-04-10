package com.priceradar.web.controller.admin;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.common.result.PageResult;
import com.priceradar.common.result.Result;
import com.priceradar.domain.entity.Product;
import com.priceradar.domain.vo.ProductVO;
import com.priceradar.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/products")
@PreAuthorize("hasRole('ADMIN')")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @GetMapping
    public Result<PageResult<ProductVO>> list(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size) {
        IPage<ProductVO> iPage = productService.search(keyword, categoryId, page, size);
        return Result.success(PageResult.of(iPage));
    }

    @PostMapping
    public Result<Product> create(@RequestBody Product product) {
        return Result.success(productService.saveProduct(product));
    }

    @PutMapping("/{id}")
    public Result<Product> update(@PathVariable Long id, @RequestBody Product product) {
        return Result.success(productService.updateProduct(id, product));
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        Product product = new Product();
        product.setStatus(0);
        productService.updateProduct(id, product);
        return Result.success();
    }
}
