package com.sportshop.catalog.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import com.sportshop.common.internal.InternalApiKeyAuthorizationManager;
import com.sportshop.common.internal.InternalApiProperties;
import com.sportshop.common.security.ApiSecurityConfigurer;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, ApiSecurityConfigurer apiSecurity,
                                            InternalApiProperties internalApiProperties) throws Exception {
        InternalApiKeyAuthorizationManager internalAccess = new InternalApiKeyAuthorizationManager(internalApiProperties);
        apiSecurity.applyDefaults(http)
                .authorizeHttpRequests(auth -> auth
                        // El catálogo es público: la navegación anónima representa la mayor parte del tráfico.
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()
                        // Comunicación servicio-a-servicio (order-service); el gateway no expone /internal.
                        .requestMatchers("/internal/**").access(internalAccess)
                        .requestMatchers("/actuator/health/**", "/v3/api-docs/**",
                                "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .anyRequest().authenticated());
        return http.build();
    }
}
