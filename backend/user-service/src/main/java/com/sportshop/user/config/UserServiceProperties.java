package com.sportshop.user.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @param frontendUrl   URL pública del frontend, usada para construir el enlace de recuperación
 * @param passwordReset configuración del token de recuperación de contraseña
 * @param login         política de bloqueo ante intentos fallidos (mitigación de fuerza bruta)
 * @param mail          envío de correos
 */
@ConfigurationProperties("app")
public record UserServiceProperties(String frontendUrl, PasswordReset passwordReset, Login login, Mail mail) {

    public UserServiceProperties {
        if (frontendUrl == null) {
            frontendUrl = "http://localhost:5173";
        }
        if (passwordReset == null) {
            passwordReset = new PasswordReset(null);
        }
        if (login == null) {
            login = new Login(0, null);
        }
        if (mail == null) {
            mail = new Mail(false, null);
        }
    }

    public record PasswordReset(Duration tokenTtl) {
        public PasswordReset {
            if (tokenTtl == null) {
                tokenTtl = Duration.ofMinutes(30);
            }
        }
    }

    public record Login(int maxFailedAttempts, Duration lockDuration) {
        public Login {
            if (maxFailedAttempts <= 0) {
                maxFailedAttempts = 5;
            }
            if (lockDuration == null) {
                lockDuration = Duration.ofMinutes(15);
            }
        }
    }

    public record Mail(boolean enabled, String from) {
        public Mail {
            if (from == null) {
                from = "SportShop <no-reply@sportshop.local>";
            }
        }
    }
}
