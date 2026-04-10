package com.priceradar.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.priceradar.domain.entity.Product;
import com.priceradar.domain.vo.ProductVO;

import java.util.List;

public interface ProductService {

    ProductVO getByBarcode(String barcode);

    ProductVO getById(Long id);

    IPage<ProductVO> search(String keyword, Long categoryId, Integer page, Integer size);

    List<ProductVO> getHotProducts(Integer limit);

    Product saveProduct(Product product);

    Product updateProduct(Long id, Product product);
}
