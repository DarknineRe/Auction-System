package com.example.project.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SellerprofileApiIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void sellerCanReadAndUpdatePrivateProfileAndPublicViewOmitsBankAccount() throws Exception {
        UserFixture seller = registerUser("Seller Profile Flow");
        createSellerProfile(seller);

        mockMvc.perform(get("/api/v1/seller-profiles/me")
                        .with(httpBasic(seller.email(), seller.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankaccount").value("test-account-123"));

        mockMvc.perform(put("/api/v1/seller-profiles/me")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"bankaccount":"updated-account-456"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.bankaccount").value("updated-account-456"));

        mockMvc.perform(get("/api/v1/seller-profiles/users/{userId}", seller.id()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value(seller.id()))
                .andExpect(jsonPath("$.bankaccount").doesNotExist());

        mockMvc.perform(get("/api/v1/seller-profiles/me"))
                .andExpect(status().isUnauthorized());
    }
}
