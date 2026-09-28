package com.sportshop.order.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportshop.common.security.AuthenticatedUser;
import com.sportshop.order.dto.CartDtos.AddCartItemRequest;
import com.sportshop.order.dto.CartDtos.CartResponse;
import com.sportshop.order.dto.CartDtos.UpdateCartItemRequest;
import com.sportshop.order.service.CartService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cart")
@Tag(name = "Carrito de compras")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Consultar el carrito con el resumen de artículos y totales")
    @GetMapping
    public CartResponse getCart(@AuthenticationPrincipal Jwt jwt) {
        return cartService.getCart(userId(jwt));
    }

    @Operation(summary = "Agregar un artículo (si ya existe, suma la cantidad)")
    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public CartResponse addItem(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AddCartItemRequest request) {
        return cartService.addItem(userId(jwt), request);
    }

    @Operation(summary = "Modificar la cantidad de un artículo")
    @PutMapping("/items/{productId}")
    public CartResponse updateItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId,
                                   @Valid @RequestBody UpdateCartItemRequest request) {
        return cartService.updateItem(userId(jwt), productId, request.quantity());
    }

    @Operation(summary = "Eliminar un artículo del carrito")
    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(@AuthenticationPrincipal Jwt jwt, @PathVariable Long productId) {
        return cartService.removeItem(userId(jwt), productId);
    }

    @Operation(summary = "Vaciar el carrito")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clear(@AuthenticationPrincipal Jwt jwt) {
        cartService.clear(userId(jwt));
    }

    private static Long userId(Jwt jwt) {
        return AuthenticatedUser.from(jwt).id();
    }
}
