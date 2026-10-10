package com.example.project.config;

import java.io.IOException;
import java.time.Instant;
import java.util.List;
import java.util.Objects;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.session.SessionRegistryImpl;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

final class EnabledAccountFilter extends OncePerRequestFilter {

    private static final String PASSWORD_HASH_SESSION_ATTRIBUTE =
            EnabledAccountFilter.class.getName() + ".passwordHash";
    private static final String PASSWORD_CHANGED_AT_SESSION_ATTRIBUTE =
            EnabledAccountFilter.class.getName() + ".passwordChangedAt";

    private final UserRepository userRepository;
    private final SessionRegistry sessionRegistry;

    EnabledAccountFilter(UserRepository userRepository) {
        this(userRepository, new SessionRegistryImpl());
    }

    EnabledAccountFilter(UserRepository userRepository, SessionRegistry sessionRegistry) {
        this.userRepository = userRepository;
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken)) {
            User user = userRepository.findByEmail(authentication.getName()).orElse(null);
            HttpSession session = request.getSession(false);
            boolean passwordChanged = false;
            if (user != null && session != null) {
                Long storedPasswordChangedAt = session.getAttribute(PASSWORD_CHANGED_AT_SESSION_ATTRIBUTE) instanceof Long value
                        ? value
                        : null;
                Long currentPasswordChangedAt = user.getPasswordChangedAt() == null
                        ? null
                        : user.getPasswordChangedAt().toEpochMilli();
                if (storedPasswordChangedAt != null && !Objects.equals(storedPasswordChangedAt, currentPasswordChangedAt)) {
                    passwordChanged = true;
                }
                if (session.getAttribute(PASSWORD_HASH_SESSION_ATTRIBUTE) instanceof String sessionPasswordHash
                        && !user.getPassword().equals(sessionPasswordHash)) {
                    passwordChanged = true;
                }
            }
            if (user == null || !user.isEnabled() || passwordChanged) {
                SecurityContextHolder.clearContext();
                if (session != null) {
                    session.invalidate();
                }
                var principal = authentication.getName();
                for (SessionInformation info : sessionRegistry.getAllSessions(principal, false)) {
                    info.expireNow();
                }
            } else {
                if (session != null) {
                    if (session.getAttribute(PASSWORD_HASH_SESSION_ATTRIBUTE) == null) {
                        session.setAttribute(PASSWORD_HASH_SESSION_ATTRIBUTE, user.getPassword());
                    }
                    if (session.getAttribute(PASSWORD_CHANGED_AT_SESSION_ATTRIBUTE) == null
                            || user.getPasswordChangedAt() != null
                            && !Objects.equals(
                                    session.getAttribute(PASSWORD_CHANGED_AT_SESSION_ATTRIBUTE),
                                    user.getPasswordChangedAt().toEpochMilli())) {
                        session.setAttribute(PASSWORD_CHANGED_AT_SESSION_ATTRIBUTE,
                                user.getPasswordChangedAt() == null ? null : user.getPasswordChangedAt().toEpochMilli());
                    }
                }
                String role = user.getRole() == null ? User.Role.USER.name() : user.getRole().name();
                var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));
                if (!authentication.getAuthorities().equals(authorities)) {
                    SecurityContext context = SecurityContextHolder.createEmptyContext();
                    UsernamePasswordAuthenticationToken refreshed =
                            new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), null, authorities);
                    refreshed.setDetails(authentication.getDetails());
                    context.setAuthentication(refreshed);
                    SecurityContextHolder.setContext(context);
                }
            }
        }
        filterChain.doFilter(request, response);
    }
}
