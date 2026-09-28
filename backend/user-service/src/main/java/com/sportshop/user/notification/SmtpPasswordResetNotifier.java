package com.sportshop.user.notification;

import java.nio.charset.StandardCharsets;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.web.util.HtmlUtils;

/** Envía el enlace por correo electrónico (en docker-compose se captura con Mailpit). */
public class SmtpPasswordResetNotifier implements PasswordResetNotifier {

    private static final Logger log = LoggerFactory.getLogger(SmtpPasswordResetNotifier.class);

    private final JavaMailSender mailSender;
    private final String from;

    public SmtpPasswordResetNotifier(JavaMailSender mailSender, String from) {
        this.mailSender = mailSender;
        this.from = from;
    }

    @Override
    public void sendResetLink(PasswordResetRequestedEvent event) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, StandardCharsets.UTF_8.name());
            helper.setFrom(from);
            helper.setTo(event.email());
            helper.setSubject("SportShop - Recuperación de contraseña");
            helper.setText(buildBody(event), true);
            mailSender.send(message);
            log.info("Correo de recuperación enviado al usuario id={}", event.userId());
        } catch (MessagingException ex) {
            throw new MailSendException("No se pudo construir el correo de recuperación", ex);
        }
    }

    private String buildBody(PasswordResetRequestedEvent event) {
        String name = HtmlUtils.htmlEscape(event.firstName());
        String link = HtmlUtils.htmlEscape(event.resetLink());
        long minutes = event.validity().toMinutes();
        return """
                <div style="font-family:Arial,sans-serif;max-width:520px;margin:auto;color:#1f2937">
                  <h2 style="color:#0f766e">Hola %s,</h2>
                  <p>Recibimos una solicitud para restablecer la contraseña de tu cuenta en SportShop.</p>
                  <p style="text-align:center;margin:28px 0">
                    <a href="%s" style="background:#0f766e;color:#fff;padding:12px 22px;border-radius:8px;
                       text-decoration:none;font-weight:bold">Restablecer contraseña</a>
                  </p>
                  <p>El enlace es válido durante %d minutos y solo puede usarse una vez.</p>
                  <p style="color:#6b7280;font-size:13px">Si no solicitaste este cambio, ignora este correo:
                    tu contraseña actual seguirá funcionando.</p>
                </div>
                """.formatted(name, link, minutes);
    }
}
