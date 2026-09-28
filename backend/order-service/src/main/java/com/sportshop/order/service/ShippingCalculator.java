package com.sportshop.order.service;

import java.math.BigDecimal;
import java.math.RoundingMode;

import org.springframework.stereotype.Component;

import com.sportshop.order.config.OrderServiceProperties;

@Component
public class ShippingCalculator {

    private final OrderServiceProperties.Shipping policy;

    public ShippingCalculator(OrderServiceProperties properties) {
        this.policy = properties.shipping();
    }

    public BigDecimal shippingFor(BigDecimal subtotal) {
        if (subtotal.signum() == 0 || subtotal.compareTo(policy.freeThreshold()) >= 0) {
            return money(BigDecimal.ZERO);
        }
        return money(policy.flatRate());
    }

    public static BigDecimal money(BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
