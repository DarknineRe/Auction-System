package com.example.project.controller.api;

import java.math.BigDecimal;
import java.util.Date;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;

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
class BiddingApiIntegrationTest extends ApiIntegrationTestSupport {

    @Test
    void sellerCreatesAuctionBiddersBidAndUsersManageComments() throws Exception {
        UserFixture seller = registerUser("Bidding Seller");
        UserFixture firstBidder = registerUser("First Bidder");
        UserFixture secondBidder = registerUser("Second Bidder");
        createSellerProfile(seller);
        long artworkId = createArtwork(seller);
        long biddingId = createBidding(seller, artworkId);

        mockMvc.perform(get("/api/v1/biddings?page=0&size=5&sort=id,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
        mockMvc.perform(get("/api/v1/biddings/{id}", biddingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mockMvc.perform(post("/api/v1/biddings/{id}/bids", biddingId)
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":1,"amount":100.00}
                                """))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/biddings/{id}/bids", biddingId)
                        .with(httpBasic(firstBidder.email(), firstBidder.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidPayload(firstBidder.id(), "100.00")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(100.00));

        mockMvc.perform(post("/api/v1/biddings/{id}/bids", biddingId)
                        .with(httpBasic(secondBidder.email(), secondBidder.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidPayload(secondBidder.id(), "102.50")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.amount").value(102.50));

        mockMvc.perform(get("/api/v1/biddings/{id}/bids/highest", biddingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(102.50));

        UserFixture commenter = firstBidder;
        MvcResult commentResult = mockMvc.perform(post("/api/v1/biddings/{id}/comments", biddingId)
                        .with(httpBasic(commenter.email(), commenter.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"' OR '1'='1 --"}
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.message").value("' OR '1'='1 --"))
                .andReturn();

        JsonNode comment = objectMapper.readTree(commentResult.getResponse().getContentAsString());
        long commentId = comment.path("id").asLong();

        mockMvc.perform(post("/api/v1/biddings/{biddingId}/comments/{commentId}/like",
                        biddingId, commentId)
                        .with(httpBasic(secondBidder.email(), secondBidder.password()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thumbsup").value(1));

        mockMvc.perform(post("/api/v1/biddings/{biddingId}/comments/{commentId}/dislike",
                        biddingId, commentId)
                        .with(httpBasic(secondBidder.email(), secondBidder.password()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.thumbsdown").value(1));

        mockMvc.perform(put("/api/v1/biddings/{biddingId}/comments/{commentId}",
                        biddingId, commentId)
                        .with(httpBasic(commenter.email(), commenter.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Updated integration test comment"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Updated integration test comment"));

        mockMvc.perform(get("/api/v1/biddings/{id}/comments", biddingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].message").value("Updated integration test comment"));

        mockMvc.perform(delete("/api/v1/biddings/{biddingId}/comments/{commentId}", biddingId, commentId)
                        .with(httpBasic(commenter.email(), commenter.password()))
                        .with(csrf()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/v1/biddings/{id}/comments", biddingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));

        mockMvc.perform(get("/api/v1/biddings/{id}/bids", biddingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2));

        mockMvc.perform(get("/api/v1/users/me/bids")
                        .with(httpBasic(firstBidder.email(), firstBidder.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].amount").value(100.00));

        mockMvc.perform(get("/api/v1/biddings/mine")
                        .with(httpBasic(seller.email(), seller.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(biddingId));

        mockMvc.perform(get("/api/v1/biddings/won")
                        .with(httpBasic(firstBidder.email(), firstBidder.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray());
    }

    @Test
    void rejectsBidBelowIncrementAndPreventsAuctionOwnerBidding() throws Exception {
        UserFixture seller = registerUser("Rule Seller");
        UserFixture bidder = registerUser("Rule Bidder");
        createSellerProfile(seller);
        long artworkId = createArtwork(seller);
        long biddingId = createBidding(seller, artworkId);

        mockMvc.perform(post("/api/v1/biddings/{id}/bids", biddingId)
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidPayload(seller.id(), "100.00")))
                .andExpect(status().isConflict());

        mockMvc.perform(post("/api/v1/biddings/{id}/bids", biddingId)
                        .with(httpBasic(bidder.email(), bidder.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidPayload(bidder.id(), "100.00")))
                .andExpect(status().isCreated());

        UserFixture laterBidder = registerUser("Below Increment Bidder");
        mockMvc.perform(post("/api/v1/biddings/{id}/bids", biddingId)
                        .with(httpBasic(laterBidder.email(), laterBidder.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bidPayload(laterBidder.id(), "102.49")))
                .andExpect(status().isConflict());

        mockMvc.perform(put("/api/v1/biddings/{id}", biddingId)
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(updateBiddingPayload()))
                .andExpect(status().isConflict());
    }

    private long createArtwork(UserFixture seller) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/v1/artworks")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Bidding Integration Artwork","imageUrl":"https://example.test/bid.jpg"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();

        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    private long createBidding(UserFixture seller, long artworkId) throws Exception {
        long startDate = System.currentTimeMillis() + 500;
        long endDate = startDate + 120_000;
        String payload = objectMapper.writeValueAsString(new CreateBiddingPayload(
                java.util.List.of(artworkId),
                seller.id(),
                new BigDecimal("100.00"),
                new BigDecimal("2.50"),
                new Date(startDate),
                new Date(endDate)));

        MvcResult result = mockMvc.perform(post("/api/v1/biddings")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(payload))
                .andExpect(status().isCreated())
                .andReturn();

        Thread.sleep(Math.max(0, startDate - System.currentTimeMillis() + 50));
        return objectMapper.readTree(result.getResponse().getContentAsString()).path("id").asLong();
    }

    private String bidPayload(long userId, String amount) throws Exception {
        return objectMapper.writeValueAsString(new PlaceBidPayload(userId, new BigDecimal(amount)));
    }

    private String updateBiddingPayload() throws Exception {
        long startDate = System.currentTimeMillis() + 10_000;
        return objectMapper.writeValueAsString(new UpdateBiddingPayload(
                new BigDecimal("120.00"),
                new BigDecimal("5.00"),
                new Date(startDate),
                new Date(startDate + 120_000)));
    }

    private record CreateBiddingPayload(
            java.util.List<Long> artworkIds,
            Long ownerId,
            BigDecimal startingPrice,
            BigDecimal minimumBidIncrement,
            Date startDate,
            Date endDate) {
    }

    private record PlaceBidPayload(Long userId, BigDecimal amount) {
    }

    private record UpdateBiddingPayload(
            BigDecimal startingPrice,
            BigDecimal minimumBidIncrement,
            Date startDate,
            Date endDate) {
    }
}
