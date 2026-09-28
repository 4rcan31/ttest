package com.sportshop.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.sportshop.user.validation.ValidationRules;

public record ResetPasswordRequest(
        @NotBlank(message = "El token de recuperación es obligatorio")
        String token,

        @NotBlank(message = "La nueva contraseña es obligatoria")
        @Pattern(regexp = ValidationRules.PASSWORD_PATTERN, message = ValidationRules.PASSWORD_MESSAGE)
        String newPassword) {
}
