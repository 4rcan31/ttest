package com.sportshop.order;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.matchesPattern;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.JwtRequestPostProcessor;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.jayway.jsonpath.JsonPath;
import com.sportshop.common.web.ApiException;
import com.sportshop.order.client.CatalogClient;
import com.sportshop.order.client.CatalogProduct;
import com.sportshop.order.client.ReservedProduct;
import com.sportshop.order.client.StockItem;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OrderApiIntegrationTest {

    private static final AtomicLong USER_IDS = new AtomicLong(100);
    private static final Map<Long, CatalogProduct> CATALOG = Map.of(
            1L, new CatalogProduct(1L, "FUT-001", "Balón de Fútbol Pro Match", new BigDecimal("39.99"), 25,
                    "/api/products/images/balon-futbol.svg"),
            13L, new CatalogProduct(13L, "FIT-003", "Cuerda para Saltar Speed Pro", new BigDecimal("12.50"), 3,
                    "/api/products/images/cuerda-saltar.svg"));

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CatalogClient catalogClient;

    private JwtRequestPostProcessor customer;

    @BeforeEach
    void setUp() {
        customer = customer(USER_IDS.incrementAndGet());
        given(catalogClient.getProducts(anyCollection())).willAnswer(invocation -> {
            Collection<Long> ids = invocation.getArgument(0);
            return ids.stream().filter(CATALOG::containsKey).map(CATALOG::get).toList();
        });
        given(catalogClient.reserve(anyList())).willAnswer(invocation -> {
            List<StockItem> items = invocation.getArgument(0);
            return items.stream().map(item -> {
                CatalogProduct p = CATALOG.get(item.productId());
                return new ReservedProduct(p.id(), p.sku(), p.name(), p.imageUrl(), p.price(), item.quantity());
            }).toList();
        });
    }

    @Test
    void requiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/cart")).andExpect(status().isUnauthorized());
        mockMvc.perform(get("/api/orders")).andExpect(status().isUnauthorized());
    }

    @Test
    void managesCartItems() throws Exception {
        addToCart(1L, 1)
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.subtotal").value(39.99))
                .andExpect(jsonPath("$.shippingCost").value(5.00))
                .andExpect(jsonPath("$.total").value(44.99))
                .andExpect(jsonPath("$.readyForCheckout").value(true));

        // Sumar cantidad de un artículo existente: envío gratis al superar $75
        addToCart(1L, 1)
                .andExpect(jsonPath("$.items[0].quantity").value(2))
                .andExpect(jsonPath("$.subtotal").value(79.98))
                .andExpect(jsonPath("$.shippingCost").value(0.00));

        addToCart(13L, 4)
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        addToCart(999L, 1)
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("PRODUCT_NOT_FOUND"));

        mockMvc.perform(put("/api/cart/items/1").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":3}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].quantity").value(3))
                .andExpect(jsonPath("$.totalItems").value(3));

        mockMvc.perform(put("/api/cart/items/1").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"quantity\":0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.quantity").value("La cantidad mínima es 1"));

        mockMvc.perform(delete("/api/cart/items/1").with(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items", hasSize(0)))
                .andExpect(jsonPath("$.readyForCheckout").value(false));

        mockMvc.perform(delete("/api/cart/items/1").with(customer))
                .andExpect(status().isNotFound());
    }

    @Test
    void confirmsOrderAndEmptiesCart() throws Exception {
        mockMvc.perform(post("/api/orders").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("Colonia Escalón, Calle La Mascota #123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("CART_EMPTY"));

        addToCart(1L, 2);
        addToCart(13L, 1);

        mockMvc.perform(post("/api/orders").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("Nueva dirección editada en el checkout 456")))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", matchesPattern("/api/orders/SS-\\d{6}-[A-Z0-9]{6}")))
                .andExpect(jsonPath("$.orderNumber", matchesPattern("SS-\\d{6}-[A-Z0-9]{6}")))
                .andExpect(jsonPath("$.status").value("CONFIRMED"))
                .andExpect(jsonPath("$.statusLabel").value("Confirmada"))
                .andExpect(jsonPath("$.shippingAddress").value("Nueva dirección editada en el checkout 456"))
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.itemCount").value(3))
                .andExpect(jsonPath("$.subtotal").value(92.48))
                .andExpect(jsonPath("$.shippingCost").value(0.00))
                .andExpect(jsonPath("$.total").value(92.48))
                .andExpect(jsonPath("$.history", hasSize(1)))
                .andExpect(jsonPath("$.customerName").value("Cliente Prueba"));

        mockMvc.perform(get("/api/cart").with(customer))
                .andExpect(jsonPath("$.items", hasSize(0)));
    }

    @Test
    void keepsCartWhenStockReservationFails() throws Exception {
        addToCart(1L, 1);
        willThrow(ApiException.conflict("INSUFFICIENT_STOCK", "No hay inventario suficiente"))
                .given(catalogClient).reserve(anyList());

        mockMvc.perform(post("/api/orders").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("Colonia Escalón, Calle La Mascota #123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_STOCK"));

        mockMvc.perform(get("/api/cart").with(customer)).andExpect(jsonPath("$.items", hasSize(1)));
        mockMvc.perform(get("/api/orders").with(customer)).andExpect(jsonPath("$.totalElements").value(0));
        verify(catalogClient, never()).release(anyList());
    }

    @Test
    void listsOrdersAndHidesOrdersFromOtherCustomers() throws Exception {
        String orderNumber = placeOrder();

        mockMvc.perform(get("/api/orders").with(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(1)))
                .andExpect(jsonPath("$.content[0].orderNumber").value(orderNumber))
                .andExpect(jsonPath("$.content[0].previewImages", hasSize(1)));

        mockMvc.perform(get("/api/orders/" + orderNumber).with(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items[0].name").value("Balón de Fútbol Pro Match"))
                .andExpect(jsonPath("$.cancellable").value(true));

        mockMvc.perform(get("/api/orders/" + orderNumber).with(customer(USER_IDS.incrementAndGet())))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_FOUND"));
    }

    @Test
    void cancelsOrderAndReleasesStock() throws Exception {
        String orderNumber = placeOrder();

        mockMvc.perform(post("/api/orders/" + orderNumber + "/cancel").with(customer))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.history", hasSize(2)))
                .andExpect(jsonPath("$.cancellable").value(false));
        verify(catalogClient).release(List.of(new StockItem(1L, 1)));

        mockMvc.perform(post("/api/orders/" + orderNumber + "/cancel").with(customer))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("ORDER_NOT_CANCELLABLE"));
    }

    @Test
    void adminAdvancesOrderStatus() throws Exception {
        String orderNumber = placeOrder();
        JwtRequestPostProcessor admin = jwt()
                .jwt(j -> j.subject("1").claim("email", "admin@sportshop.com").claim("roles", List.of("ADMIN")))
                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN"));

        mockMvc.perform(get("/api/admin/orders").with(customer))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/admin/orders").param("status", "CONFIRMED").with(admin))
                .andExpect(status().isOk());

        mockMvc.perform(patch("/api/admin/orders/" + orderNumber + "/status").with(admin)
                        .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"DELIVERED\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("INVALID_STATUS_TRANSITION"));

        for (String next : List.of("PROCESSING", "SHIPPED", "DELIVERED")) {
            mockMvc.perform(patch("/api/admin/orders/" + orderNumber + "/status").with(admin)
                            .contentType(MediaType.APPLICATION_JSON).content("{\"status\":\"" + next + "\"}"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value(next));
        }

        mockMvc.perform(get("/api/orders/" + orderNumber).with(customer))
                .andExpect(jsonPath("$.statusLabel").value("Entregada"))
                .andExpect(jsonPath("$.history", hasSize(4)))
                .andExpect(jsonPath("$.history[3].changedBy").value("ADMIN"));
    }

    private String placeOrder() throws Exception {
        addToCart(1L, 1);
        MvcResult result = mockMvc.perform(post("/api/orders").with(customer).contentType(MediaType.APPLICATION_JSON)
                        .content(orderJson("Colonia Escalón, Calle La Mascota #123")))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.orderNumber");
    }

    private ResultActions addToCart(Long productId, int quantity)
            throws Exception {
        return mockMvc.perform(post("/api/cart/items").with(customer).contentType(MediaType.APPLICATION_JSON)
                .content("{\"productId\":" + productId + ",\"quantity\":" + quantity + "}"));
    }

    private static JwtRequestPostProcessor customer(long userId) {
        return jwt().jwt(j -> j.subject(String.valueOf(userId))
                        .claim("email", "cliente" + userId + "@test.com")
                        .claim("name", "Cliente Prueba")
                        .claim("roles", List.of("CUSTOMER")))
                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
    }

    private static String orderJson(String address) {
        return "{\"shippingAddress\":\"" + address + "\"}";
    }
}
