package com.sportshop.common.internal;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Clave compartida para la comunicación servicio-a-servicio (endpoints {@code /internal/**}).
 * Estos endpoints no se exponen a través del API Gateway.
 */
@ConfigurationProperties("app.internal")
public record InternalApiProperties(String apiKey) {

    public static final String HEADER = "X-Internal-Api-Key";
}
