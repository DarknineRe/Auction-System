package com.example.project.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserApiIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void registersAuthenticatesUpdatesAndChangesPassword() throws Exception {
        UserFixture user = registerUser("User Flow");

        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/users/me")
                        .with(httpBasic(user.email(), user.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(user.email()));

        mockMvc.perform(put("/api/v1/users/me")
                        .with(httpBasic(user.email(), user.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Updated Test User","phone":"0899999999","address":"Updated address"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test User"));

        mockMvc.perform(put("/api/v1/users/me/password")
                        .with(httpBasic(user.email(), user.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"currentPassword":"TestPassword123!","newPassword":"NewPassword456!"}
                                """))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/users/me")
                        .with(httpBasic(user.email(), "NewPassword456!")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Test User"));

        mockMvc.perform(get("/api/v1/users/me")
                        .with(httpBasic(user.email(), user.password())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void rejectsInvalidAndDuplicateRegistration() throws Exception {
        UserFixture user = registerUser("Duplicate User");
        String duplicateRequest = objectMapper.writeValueAsString(new RegistrationPayload(
                "Duplicate User", user.email(), "AnotherPassword123!", "0812345678", "Address"));

        mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(duplicateRequest))
                .andExpect(status().isConflict());

        MvcResult invalidResult = mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","email":"not-an-email","password":"short"}
                                """))
                .andExpect(status().isBadRequest())
                .andReturn();

        assertTrue(invalidResult.getResponse().getContentAsString().contains("Validation failed"));
    }

    @Test
    void regularUserCannotAccessAdminEndpoints() throws Exception {
        UserFixture user = registerUser("Non Admin");

        mockMvc.perform(get("/api/v1/admin/users")
                        .with(httpBasic(user.email(), user.password())))
                .andExpect(status().isForbidden());
    }

    @Test
    void rejectsStateChangingApiRequestWithoutCsrfToken() throws Exception {
        UserFixture user = registerUser("Csrf Protected User");

        mockMvc.perform(put("/api/v1/users/me")
                        .with(httpBasic(user.email(), user.password()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"Should Not Update","phone":"0811111111","address":"No token"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/users/me")
                        .with(httpBasic(user.email(), user.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Csrf Protected User"));
    }

    private record RegistrationPayload(
            String name,
            String email,
            String password,
            String phone,
            String address) {
    }
}
