package com.sportshop.user.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import com.sportshop.common.web.ApiException;
import com.sportshop.user.config.UserServiceProperties;
import com.sportshop.user.domain.PasswordResetToken;
import com.sportshop.user.domain.User;
import com.sportshop.user.notification.PasswordResetRequestedEvent;
import com.sportshop.user.repository.PasswordResetTokenRepository;
import com.sportshop.user.repository.UserRepository;

/**
 * Recuperación de contraseña con token aleatorio de un solo uso y vigencia limitada.
 * La respuesta es siempre la misma exista o no el correo, para no revelar cuentas registradas.
 */
@Service
public class PasswordResetService {

    private static final Logger log = LoggerFactory.getLogger(PasswordResetService.class);
    private static final int TOKEN_BYTES = 32;

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;
    private final UserServiceProperties properties;
    private final Clock clock;
    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository,
                                PasswordEncoder passwordEncoder, ApplicationEventPublisher eventPublisher,
                                UserServiceProperties properties, Clock clock) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
        this.properties = properties;
        this.clock = clock;
    }

    @Transactional
    public void requestReset(String rawEmail) {
        userRepository.findByEmail(AuthService.normalizeEmail(rawEmail)).ifPresent(user -> {
            Instant now = clock.instant();
            tokenRepository.invalidateActiveTokens(user.getId(), now);

            String token = generateToken();
            tokenRepository.save(new PasswordResetToken(user, sha256(token),
                    now.plus(properties.passwordReset().tokenTtl())));

            String link = UriComponentsBuilder.fromUriString(properties.frontendUrl())
                    .path("/restablecer-password")
                    .queryParam("token", token)
                    .toUriString();
            // El correo se envía de forma asíncrona tras el commit: el tiempo de respuesta no revela
            // si la cuenta existe y no se envían enlaces de tokens que no llegaron a persistirse.
            eventPublisher.publishEvent(new PasswordResetRequestedEvent(user.getId(), user.getEmail(),
                    user.getFirstName(), link, properties.passwordReset().tokenTtl()));
        });
    }

    @Transactional
    public void resetPassword(String token, String newPassword) {
        Instant now = clock.instant();
        PasswordResetToken resetToken = tokenRepository.findByTokenHash(sha256(token))
                .filter(t -> t.isUsable(now))
                .orElseThrow(() -> ApiException.badRequest("INVALID_RESET_TOKEN",
                        "El enlace de recuperación no es válido o ya expiró. Solicite uno nuevo."));

        User user = resetToken.getUser();
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.resetLoginAttempts();
        resetToken.markUsed(now);
        log.info("Contraseña restablecida para el usuario id={}", user.getId());
    }

    private String generateToken() {
        byte[] bytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(bytes);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    static String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("SHA-256 no disponible", ex);
        }
    }
}
