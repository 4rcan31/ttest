package com.sportshop.user.notification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
public class PasswordResetEmailListener {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetEmailListener.class);

    private final PasswordResetNotifier notifier;

    public PasswordResetEmailListener(PasswordResetNotifier notifier) {
        this.notifier = notifier;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onPasswordResetRequested(PasswordResetRequestedEvent event) {
        try {
            notifier.sendResetLink(event);
        } catch (RuntimeException ex) {
            log.error("No fue posible enviar el correo de recuperación al usuario id={}", event.userId(), ex);
        }
    }
}
