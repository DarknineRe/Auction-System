package com.example.project.controller.api;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

abstract class ApiIntegrationTestSupport {

    @Autowired
    protected MockMvc mockMvc;

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected UserFixture registerUser(String name) throws Exception {
        String email = name.toLowerCase().replaceAll("[^a-z0-9]", "") + "-"
                + UUID.randomUUID() + "@example.test";
        String password = "TestPassword123!";
        String request = objectMapper.writeValueAsString(new RegistrationRequest(
                name, email, password, "0812345678", "Test address"));

        MvcResult result = mockMvc.perform(post("/api/v1/users")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode response = objectMapper.readTree(result.getResponse().getContentAsString(StandardCharsets.UTF_8));
        return new UserFixture(response.path("id").asLong(), email, password);
    }

    protected void createSellerProfile(UserFixture user) throws Exception {
        mockMvc.perform(post("/api/v1/seller-profiles")
                        .with(httpBasic(user.email(), user.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankaccount":"test-account-123"}
                                """))
                .andExpect(status().isCreated());
    }

    protected record UserFixture(Long id, String email, String password) {
    }

    private record RegistrationRequest(
            String name,
            String email,
            String password,
            String phone,
            String address) {
    }
}
