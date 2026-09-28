package com.sportshop.common.security;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuración del token JWT compartida por todos los microservicios.
 *
 * @param secret     clave HMAC (mínimo 32 bytes) usada para firmar/validar los tokens
 * @param issuer     emisor esperado en el claim {@code iss}
 * @param expiration tiempo de vida del token de acceso
 */
@ConfigurationProperties("app.jwt")
public record JwtProperties(String secret, String issuer, Duration expiration) {

    public JwtProperties {
        if (issuer == null || issuer.isBlank()) {
            issuer = "sportshop";
        }
        if (expiration == null) {
            expiration = Duration.ofMinutes(60);
        }
    }
}
