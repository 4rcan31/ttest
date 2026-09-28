package com.sportshop.user.web;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.sportshop.common.security.AuthenticatedUser;
import com.sportshop.user.dto.ChangePasswordRequest;
import com.sportshop.user.dto.UpdateAddressRequest;
import com.sportshop.user.dto.UpdateProfileRequest;
import com.sportshop.user.dto.UserResponse;
import com.sportshop.user.service.UserService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

/** Perfil del cliente autenticado: el id se toma siempre del token, nunca de la URL (evita IDOR). */
@RestController
@RequestMapping("/api/users/me")
@Tag(name = "Perfil del cliente")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @Operation(summary = "Consultar el perfil")
    @GetMapping
    public UserResponse getProfile(@AuthenticationPrincipal Jwt jwt) {
        return userService.getProfile(AuthenticatedUser.from(jwt).id());
    }

    @Operation(summary = "Actualizar el perfil completo")
    @PutMapping
    public UserResponse updateProfile(@AuthenticationPrincipal Jwt jwt,
                                      @Valid @RequestBody UpdateProfileRequest request) {
        return userService.updateProfile(AuthenticatedUser.from(jwt).id(), request);
    }

    @Operation(summary = "Actualizar solo la dirección de envío")
    @PatchMapping("/shipping-address")
    public UserResponse updateShippingAddress(@AuthenticationPrincipal Jwt jwt,
                                              @Valid @RequestBody UpdateAddressRequest request) {
        return userService.updateShippingAddress(AuthenticatedUser.from(jwt).id(), request);
    }

    @Operation(summary = "Cambiar la contraseña")
    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @Valid @RequestBody ChangePasswordRequest request) {
        userService.changePassword(AuthenticatedUser.from(jwt).id(), request);
    }

    @Operation(summary = "Eliminar la cuenta")
    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAccount(@AuthenticationPrincipal Jwt jwt) {
        userService.deleteAccount(AuthenticatedUser.from(jwt).id());
    }
}
