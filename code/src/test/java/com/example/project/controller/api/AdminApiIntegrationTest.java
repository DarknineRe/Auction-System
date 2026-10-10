package com.example.project.controller.api;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MvcResult;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;
import com.example.project.service.AdminUserService;
import com.fasterxml.jackson.databind.JsonNode;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminApiIntegrationTest extends ApiIntegrationTestSupport {

    @Autowired
    private AdminUserService adminUserService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void adminCanRemoveCommentFromAuctionPageAndModerationPageIsGone() throws Exception {
        UserFixture admin = createRoleAdmin(User.Role.ADMIN);
        UserFixture seller = registerUser("Comment Moderation Seller");
        UserFixture commenter = registerUser("Comment Moderation User");
        createSellerProfile(seller);
        long biddingId = createAuction(seller, 60_000);

        MvcResult commentResult = mockMvc.perform(post("/api/v1/biddings/{id}/comments", biddingId)
                        .with(httpBasic(commenter.email(), commenter.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"message":"Comment to remove"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long commentId = objectMapper.readTree(commentResult.getResponse().getContentAsString())
                .path("id").asLong();

        mockMvc.perform(get("/admin/moderation")
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/biddings/{id}", biddingId)
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Comment to remove")))
                .andExpect(content().string(containsString(">Remove</button>")));

        mockMvc.perform(get("/biddings/{id}", biddingId)
                        .with(httpBasic(seller.email(), seller.password())))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString(">Remove</button>"))));

        mockMvc.perform(post("/biddings/{biddingId}/comments/{commentId}/delete",
                        biddingId, commentId)
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/biddings/{biddingId}/comments/{commentId}/delete",
                        biddingId, commentId)
                        .with(httpBasic(admin.email(), admin.password()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/biddings/{id}/comments", biddingId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void superAdminCanListUsersAndDisableRegularUser() throws Exception {
        UserFixture admin = createAdmin();
        UserFixture user = registerUser("Admin Status Target");

        mockMvc.perform(get("/api/v1/admin/users?page=0&size=100")
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].email", hasItem(user.email())));

        mockMvc.perform(get("/admin/users")
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Promote to Admin")));

        mockMvc.perform(post("/admin/users/{userId}/promote", user.id())
                        .with(httpBasic(admin.email(), admin.password()))
                                .with(csrf()))
                        .andExpect(status().is3xxRedirection());

        mockMvc.perform(get("/api/v1/admin/users/{userId}", user.id())
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));

        mockMvc.perform(patch("/api/v1/admin/users/{userId}/status", user.id())
                        .with(httpBasic(admin.email(), admin.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"enabled":false}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.enabled").value(false));

        mockMvc.perform(get("/api/v1/users/me")
                        .with(httpBasic(user.email(), user.password())))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void regularAdminCannotPromoteAUser() throws Exception {
        UserFixture admin = createRoleAdmin(User.Role.ADMIN);
        UserFixture user = registerUser("Promotion Permission Target");

        mockMvc.perform(get("/admin/users")
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.not(containsString("Promote to Admin"))));

        mockMvc.perform(post("/admin/users/{userId}/promote", user.id())
                        .with(httpBasic(admin.email(), admin.password()))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(get("/api/v1/admin/users/{userId}", user.id())
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("USER"));
    }

    @Test
    void onlySuperAdminCanDemoteAnAdmin() throws Exception {
        UserFixture superAdmin = createAdmin();
        UserFixture regularAdmin = createRoleAdmin(User.Role.ADMIN);
        MvcResult adminLogin = mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("email", regularAdmin.email())
                        .param("password", regularAdmin.password()))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession adminSession =
                (MockHttpSession) adminLogin.getRequest().getSession(false);

        mockMvc.perform(get("/admin/users")
                        .with(httpBasic(superAdmin.email(), superAdmin.password())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Demote to User")));

        mockMvc.perform(post("/admin/users/{userId}/demote", regularAdmin.id())
                        .with(httpBasic(regularAdmin.email(), regularAdmin.password()))
                        .with(csrf()))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/admin/users/{userId}/demote", regularAdmin.id())
                        .with(httpBasic(superAdmin.email(), superAdmin.password()))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection());

        org.junit.jupiter.api.Assertions.assertEquals(User.Role.USER,
                userRepository.findById(regularAdmin.id()).orElseThrow().getRole());

        mockMvc.perform(get("/admin").session(adminSession))
                .andExpect(status().isForbidden());
    }

    @Test
    void superAdminCanCancelAuctionAndCloseWinningAuctionThenCancelPayment() throws Exception {
        UserFixture admin = createAdmin();
        UserFixture paymentAdmin = createRoleAdmin(User.Role.ADMIN);
        UserFixture seller = registerUser("Admin Flow Seller");
        UserFixture buyer = registerUser("Admin Flow Buyer");
        createSellerProfile(seller);

        long cancelledAuctionId = createAuction(seller, 150_000);
        mockMvc.perform(post("/api/v1/admin/biddings/{biddingId}/cancel", cancelledAuctionId)
                        .with(httpBasic(admin.email(), admin.password()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

        long closedAuctionId = createAuction(seller, 150_000);
        mockMvc.perform(post("/api/v1/biddings/{biddingId}/bids", closedAuctionId)
                        .with(httpBasic(buyer.email(), buyer.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"userId":%d,"amount":100.00}
                                """.formatted(buyer.id())))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/admin/biddings/{biddingId}/close", closedAuctionId)
                        .with(httpBasic(admin.email(), admin.password()))
                        .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CLOSED"));

        MvcResult purchasesResult = mockMvc.perform(get("/api/v1/payments/purchases")
                        .with(httpBasic(buyer.email(), buyer.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].status").value("AWAITING_PAYMENT"))
                .andReturn();
        JsonNode purchases = objectMapper.readTree(purchasesResult.getResponse().getContentAsString());
        long paymentId = purchases.path("content").get(0).path("id").asLong();

        mockMvc.perform(get("/api/v1/admin/payments")
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) paymentId)));

        mockMvc.perform(get("/api/v1/admin/payments")
                        .with(httpBasic(paymentAdmin.email(), paymentAdmin.password())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].id", hasItem((int) paymentId)));

        mockMvc.perform(get("/admin/payments")
                        .with(httpBasic(admin.email(), admin.password())))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Manage payments")));

        mockMvc.perform(post("/api/v1/admin/payments/{paymentId}/cancel", paymentId)
                        .with(httpBasic(admin.email(), admin.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"reason":"Integration test cancellation"}
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"))
                .andExpect(jsonPath("$.cancelReason").value("Integration test cancellation"));
    }

    private UserFixture createAdmin() {
        String email = "test-admin-" + UUID.randomUUID() + "@example.test";
        String password = "AdminTest123!";
        adminUserService.createAdminIfAbsent("Integration Test Admin", email, password);
        return new UserFixture(null, email, password);
    }

    private UserFixture createRoleAdmin(User.Role role) {
        String email = "test-admin-" + UUID.randomUUID() + "@example.test";
        String password = "AdminTest123!";
        User admin = new User();
        admin.setName("Integration Test Admin");
        admin.setEmail(email);
        admin.setPassword(passwordEncoder.encode(password));
        admin.setRole(role);
        admin.setEnabled(true);
        userRepository.save(admin);
        return new UserFixture(admin.getId(), email, password);
    }

    private long createAuction(UserFixture seller, long durationMillis) throws Exception {
        MvcResult artworkResult = mockMvc.perform(post("/api/v1/artworks")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"title":"Admin Integration Artwork","imageUrl":"https://example.test/admin.jpg"}
                                """))
                .andExpect(status().isCreated())
                .andReturn();
        long artworkId = objectMapper.readTree(artworkResult.getResponse().getContentAsString())
                .path("id").asLong();

        long startDate = System.currentTimeMillis() + 10_000;
        long endDate = startDate + durationMillis;
        String request = objectMapper.writeValueAsString(new AuctionRequest(
                List.of(artworkId),
                seller.id(),
                new BigDecimal("100.00"),
                new BigDecimal("2.50"),
                new Date(startDate),
                new Date(endDate)));

        MvcResult biddingResult = mockMvc.perform(post("/api/v1/biddings")
                        .with(httpBasic(seller.email(), seller.password()))
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(request))
                .andExpect(status().isCreated())
                .andReturn();
        Thread.sleep(Math.max(0, startDate - System.currentTimeMillis() + 50));
        return objectMapper.readTree(biddingResult.getResponse().getContentAsString())
                .path("id").asLong();
    }

    private record AuctionRequest(
            List<Long> artworkIds,
            Long ownerId,
            BigDecimal startingPrice,
            BigDecimal minimumBidIncrement,
            Date startDate,
            Date endDate) {
    }
}
