package com.sportshop.order.config;

import java.math.BigDecimal;
import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param catalog  conexión con catalog-service
 * @param shipping política de costo de envío
 * @param cart     límites del carrito
 */
@ConfigurationProperties("app")
public record OrderServiceProperties(Catalog catalog, Shipping shipping, Cart cart) {

    public OrderServiceProperties {
        if (catalog == null) {
            catalog = new Catalog(null, null, null);
        }
        if (shipping == null) {
            shipping = new Shipping(null, null);
        }
        if (cart == null) {
            cart = new Cart(0);
        }
    }

    public record Catalog(String baseUrl, Duration connectTimeout, Duration readTimeout) {
        public Catalog {
            if (baseUrl == null) {
                baseUrl = "http://localhost:8082";
            }
            if (connectTimeout == null) {
                connectTimeout = Duration.ofSeconds(2);
            }
            if (readTimeout == null) {
                readTimeout = Duration.ofSeconds(5);
            }
        }
    }

    /** Envío con tarifa plana, gratuito a partir de {@code freeThreshold}. */
    public record Shipping(BigDecimal flatRate, BigDecimal freeThreshold) {
        public Shipping {
            if (flatRate == null) {
                flatRate = new BigDecimal("5.00");
            }
            if (freeThreshold == null) {
                freeThreshold = new BigDecimal("75.00");
            }
        }
    }

    public record Cart(int maxQuantityPerItem) {
        public Cart {
            if (maxQuantityPerItem <= 0) {
                maxQuantityPerItem = 10;
            }
        }
    }
}
