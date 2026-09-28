package com.sportshop.order.client;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ReservedProduct(Long productId, String sku, String name, String imageUrl, BigDecimal unitPrice,
                              int quantity) {
}
