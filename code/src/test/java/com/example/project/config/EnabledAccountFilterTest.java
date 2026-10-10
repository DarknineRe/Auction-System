package com.example.project.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;

class EnabledAccountFilterTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void invalidatesExistingSessionAfterPasswordChanges() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setEmail("user@example.test");
        user.setPassword("old-password-hash");
        user.setRole(User.Role.USER);
        user.setEnabled(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        EnabledAccountFilter filter = new EnabledAccountFilter(userRepository);
        MockHttpSession session = new MockHttpSession();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        user.getEmail(), null, java.util.List.of()));

        MockHttpServletRequest firstRequest = new MockHttpServletRequest();
        firstRequest.setSession(session);
        filter.doFilter(firstRequest, new MockHttpServletResponse(), (request, response) -> {
        });
        user.setPassword("new-password-hash");

        MockHttpServletRequest nextRequest = new MockHttpServletRequest();
        nextRequest.setSession(session);
        filter.doFilter(nextRequest, new MockHttpServletResponse(), (request, response) -> {
        });

        assertTrue(session.isInvalid());
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    @Test
    void refreshesAuthoritiesWhenAnAdminRoleIsRemoved() throws Exception {
        UserRepository userRepository = mock(UserRepository.class);
        User user = new User();
        user.setEmail("admin@example.test");
        user.setPassword("password-hash");
        user.setRole(User.Role.ADMIN);
        user.setEnabled(true);
        when(userRepository.findByEmail(user.getEmail())).thenReturn(Optional.of(user));
        EnabledAccountFilter filter = new EnabledAccountFilter(userRepository);
        MockHttpSession session = new MockHttpSession();
        SecurityContextHolder.getContext().setAuthentication(
                UsernamePasswordAuthenticationToken.authenticated(
                        user.getEmail(),
                        null,
                        java.util.List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))));

        MockHttpServletRequest firstRequest = new MockHttpServletRequest();
        firstRequest.setSession(session);
        filter.doFilter(firstRequest, new MockHttpServletResponse(), (request, response) -> {
        });

        user.setRole(User.Role.USER);
        MockHttpServletRequest nextRequest = new MockHttpServletRequest();
        nextRequest.setSession(session);
        filter.doFilter(nextRequest, new MockHttpServletResponse(), (request, response) -> {
        });

        assertEquals(
                java.util.List.of(new SimpleGrantedAuthority("ROLE_USER")),
                SecurityContextHolder.getContext().getAuthentication().getAuthorities());
    }
}
