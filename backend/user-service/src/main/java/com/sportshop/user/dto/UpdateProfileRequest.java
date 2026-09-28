package com.sportshop.user.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import com.sportshop.user.validation.Adult;
import com.sportshop.user.validation.ValidationRules;

/** Actualización completa del perfil (PUT). La contraseña se cambia en un endpoint dedicado. */
public record UpdateProfileRequest(
        @NotBlank(message = "Los nombres son obligatorios")
        @Size(min = 2, max = 80, message = "Los nombres deben tener entre 2 y 80 caracteres")
        @Pattern(regexp = ValidationRules.NAME_PATTERN, message = "Los nombres solo pueden contener letras")
        String firstName,

        @NotBlank(message = "Los apellidos son obligatorios")
        @Size(min = 2, max = 80, message = "Los apellidos deben tener entre 2 y 80 caracteres")
        @Pattern(regexp = ValidationRules.NAME_PATTERN, message = "Los apellidos solo pueden contener letras")
        String lastName,

        @NotBlank(message = "La dirección de envío es obligatoria")
        @Size(min = 10, max = 255, message = "La dirección de envío debe tener entre 10 y 255 caracteres")
        String shippingAddress,

        @NotBlank(message = "El correo electrónico es obligatorio")
        @Size(max = 120, message = "El correo electrónico no puede superar 120 caracteres")
        @Pattern(regexp = ValidationRules.EMAIL_PATTERN, message = "El formato del correo electrónico no es válido")
        String email,

        @NotNull(message = "La fecha de nacimiento es obligatoria")
        @Adult
        LocalDate birthDate) {
}
