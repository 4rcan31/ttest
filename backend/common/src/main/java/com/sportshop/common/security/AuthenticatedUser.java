package com.sportshop.common.security;

import java.util.List;

import org.springframework.security.oauth2.jwt.Jwt;

/**
 * Datos del usuario autenticado extraídos de los claims del JWT.
 * El {@code sub} del token es el id del usuario en user-service.
 */
public record AuthenticatedUser(Long id, String email, String name, List<String> roles) {

    public static final String CLAIM_EMAIL = "email";
    public static final String CLAIM_NAME = "name";
    public static final String CLAIM_ROLES = "roles";

    public static AuthenticatedUser from(Jwt jwt) {
        List<String> roles = jwt.getClaimAsStringList(CLAIM_ROLES);
        return new AuthenticatedUser(
                Long.valueOf(jwt.getSubject()),
                jwt.getClaimAsString(CLAIM_EMAIL),
                jwt.getClaimAsString(CLAIM_NAME),
                roles == null ? List.of() : roles);
    }

    public boolean isAdmin() {
        return roles.contains("ADMIN");
    }
}
