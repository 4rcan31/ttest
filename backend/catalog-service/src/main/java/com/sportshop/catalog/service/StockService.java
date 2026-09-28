package com.sportshop.catalog.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.catalog.domain.Product;
import com.sportshop.catalog.dto.ReservedProduct;
import com.sportshop.catalog.dto.StockRequest;
import com.sportshop.catalog.repository.ProductRepository;
import com.sportshop.common.web.ApiException;

/**
 * Reserva y liberación de inventario. La reserva es "todo o nada": si un artículo no tiene
 * existencias suficientes se revierte toda la transacción.
 */
@Service
public class StockService {

    private static final Logger log = LoggerFactory.getLogger(StockService.class);

    private final ProductRepository productRepository;

    public StockService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @Transactional
    public List<ReservedProduct> reserve(StockRequest request) {
        Map<Long, Integer> quantities = mergeByProduct(request);
        Map<Long, Product> products = productRepository.findByIdInAndActiveTrue(quantities.keySet()).stream()
                .collect(Collectors.toMap(Product::getId, Function.identity()));

        // Se procesa en orden de id para que transacciones concurrentes bloqueen filas en el mismo orden.
        quantities.forEach((productId, quantity) -> {
            Product product = products.get(productId);
            if (product == null) {
                throw ApiException.notFound("PRODUCT_NOT_FOUND",
                        "El artículo con id " + productId + " ya no está disponible");
            }
            if (productRepository.decrementStock(productId, quantity) == 0) {
                throw ApiException.conflict("INSUFFICIENT_STOCK", "No hay inventario suficiente de \""
                        + product.getName() + "\". Disponible: " + product.getStock() + ", solicitado: " + quantity);
            }
        });
        log.info("Inventario reservado: {}", quantities);

        return quantities.entrySet().stream()
                .map(entry -> {
                    Product product = products.get(entry.getKey());
                    return new ReservedProduct(product.getId(), product.getSku(), product.getName(),
                            product.getImageUrl(), product.getPrice(), entry.getValue());
                })
                .sorted(Comparator.comparing(ReservedProduct::productId))
                .toList();
    }

    @Transactional
    public void release(StockRequest request) {
        mergeByProduct(request).forEach(productRepository::incrementStock);
        log.info("Inventario liberado: {}", request.items());
    }

    private static Map<Long, Integer> mergeByProduct(StockRequest request) {
        return request.items().stream().collect(Collectors.toMap(
                StockRequest.Item::productId, StockRequest.Item::quantity, Integer::sum, TreeMap::new));
    }
}
