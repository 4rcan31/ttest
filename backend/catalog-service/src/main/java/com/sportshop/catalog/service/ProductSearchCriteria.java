package com.sportshop.catalog.service;

import java.math.BigDecimal;

/**
 * Filtros de búsqueda del catálogo.
 *
 * @param query    texto libre sobre nombre, descripción, marca y categoría
 * @param category slug de la categoría
 * @param inStock  si es {@code true}, solo artículos con inventario disponible
 * @param sort     featured | price_asc | price_desc | name
 */
public record ProductSearchCriteria(String query, String category, BigDecimal minPrice, BigDecimal maxPrice,
                                    Boolean inStock, String sort, int page, int size) {
}
