package com.sportshop.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Actualización parcial (PATCH) de la dirección de envío, usada desde el checkout. */
public record UpdateAddressRequest(
        @NotBlank(message = "La dirección de envío es obligatoria")
        @Size(min = 10, max = 255, message = "La dirección de envío debe tener entre 10 y 255 caracteres")
        String shippingAddress) {
}
