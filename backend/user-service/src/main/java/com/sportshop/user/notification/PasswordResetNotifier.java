package com.sportshop.user.notification;

/** Canal por el que se entrega el enlace de recuperación de contraseña. */
public interface PasswordResetNotifier {

    void sendResetLink(PasswordResetRequestedEvent event);
}
