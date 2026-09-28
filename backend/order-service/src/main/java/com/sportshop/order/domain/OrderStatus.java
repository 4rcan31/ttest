package com.sportshop.order.domain;

import java.util.Set;

/** Ciclo de vida de una orden y transiciones permitidas. */
public enum OrderStatus {

    CONFIRMED("Confirmada"),
    PROCESSING("En preparación"),
    SHIPPED("Enviada"),
    DELIVERED("Entregada"),
    CANCELLED("Cancelada");

    private final String label;

    OrderStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public Set<OrderStatus> nextStatuses() {
        return switch (this) {
            case CONFIRMED -> Set.of(PROCESSING, CANCELLED);
            case PROCESSING -> Set.of(SHIPPED, CANCELLED);
            case SHIPPED -> Set.of(DELIVERED);
            case DELIVERED, CANCELLED -> Set.of();
        };
    }

    public boolean canTransitionTo(OrderStatus target) {
        return nextStatuses().contains(target);
    }

    /** El cliente solo puede cancelar mientras la orden no ha entrado a preparación. */
    public boolean isCancellableByCustomer() {
        return this == CONFIRMED;
    }
}
