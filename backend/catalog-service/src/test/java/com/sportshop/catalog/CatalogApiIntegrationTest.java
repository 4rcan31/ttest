package com.sportshop.catalog;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.everyItem;
import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.head;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.sportshop.common.internal.InternalApiProperties;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
class CatalogApiIntegrationTest {

    private static final String KEY = "test-internal-key";

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listsCatalogAnonymouslyWithPagination() throws Exception {
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(12)))
                .andExpect(jsonPath("$.totalElements").value(18))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.content[0].name").value("Balón de Fútbol Pro Match"))
                .andExpect(jsonPath("$.content[0].imageUrl").value("/api/products/images/balon-futbol.svg"))
                .andExpect(jsonPath("$.content[0].category.slug").value("futbol"));
    }

    @Test
    void searchesByTextCategoryAndStock() throws Exception {
        mockMvc.perform(get("/api/products").param("q", "zapatillas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)));

        mockMvc.perform(get("/api/products").param("category", "fitness").param("inStock", "true"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(4)))
                .andExpect(jsonPath("$.content[*].stock", everyItem(greaterThan(0))));

        mockMvc.perform(get("/api/products").param("sort", "price_desc").param("size", "1"))
                .andExpect(jsonPath("$.content[0].sku").value("RUN-002"));

        mockMvc.perform(get("/api/products").param("q", "100%_"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(0)));
    }

    @Test
    void returnsProductDetailAndCategories() throws Exception {
        mockMvc.perform(get("/api/products/14"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sku").value("FIT-004"))
                .andExpect(jsonPath("$.available").value(false));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        mockMvc.perform(get("/api/products/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(7)));
    }

    @Test
    void servesProductImages() throws Exception {
        mockMvc.perform(get("/api/products/images/balon-futbol.svg"))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("image/svg+xml"))
                .andExpect(header().string("Cache-Control", containsString("max-age")));
        mockMvc.perform(head("/api/products/images/balon-futbol.svg"))
                .andExpect(status().isOk());
    }

    @Test
    void internalEndpointsRequireApiKey() throws Exception {
        mockMvc.perform(get("/internal/products").param("ids", "1,2"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/internal/products").param("ids", "1,2")
                        .header(InternalApiProperties.HEADER, "clave-incorrecta"))
                .andExpect(status().isUnauthorized());
        mockMvc.perform(get("/internal/products").param("ids", "1,2").header(InternalApiProperties.HEADER, KEY))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    void reservesAndReleasesStockAtomically() throws Exception {
        // Guantes de portero (id 3): 18 unidades
        mockMvc.perform(post("/internal/products/reserve").header(InternalApiProperties.HEADER, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":3,\"quantity\":2},{\"productId\":1,\"quantity\":1}]}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[1].productId").value(3))
                .andExpect(jsonPath("$[1].unitPrice").value(34.50));
        mockMvc.perform(get("/api/products/3")).andExpect(jsonPath("$.stock").value(16));

        // Si un artículo no alcanza, no se descuenta ninguno (todo o nada).
        mockMvc.perform(post("/internal/products/reserve").header(InternalApiProperties.HEADER, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":3,\"quantity\":1},{\"productId\":13,\"quantity\":5}]}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"))
                .andExpect(jsonPath("$.message", containsString("Cuerda para Saltar")));
        mockMvc.perform(get("/api/products/3")).andExpect(jsonPath("$.stock").value(16));

        mockMvc.perform(post("/internal/products/release").header(InternalApiProperties.HEADER, KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"items\":[{\"productId\":3,\"quantity\":2},{\"productId\":1,\"quantity\":1}]}"))
                .andExpect(status().isNoContent());
        mockMvc.perform(get("/api/products/3")).andExpect(jsonPath("$.stock").value(18));
    }

    @Test
    void writeOperationsOnPublicApiRequireAuthentication() throws Exception {
        mockMvc.perform(post("/api/products").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isUnauthorized());
    }
}
