package com.priceradar.domain.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class ProductVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;

    private String barcode;

    private String name;

    private String brand;

    private String categoryName;

    private String imageUrl;

    private String description;

    private String spec;

    private Integer status;
}
