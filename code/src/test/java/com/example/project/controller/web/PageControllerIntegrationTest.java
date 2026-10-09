package com.example.project.controller.web;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
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
        var registration = mockMvc.perform(post("/register")
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

        mockMvc.perform(get("/profile").session(
                        (org.springframework.mock.web.MockHttpSession) registration.getRequest().getSession(false)))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Auto Login User")));
    }
}
