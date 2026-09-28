package com.sportshop.order.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import com.sportshop.order.domain.Order;
import com.sportshop.order.domain.OrderItem;
import com.sportshop.order.domain.OrderStatus;
import com.sportshop.order.domain.OrderStatusHistory;

/** Contratos de la API de órdenes. */
public final class OrderDtos {

    private OrderDtos() {
    }

    /** La dirección se envía explícitamente porque el cliente puede editarla al confirmar el pedido. */
    public record CreateOrderRequest(
            @NotBlank(message = "La dirección de envío es obligatoria")
            @Size(min = 10, max = 255, message = "La dirección de envío debe tener entre 10 y 255 caracteres")
            String shippingAddress) {
    }

    public record UpdateOrderStatusRequest(@NotNull(message = "El estado es obligatorio") OrderStatus status) {
    }

    public record OrderItemResponse(Long productId, String sku, String name, String imageUrl, BigDecimal unitPrice,
                                    int quantity, BigDecimal lineTotal) {

        static OrderItemResponse from(OrderItem item) {
            return new OrderItemResponse(item.getProductId(), item.getSku(), item.getProductName(),
                    item.getImageUrl(), item.getUnitPrice(), item.getQuantity(), item.getLineTotal());
        }
    }

    public record StatusChangeResponse(OrderStatus status, String statusLabel, String changedBy, Instant changedAt) {

        static StatusChangeResponse from(OrderStatusHistory entry) {
            return new StatusChangeResponse(entry.getStatus(), entry.getStatus().label(), entry.getChangedBy(),
                    entry.getChangedAt());
        }
    }

    public record OrderResponse(
            String orderNumber,
            OrderStatus status,
            String statusLabel,
            String customerName,
            String customerEmail,
            String shippingAddress,
            int itemCount,
            BigDecimal subtotal,
            BigDecimal shippingCost,
            BigDecimal total,
            boolean cancellable,
            List<OrderStatus> nextStatuses,
            List<OrderItemResponse> items,
            List<StatusChangeResponse> history,
            Instant createdAt,
            Instant updatedAt) {

        public static OrderResponse from(Order order) {
            return new OrderResponse(order.getOrderNumber(), order.getStatus(), order.getStatus().label(),
                    order.getCustomerName(), order.getCustomerEmail(), order.getShippingAddress(),
                    order.getItemCount(), order.getSubtotal(), order.getShippingCost(), order.getTotal(),
                    order.getStatus().isCancellableByCustomer(),
                    order.getStatus().nextStatuses().stream().sorted().toList(),
                    order.getItems().stream().map(OrderItemResponse::from).toList(),
                    order.getHistory().stream().map(StatusChangeResponse::from).toList(),
                    order.getCreatedAt(), order.getUpdatedAt());
        }
    }

    public record OrderSummaryResponse(
            String orderNumber,
            OrderStatus status,
            String statusLabel,
            String customerName,
            int itemCount,
            BigDecimal total,
            List<String> previewImages,
            Instant createdAt) {

        public static OrderSummaryResponse from(Order order) {
            return new OrderSummaryResponse(order.getOrderNumber(), order.getStatus(), order.getStatus().label(),
                    order.getCustomerName(), order.getItemCount(), order.getTotal(),
                    order.getItems().stream().limit(3).map(OrderItem::getImageUrl).toList(),
                    order.getCreatedAt());
        }
    }
}
