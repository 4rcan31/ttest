package com.sportshop.order.web;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.sportshop.common.security.AuthenticatedUser;
import com.sportshop.order.dto.OrderDtos.CreateOrderRequest;
import com.sportshop.order.dto.OrderDtos.OrderResponse;
import com.sportshop.order.dto.OrderDtos.OrderSummaryResponse;
import com.sportshop.order.dto.PageResponse;
import com.sportshop.order.service.OrderService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/orders")
@Tag(name = "Órdenes del cliente")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @Operation(summary = "Confirmar el pedido con el contenido actual del carrito")
    @PostMapping
    public ResponseEntity<OrderResponse> create(@AuthenticationPrincipal Jwt jwt,
                                                @Valid @RequestBody CreateOrderRequest request) {
        OrderResponse order = orderService.createOrder(AuthenticatedUser.from(jwt), request);
        return ResponseEntity.created(URI.create("/api/orders/" + order.orderNumber())).body(order);
    }

    @Operation(summary = "Listar las órdenes del cliente (más recientes primero)")
    @GetMapping
    public PageResponse<OrderSummaryResponse> list(@AuthenticationPrincipal Jwt jwt,
                                                   @RequestParam(defaultValue = "0") int page,
                                                   @RequestParam(defaultValue = "10") int size) {
        return orderService.getOrders(AuthenticatedUser.from(jwt).id(), page, size);
    }

    @Operation(summary = "Consultar el detalle y el estado de una orden")
    @GetMapping("/{orderNumber}")
    public OrderResponse get(@AuthenticationPrincipal Jwt jwt, @PathVariable String orderNumber) {
        return orderService.getOrder(AuthenticatedUser.from(jwt).id(), orderNumber);
    }

    @Operation(summary = "Cancelar una orden que aún no está en preparación (libera el inventario)")
    @PostMapping("/{orderNumber}/cancel")
    public OrderResponse cancel(@AuthenticationPrincipal Jwt jwt, @PathVariable String orderNumber) {
        return orderService.cancelOrder(AuthenticatedUser.from(jwt).id(), orderNumber);
    }
}
