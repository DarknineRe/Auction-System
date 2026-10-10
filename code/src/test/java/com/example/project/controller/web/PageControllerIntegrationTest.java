package com.example.project.controller.web;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import com.example.project.repository.UserRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class PageControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Test
    void rendersPublicHomeLoginAndRegistrationPages() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("Live auctions")));

        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));

        mockMvc.perform(get("/register"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"));
    }

    @Test
    void registeringThroughWebFormAutomaticallyLogsUserIn() throws Exception {
        MockHttpSession session = new MockHttpSession();
        String originalSessionId = session.getId();
        var registration = mockMvc.perform(post("/register")
                        .session(session)
                        .with(csrf())
                        .param("name", "Auto Login User")
                        .param("email", "auto-login-user@example.test")
                        .param("password", "AutoLogin123!")
                        .param("confirmPassword", "AutoLogin123!")
                        .param("phone", "0812345678")
                        .param("address", "Test address"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/"))
                .andReturn();

        assertNotEquals(originalSessionId, session.getId());
        mockMvc.perform(get("/profile").session(
                        (org.springframework.mock.web.MockHttpSession) registration.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Auto Login User")));
    }

    @Test
    void disablingAnAccountInvalidatesItsExistingSession() throws Exception {
        String email = "disabled-session-" + UUID.randomUUID() + "@example.test";
        var registration = mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Disabled Session User")
                        .param("email", email)
                        .param("password", "DisabledSession123!")
                        .param("confirmPassword", "DisabledSession123!")
                        .param("phone", "0812345678")
                        .param("address", "Test address"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);

        var user = userRepository.findByEmail(email).orElseThrow();
        user.setEnabled(false);
        userRepository.save(user);

        mockMvc.perform(get("/profile").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    void auctionCreationRequiresSellerProfileAndExplainsHowToCreateOne() throws Exception {
        var registration = mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("name", "Auction Seller Setup")
                        .param("email", "auction-seller-setup@example.test")
                        .param("password", "AuctionSeller123!")
                        .param("confirmPassword", "AuctionSeller123!")
                        .param("phone", "0812345678")
                        .param("address", "Test address"))
                .andExpect(status().is3xxRedirection())
                .andReturn();
        MockHttpSession session = (MockHttpSession) registration.getRequest().getSession(false);

        mockMvc.perform(get("/auctions/new").session(session))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/settings?auctionRequired"));

        mockMvc.perform(get("/seller/settings").param("auctionRequired", "").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString(
                        "A seller profile is required before you can create an auction.")))
                .andExpect(content().string(containsString("Create seller profile")));
    }
}
