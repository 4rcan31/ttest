package com.sportshop.user.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Alternativa para desarrollo local sin servidor SMTP: escribe el enlace en el log.
 * Nunca debe activarse en producción ({@code app.mail.enabled=true}).
 */
public class LoggingPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(LoggingPasswordResetNotifier.class);

    @Override
    public void sendResetLink(PasswordResetRequestedEvent event) {
        log.warn("[DEV] Envío de correo deshabilitado. Enlace de recuperación para {}: {}",
                event.email(), event.resetLink());
    }
}
