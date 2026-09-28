package com.sportshop.user.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.notNullValue;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.jayway.jsonpath.JsonPath;
import com.sportshop.user.notification.PasswordResetNotifier;
import com.sportshop.user.notification.PasswordResetRequestedEvent;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserApiIntegrationTest {

    private static final String ADULT_BIRTH_DATE = LocalDate.now().minusYears(25).toString();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PasswordResetNotifier notifier;

    @Test
    void registersUserAndReturnsToken() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Ana", "ana.registro@test.com", ADULT_BIRTH_DATE, "Secreta123")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.user.email").value("ana.registro@test.com"))
                .andExpect(jsonPath("$.user.role").value("CUSTOMER"))
                .andExpect(jsonPath("$.user.passwordHash").doesNotExist());
    }

    @Test
    void rejectsInvalidRegistrationWithFieldErrors() throws Exception {
        String underage = LocalDate.now().minusYears(17).toString();
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("", "correo-invalido", underage, "corta")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("VALIDATION_ERROR"))
                .andExpect(jsonPath("$.fieldErrors.firstName", notNullValue()))
                .andExpect(jsonPath("$.fieldErrors.email").value("El formato del correo electrónico no es válido"))
                .andExpect(jsonPath("$.fieldErrors.birthDate").value("Debe ser mayor de 18 años"))
                .andExpect(jsonPath("$.fieldErrors.password", containsString("mayúscula")));
    }

    @Test
    void rejectsMissingFields() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").value("Los nombres son obligatorios"))
                .andExpect(jsonPath("$.fieldErrors.lastName").value("Los apellidos son obligatorios"))
                .andExpect(jsonPath("$.fieldErrors.shippingAddress").value("La dirección de envío es obligatoria"))
                .andExpect(jsonPath("$.fieldErrors.email").value("El correo electrónico es obligatorio"))
                .andExpect(jsonPath("$.fieldErrors.birthDate").value("La fecha de nacimiento es obligatoria"))
                .andExpect(jsonPath("$.fieldErrors.password").value("La contraseña es obligatoria"));
    }

    @Test
    void rejectsDuplicatedEmail() throws Exception {
        mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson("Otro", "CLIENTE@sportshop.com", ADULT_BIRTH_DATE, "Secreta123")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.code").value("EMAIL_ALREADY_REGISTERED"));
    }

    @Test
    void logsInWithSeededUser() throws Exception {
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("cliente@sportshop.com", "Demo1234")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken", notNullValue()))
                .andExpect(jsonPath("$.user.firstName").value("María José"));
    }

    @Test
    void rejectsWrongPasswordAndLocksAccountAfterFiveAttempts() throws Exception {
        String email = "bloqueo@test.com";
        register("Bloqueo", email);
        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                            .content(loginJson(email, "Incorrecta1")))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.code").value("INVALID_CREDENTIALS"));
        }
        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson(email, "Secreta123")))
                .andExpect(status().isLocked())
                .andExpect(jsonPath("$.code").value("ACCOUNT_LOCKED"));
    }

    @Test
    void profileRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/api/users/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.code").value("UNAUTHORIZED"));
        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer token.invalido.x"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void readsAndUpdatesProfile() throws Exception {
        String token = register("Perfil", "perfil@test.com");

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("perfil@test.com"));

        mockMvc.perform(put("/api/users/me").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"firstName":"Perfil Editado","lastName":"Gómez","email":"perfil@test.com",
                                 "shippingAddress":"Nueva dirección de envío 456","birthDate":"%s"}
                                """.formatted(ADULT_BIRTH_DATE)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Perfil Editado"))
                .andExpect(jsonPath("$.shippingAddress").value("Nueva dirección de envío 456"));

        mockMvc.perform(patch("/api/users/me/shipping-address").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"shippingAddress\":\"Dirección desde el checkout 789\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.shippingAddress").value("Dirección desde el checkout 789"));
    }

    @Test
    void changesPasswordAndDeletesAccount() throws Exception {
        String token = register("Borrar", "borrar@test.com");

        mockMvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"Mala12345\",\"newPassword\":\"NuevaClave1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_CURRENT_PASSWORD"));

        mockMvc.perform(put("/api/users/me/password").header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currentPassword\":\"Secreta123\",\"newPassword\":\"NuevaClave1\"}"))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("borrar@test.com", "NuevaClave1")))
                .andExpect(status().isOk());

        mockMvc.perform(delete("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/users/me").header("Authorization", "Bearer " + token))
                .andExpect(status().isNotFound());
    }

    @Test
    void recoversPasswordWithSingleUseToken() throws Exception {
        register("Recuperar", "recuperar@test.com");

        mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"recuperar@test.com\"}"))
                .andExpect(status().isAccepted());

        ArgumentCaptor<PasswordResetRequestedEvent> captor = ArgumentCaptor.forClass(PasswordResetRequestedEvent.class);
        verify(notifier, timeout(3000)).sendResetLink(captor.capture());
        String link = captor.getValue().resetLink();
        assertThat(link).startsWith("http://localhost:5173/restablecer-password?token=");
        String token = link.substring(link.indexOf("token=") + 6);

        String resetBody = "{\"token\":\"" + token + "\",\"newPassword\":\"Recuperada1\"}";
        mockMvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(resetBody))
                .andExpect(status().isOk());
        mockMvc.perform(post("/api/auth/reset-password").contentType(MediaType.APPLICATION_JSON).content(resetBody))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_RESET_TOKEN"));

        mockMvc.perform(post("/api/auth/login").contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson("recuperar@test.com", "Recuperada1")))
                .andExpect(status().isOk());
    }

    @Test
    void forgotPasswordDoesNotRevealUnknownEmails() throws Exception {
        mockMvc.perform(post("/api/auth/forgot-password").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"no-existe@test.com\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.message", containsString("Si el correo está registrado")));
    }

    private String register(String firstName, String email) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
                        .content(registerJson(firstName, email, ADULT_BIRTH_DATE, "Secreta123")))
                .andExpect(status().isCreated())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.accessToken");
    }

    private static String registerJson(String firstName, String email, String birthDate, String password) {
        return """
                {"firstName":"%s","lastName":"Pruebas","shippingAddress":"Calle de prueba 123, Ciudad",
                 "email":"%s","birthDate":"%s","password":"%s"}
                """.formatted(firstName, email, birthDate, password);
    }

    private static String loginJson(String email, String password) {
        return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\"}";
    }
}
