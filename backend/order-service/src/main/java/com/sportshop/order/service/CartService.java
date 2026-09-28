package com.sportshop.order.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.sportshop.common.web.ApiException;
import com.sportshop.order.client.CatalogClient;
import com.sportshop.order.client.CatalogProduct;
import com.sportshop.order.config.OrderServiceProperties;
import com.sportshop.order.domain.CartItem;
import com.sportshop.order.dto.CartDtos.AddCartItemRequest;
import com.sportshop.order.dto.CartDtos.CartItemResponse;
import com.sportshop.order.dto.CartDtos.CartResponse;
import com.sportshop.order.repository.CartItemRepository;

/**
 * Carrito de compras por usuario. Solo persiste producto y cantidad; precio, nombre, imagen e
 * inventario se consultan al catálogo en cada lectura para mostrar siempre información vigente.
 */
@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final CatalogClient catalogClient;
    private final ShippingCalculator shippingCalculator;
    private final int maxQuantityPerItem;

    public CartService(CartItemRepository cartItemRepository, CatalogClient catalogClient,
                       ShippingCalculator shippingCalculator, OrderServiceProperties properties) {
        this.cartItemRepository = cartItemRepository;
        this.catalogClient = catalogClient;
        this.shippingCalculator = shippingCalculator;
        this.maxQuantityPerItem = properties.cart().maxQuantityPerItem();
    }

    public CartResponse getCart(Long userId) {
        return buildCart(cartItemRepository.findByUserIdOrderByCreatedAtAscIdAsc(userId));
    }

    public CartResponse addItem(Long userId, AddCartItemRequest request) {
        CatalogProduct product = findProduct(request.productId());
        CartItem item = cartItemRepository.findByUserIdAndProductId(userId, request.productId())
                .orElseGet(() -> new CartItem(userId, request.productId(), 0));
        int newQuantity = item.getQuantity() + request.quantity();
        validateQuantity(product, newQuantity);
        item.setQuantity(newQuantity);
        try {
            cartItemRepository.save(item);
        } catch (DataIntegrityViolationException concurrentInsert) {
            // Otra petición simultánea (p. ej. doble clic) ya creó la línea: se suma sobre la existente.
            CartItem existing = findCartItem(userId, request.productId());
            int mergedQuantity = existing.getQuantity() + request.quantity();
            validateQuantity(product, mergedQuantity);
            existing.setQuantity(mergedQuantity);
            cartItemRepository.save(existing);
        }
        return getCart(userId);
    }

    public CartResponse updateItem(Long userId, Long productId, int quantity) {
        CartItem item = findCartItem(userId, productId);
        validateQuantity(findProduct(productId), quantity);
        item.setQuantity(quantity);
        cartItemRepository.save(item);
        return getCart(userId);
    }

    public CartResponse removeItem(Long userId, Long productId) {
        cartItemRepository.delete(findCartItem(userId, productId));
        return getCart(userId);
    }

    @Transactional
    public void clear(Long userId) {
        cartItemRepository.deleteByUserId(userId);
    }

    private CartResponse buildCart(List<CartItem> items) {
        Map<Long, CatalogProduct> products = catalogClient
                .getProducts(items.stream().map(CartItem::getProductId).toList()).stream()
                .collect(Collectors.toMap(CatalogProduct::id, Function.identity()));

        List<CartItemResponse> lines = items.stream().map(item -> {
            CatalogProduct product = products.get(item.getProductId());
            if (product == null) {
                return new CartItemResponse(item.getProductId(), null, "Artículo no disponible", null,
                        BigDecimal.ZERO, item.getQuantity(), BigDecimal.ZERO, 0, false);
            }
            BigDecimal lineTotal = product.price().multiply(BigDecimal.valueOf(item.getQuantity()));
            return new CartItemResponse(product.id(), product.sku(), product.name(), product.imageUrl(),
                    product.price(), item.getQuantity(), ShippingCalculator.money(lineTotal), product.stock(),
                    product.stock() >= item.getQuantity());
        }).toList();

        BigDecimal subtotal = ShippingCalculator.money(lines.stream()
                .filter(CartItemResponse::available)
                .map(CartItemResponse::lineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add));
        BigDecimal shipping = shippingCalculator.shippingFor(subtotal);
        int totalItems = lines.stream().mapToInt(CartItemResponse::quantity).sum();
        boolean ready = !lines.isEmpty() && lines.stream().allMatch(CartItemResponse::available);
        return new CartResponse(lines, totalItems, subtotal, shipping, subtotal.add(shipping), ready);
    }

    private void validateQuantity(CatalogProduct product, int quantity) {
        if (quantity > maxQuantityPerItem) {
            throw ApiException.badRequest("MAX_QUANTITY_EXCEEDED",
                    "Puede agregar como máximo " + maxQuantityPerItem + " unidades de cada artículo");
        }
        if (quantity > product.stock()) {
            throw ApiException.conflict("INSUFFICIENT_STOCK", product.stock() == 0
                    ? "\"" + product.name() + "\" está agotado"
                    : "Solo hay " + product.stock() + " unidades disponibles de \"" + product.name() + "\"");
        }
    }

    private CatalogProduct findProduct(Long productId) {
        return catalogClient.getProducts(List.of(productId)).stream().findFirst()
                .orElseThrow(() -> ApiException.notFound("PRODUCT_NOT_FOUND", "El artículo no existe"));
    }

    private CartItem findCartItem(Long userId, Long productId) {
        return cartItemRepository.findByUserIdAndProductId(userId, productId)
                .orElseThrow(() -> ApiException.notFound("CART_ITEM_NOT_FOUND", "El artículo no está en el carrito"));
    }
}
