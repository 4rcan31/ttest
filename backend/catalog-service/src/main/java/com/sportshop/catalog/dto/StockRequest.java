package com.sportshop.catalog.dto;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

/** Solicitud interna para reservar (descontar) o liberar (devolver) inventario. */
public record StockRequest(@NotEmpty @Valid List<Item> items) {

    public record Item(
            @NotNull Long productId,
            @Min(1) @Max(99) int quantity) {
    }
}
