package com.example.project.controller.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ArtworkApiIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void sellerCreatesReadsUpdatesAndDeletesArtwork() throws Exception {
        UserFixture seller = registerUser("Artwork Seller");
        UserFixture otherUser = registerUser("Other User");
        createSellerProfile(seller);

        MvcResult created = mockMvc.perform(post("/api/v1/artworks")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Integration Artwork","imageUrl":"https://example.test/artwork.jpg"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Integration Artwork"))
                .andReturn();

        JsonNode createdJson = objectMapper.readTree(created.getResponse().getContentAsString());
        long artworkId = createdJson.path("id").asLong();

        MvcResult listing = mockMvc.perform(get("/api/v1/artworks?page=0&size=10&sort=id,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andReturn();
        assertTrue(listing.getResponse().getContentAsString().contains("Integration Artwork"));

        mockMvc.perform(get("/api/v1/artworks/{id}", artworkId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(artworkId));

        mockMvc.perform(put("/api/v1/artworks/{id}", artworkId)
                        .with(httpBasic(otherUser.email(), otherUser.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Unauthorized Update","imageUrl":"https://example.test/no.jpg"}
                                """))
                .andExpect(status().isForbidden());

        mockMvc.perform(put("/api/v1/artworks/{id}", artworkId)
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Updated Integration Artwork","imageUrl":"https://example.test/updated.jpg"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Integration Artwork"));

        mockMvc.perform(delete("/api/v1/artworks/{id}", artworkId)
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/artworks/{id}", artworkId))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidArtworkAndUnauthenticatedCreation() throws Exception {
        UserFixture seller = registerUser("Invalid Artwork Seller");
        createSellerProfile(seller);

        mockMvc.perform(post("/api/v1/artworks")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Unauthenticated Artwork"}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/artworks")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":" "}
                                """))
                .andExpect(status().isBadRequest());
    }
}
