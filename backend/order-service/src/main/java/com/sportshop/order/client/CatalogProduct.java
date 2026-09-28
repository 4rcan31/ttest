package com.sportshop.order.client;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Vista del producto que order-service necesita del catálogo. */
@JsonIgnoreProperties(ignoreUnknown = true)
public record CatalogProduct(Long id, String sku, String name, BigDecimal price, int stock, String imageUrl) {
}
