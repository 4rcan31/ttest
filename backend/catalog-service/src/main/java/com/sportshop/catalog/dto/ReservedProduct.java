package com.sportshop.catalog.dto;

import java.math.BigDecimal;

/** Foto del producto al momento de la reserva: order-service la guarda como detalle inmutable de la orden. */
public record ReservedProduct(Long productId, String sku, String name, String imageUrl, BigDecimal unitPrice,
                              int quantity) {
}
