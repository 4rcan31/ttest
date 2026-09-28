package com.sportshop.order.client;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sportshop.common.web.ApiException;

import tools.jackson.databind.ObjectMapper;

/**
 * Cliente de la API interna de catalog-service. Traduce los errores de negocio del catálogo
 * (404/409) a {@link ApiException} y los fallos técnicos a 503.
 */
@Component
public class CatalogClient {

    private static final Logger log = LoggerFactory.getLogger(CatalogClient.class);
    private static final Set<Integer> PROPAGATED_STATUSES = Set.of(400, 404, 409);

    private final RestClient restClient;
    private final ObjectMapper objectMapper;

    public CatalogClient(RestClient catalogRestClient, ObjectMapper objectMapper) {
        this.restClient = catalogRestClient;
        this.objectMapper = objectMapper;
    }

    public List<CatalogProduct> getProducts(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return call(() -> restClient.get()
                .uri(uri -> uri.path("/internal/products").queryParam("ids", ids.toArray()).build())
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw toApiException(response.getStatusCode(), response.getBody().readAllBytes());
                })
                .body(new ParameterizedTypeReference<List<CatalogProduct>>() {
                }));
    }

    public List<ReservedProduct> reserve(List<StockItem> items) {
        return call(() -> restClient.post()
                .uri("/internal/products/reserve")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("items", items))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw toApiException(response.getStatusCode(), response.getBody().readAllBytes());
                })
                .body(new ParameterizedTypeReference<List<ReservedProduct>>() {
                }));
    }

    public void release(List<StockItem> items) {
        call(() -> restClient.post()
                .uri("/internal/products/release")
                .contentType(MediaType.APPLICATION_JSON)
                .body(Map.of("items", items))
                .retrieve()
                .onStatus(HttpStatusCode::isError, (request, response) -> {
                    throw toApiException(response.getStatusCode(), response.getBody().readAllBytes());
                })
                .toBodilessEntity());
    }

    private <T> T call(Supplier<T> action) {
        try {
            return action.get();
        } catch (ApiException ex) {
            throw ex;
        } catch (ResourceAccessException ex) {
            log.error("catalog-service no responde: {}", ex.getMessage());
            throw unavailable();
        }
    }

    private ApiException toApiException(HttpStatusCode status, byte[] body) {
        if (PROPAGATED_STATUSES.contains(status.value())) {
            try {
                CatalogError error = objectMapper.readValue(body, CatalogError.class);
                return new ApiException(HttpStatus.valueOf(status.value()), error.code(), error.message());
            } catch (RuntimeException parseError) {
                log.warn("Respuesta de error del catálogo no reconocida ({})", status.value());
            }
        }
        log.error("Error inesperado de catalog-service: HTTP {}", status.value());
        return unavailable();
    }

    private static ApiException unavailable() {
        return ApiException.serviceUnavailable("CATALOG_UNAVAILABLE",
                "El catálogo no está disponible en este momento. Intente nuevamente en unos minutos.");
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record CatalogError(String code, String message) {
    }
}
