package com.sportshop.catalog.dto;

import java.math.BigDecimal;

import com.sportshop.catalog.domain.Product;

public record ProductResponse(
        Long id,
        String sku,
        String name,
        String description,
        String brand,
        CategoryResponse category,
        BigDecimal price,
        int stock,
        boolean available,
        String imageUrl) {

    public static ProductResponse from(Product product) {
        return new ProductResponse(product.getId(), product.getSku(), product.getName(), product.getDescription(),
                product.getBrand(), CategoryResponse.from(product.getCategory()), product.getPrice(),
                product.getStock(), product.getStock() > 0, product.getImageUrl());
    }
}
