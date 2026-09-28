package com.sportshop.order.service;

import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

/**
 * Genera números de orden legibles y no secuenciales, p. ej. {@code SS-260928-K7Q2MX}.
 * No usar el id autoincremental evita exponer el volumen de ventas y adivinar órdenes ajenas.
 */
@Component
public class OrderNumberGenerator {

    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("yyMMdd");
    private static final int RANDOM_LENGTH = 6;

    private final SecureRandom random = new SecureRandom();
    private final Clock clock;

    public OrderNumberGenerator(Clock clock) {
        this.clock = clock;
    }

    public String next() {
        StringBuilder sb = new StringBuilder("SS-").append(LocalDate.now(clock).format(DATE)).append('-');
        for (int i = 0; i < RANDOM_LENGTH; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}
