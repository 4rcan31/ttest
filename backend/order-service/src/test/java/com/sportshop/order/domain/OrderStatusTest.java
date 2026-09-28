package com.sportshop.order.domain;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class OrderStatusTest {

    @Test
    void allowsOnlyForwardTransitions() {
        assertThat(OrderStatus.CONFIRMED.canTransitionTo(OrderStatus.PROCESSING)).isTrue();
        assertThat(OrderStatus.PROCESSING.canTransitionTo(OrderStatus.SHIPPED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.DELIVERED)).isTrue();
        assertThat(OrderStatus.SHIPPED.canTransitionTo(OrderStatus.CANCELLED)).isFalse();
        assertThat(OrderStatus.DELIVERED.nextStatuses()).isEmpty();
        assertThat(OrderStatus.CANCELLED.nextStatuses()).isEmpty();
    }

    @Test
    void customerCanOnlyCancelConfirmedOrders() {
        assertThat(OrderStatus.CONFIRMED.isCancellableByCustomer()).isTrue();
        assertThat(OrderStatus.PROCESSING.isCancellableByCustomer()).isFalse();
    }
}
