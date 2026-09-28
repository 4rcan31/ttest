package com.sportshop.common.web;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/** Orígenes permitidos para llamadas directas desde el navegador (sin pasar por el gateway). */
@ConfigurationProperties("app.cors")
public record CorsProperties(List<String> allowedOrigins) {

    public CorsProperties {
        if (allowedOrigins == null) {
            allowedOrigins = List.of("http://localhost:5173", "http://localhost:8080");
        }
    }
}
