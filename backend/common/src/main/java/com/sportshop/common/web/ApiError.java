package com.sportshop.common.web;

import java.time.Instant;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Formato único de error que devuelven todos los microservicios.
 *
 * @param code        código funcional estable para que el frontend pueda reaccionar (p. ej. INSUFFICIENT_STOCK)
 * @param fieldErrors errores de validación por campo (solo en respuestas 400 de validación)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiError(
        Instant timestamp,
        int status,
        String error,
        String code,
        String message,
        String path,
        Map<String, String> fieldErrors) {

    public static ApiError of(int status, String error, String code, String message, String path) {
        return new ApiError(Instant.now(), status, error, code, message, path, null);
    }
}
