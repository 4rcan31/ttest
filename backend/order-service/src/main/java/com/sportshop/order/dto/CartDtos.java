package com.sportshop.order.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Contratos de la API del carrito de compras. */
public final class CartDtos {

    private CartDtos() {
    }

    public record AddCartItemRequest(
            @NotNull(message = "El artículo es obligatorio") Long productId,
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = 10, message = "La cantidad máxima por artículo es 10") int quantity) {
    }

    public record UpdateCartItemRequest(
            @Min(value = 1, message = "La cantidad mínima es 1")
            @Max(value = 10, message = "La cantidad máxima por artículo es 10") int quantity) {
    }

    /**
     * @param available {@code false} si el artículo ya no existe o no hay inventario suficiente
     */
    public record CartItemResponse(
            Long productId,
            String sku,
            String name,
            String imageUrl,
            BigDecimal unitPrice,
            int quantity,
            BigDecimal lineTotal,
            int availableStock,
            boolean available) {
    }

    public record CartResponse(
            List<CartItemResponse> items,
            int totalItems,
            BigDecimal subtotal,
            BigDecimal shippingCost,
            BigDecimal total,
            boolean readyForCheckout) {
    }
}
