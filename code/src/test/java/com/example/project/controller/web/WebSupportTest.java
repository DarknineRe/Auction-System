package com.example.project.controller.web;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.dto.request.CancelPaymentRequest;
import com.example.project.model.User;
import com.example.project.service.UserService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

class WebSupportTest {

    private final Validator validator = mock(Validator.class);
    private final WebSupport web = new WebSupport(mock(UserService.class), validator);

    @Test
    void newestFirstClampsNegativePagesAndSortsByIdDescending() {
        Pageable pageable = web.newestFirst(-3);

        assertEquals(0, pageable.getPageNumber());
        assertEquals(WebSupport.PAGE_SIZE, pageable.getPageSize());
        assertTrue(pageable.getSort().getOrderFor("id").isDescending());
    }

    @Test
    void hasAnyRoleMatchesOnlyTheGivenAuthorities() {
        var admin = UsernamePasswordAuthenticationToken.authenticated(
                "a@example.test", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));

        assertTrue(web.hasAnyRole(admin, "ROLE_ADMIN", "ROLE_SUPER_ADMIN"));
        assertFalse(web.hasAnyRole(admin, "ROLE_SUPER_ADMIN"));
    }

    @Test
    void currentRoleChecksUseThePersistedUserRole() {
        UserService userService = mock(UserService.class);
        WebSupport support = new WebSupport(userService, validator);
        var admin = UsernamePasswordAuthenticationToken.authenticated(
                "a@example.test", null, List.of(new SimpleGrantedAuthority("ROLE_ADMIN")));
        var persistedUser = new User();
        persistedUser.setRole(User.Role.USER);
        when(userService.getCurrentUser("a@example.test")).thenReturn(persistedUser);

        assertFalse(support.hasAnyCurrentRole(admin, "ADMIN", "SUPER_ADMIN"));
        assertFalse(support.hasCurrentRole(admin, "SUPER_ADMIN"));
    }

    @Test
    void validateTurnsViolationsIntoBadRequest() {
        ConstraintViolation<CancelPaymentRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Reason is required");
        CancelPaymentRequest form = new CancelPaymentRequest("");
        when(validator.validate(form)).thenReturn(Set.of(violation));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> web.validate(form));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        assertEquals("Reason is required", exception.getReason());
    }
}
