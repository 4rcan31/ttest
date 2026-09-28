package com.sportshop.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

import com.sportshop.user.validation.ValidationRules;

public record ForgotPasswordRequest(
        @NotBlank(message = "El correo electrónico es obligatorio")
        @Pattern(regexp = ValidationRules.EMAIL_PATTERN, message = "El formato del correo electrónico no es válido")
        String email) {
}
