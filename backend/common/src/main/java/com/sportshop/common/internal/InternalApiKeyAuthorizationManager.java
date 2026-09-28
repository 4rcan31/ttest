package com.sportshop.common.internal;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.function.Supplier;

import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationResult;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

/**
 * Autoriza las llamadas internas comparando el header {@value InternalApiProperties#HEADER}
 * contra la clave configurada en tiempo constante (evita ataques de timing).
 */
public class InternalApiKeyAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final byte[] expectedKey;

    public InternalApiKeyAuthorizationManager(InternalApiProperties properties) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new IllegalStateException("Debe configurar app.internal.api-key (variable INTERNAL_API_KEY)");
        }
        this.expectedKey = properties.apiKey().getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public AuthorizationResult authorize(Supplier<? extends Authentication> authentication,
                                         RequestAuthorizationContext context) {
        String provided = context.getRequest().getHeader(InternalApiProperties.HEADER);
        boolean granted = provided != null
                && MessageDigest.isEqual(expectedKey, provided.getBytes(StandardCharsets.UTF_8));
        return new AuthorizationDecision(granted);
    }
}
