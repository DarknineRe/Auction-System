package com.example.project.controller.web;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Set;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.User;
import com.example.project.service.UserService;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;

/** Small helpers shared by the server-rendered page controllers. */
@Component
public class WebSupport {

    static final int PAGE_SIZE = 15;
    private static final Sort NEWEST_FIRST = Sort.by(Sort.Direction.DESC, "id");

    private final UserService userService;
    private final Validator validator;

    public WebSupport(UserService userService, Validator validator) {
        this.userService = userService;
        this.validator = validator;
    }

    /** Page {@code page} (clamped at 0) of the standard list size, newest id first. */
    public Pageable newestFirst(int page) {
        return PageRequest.of(Math.max(0, page), PAGE_SIZE, NEWEST_FIRST);
    }

    public User currentUser(Authentication authentication) {
        return userService.getCurrentUser(authentication.getName());
    }

    public boolean isAuthenticated(Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

    public boolean hasAnyRole(Authentication authentication, String... roles) {
        Set<String> wanted = Set.of(roles);
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> wanted.contains(authority.getAuthority()));
    }

    public Date toDate(LocalDateTime value) {
        return Date.from(value.atZone(ZoneId.systemDefault()).toInstant());
    }

    /** Applies the REST DTO's bean-validation rules to a web form so both entry points agree. */
    public <T> void validate(T form) {
        var violations = validator.validate(form);
        if (!violations.isEmpty()) {
            String message = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .distinct()
                    .reduce((left, right) -> left + ", " + right)
                    .orElse("Invalid request");
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, message);
        }
    }

    /** Renders the shared workspace layout with the given inner page and title. */
    public String workspace(Model model, String page, String title) {
        model.addAttribute("page", page);
        model.addAttribute("pageTitle", title);
        return "workspace";
    }
}
