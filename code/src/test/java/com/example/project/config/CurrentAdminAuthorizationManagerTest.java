package com.example.project.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;

class CurrentAdminAuthorizationManagerTest {

    private final UserRepository userRepository = mock(UserRepository.class);
    private final CurrentAdminAuthorizationManager authorizationManager =
            new CurrentAdminAuthorizationManager(userRepository);
    private final RequestAuthorizationContext requestContext =
            new RequestAuthorizationContext(new MockHttpServletRequest());

    @Test
    void allowsAnEnabledCurrentAdmin() {
        User user = user(User.Role.ADMIN, true);
        when(userRepository.findByEmail("admin@example.test")).thenReturn(Optional.of(user));

        AuthorizationDecision decision = authorizationManager.authorize(
                () -> authentication("admin@example.test", "ROLE_ADMIN"), requestContext);

        assertTrue(decision.isGranted());
    }

    @Test
    void deniesAnAdminSessionAfterThePersistedRoleIsDemoted() {
        User user = user(User.Role.USER, true);
        when(userRepository.findByEmail("admin@example.test")).thenReturn(Optional.of(user));

        AuthorizationDecision decision = authorizationManager.authorize(
                () -> authentication("admin@example.test", "ROLE_ADMIN"), requestContext);

        assertFalse(decision.isGranted());
    }

    @Test
    void deniesDisabledAdministrators() {
        User user = user(User.Role.SUPER_ADMIN, false);
        when(userRepository.findByEmail("admin@example.test")).thenReturn(Optional.of(user));

        AuthorizationDecision decision = authorizationManager.authorize(
                () -> authentication("admin@example.test", "ROLE_SUPER_ADMIN"), requestContext);

        assertFalse(decision.isGranted());
    }

    private User user(User.Role role, boolean enabled) {
        User user = new User();
        user.setEmail("admin@example.test");
        user.setRole(role);
        user.setEnabled(enabled);
        return user;
    }

    private UsernamePasswordAuthenticationToken authentication(String email, String role) {
        return UsernamePasswordAuthenticationToken.authenticated(
                email, null, List.of(new SimpleGrantedAuthority(role)));
    }
}
