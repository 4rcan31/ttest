package com.sportshop.order.web;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportshop.order.domain.OrderStatus;
import com.sportshop.order.dto.OrderDtos.OrderResponse;
import com.sportshop.order.dto.OrderDtos.OrderSummaryResponse;
import com.sportshop.order.dto.OrderDtos.UpdateOrderStatusRequest;
import com.sportshop.order.dto.PageResponse;
import com.sportshop.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Gestión de estados de las órdenes (rol ADMIN) para simular el avance del pedido. */
@RestController
@RequestMapping("/api/admin/orders")
@Tag(name = "Administración de órdenes")
public class AdminOrderController {

    private final OrderService orderService;

    public AdminOrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Listar todas las órdenes, opcionalmente filtradas por estado")
    @GetMapping
    public PageResponse<OrderSummaryResponse> list(@RequestParam(required = false) OrderStatus status,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "20") int size) {
        return orderService.getAllOrders(status, page, size);
    }

    @Operation(summary = "Consultar cualquier orden")
    @GetMapping("/{orderNumber}")
    public OrderResponse get(@PathVariable String orderNumber) {
        return orderService.getAnyOrder(orderNumber);
    }

    @Operation(summary = "Cambiar el estado de una orden")
    @PatchMapping("/{orderNumber}/status")
    public OrderResponse updateStatus(@PathVariable String orderNumber,
                                      @Valid @RequestBody UpdateOrderStatusRequest request) {
        return orderService.updateStatus(orderNumber, request.status());
    }
}
