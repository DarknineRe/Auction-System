package com.example.project.config;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
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

    private final UserRepository userRepository;

    EnabledAccountFilter(UserRepository userRepository) {
        this.userRepository = userRepository;
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
            boolean passwordChanged = user != null && session != null
                    && session.getAttribute(PASSWORD_HASH_SESSION_ATTRIBUTE) instanceof String sessionPasswordHash
                    && !user.getPassword().equals(sessionPasswordHash);
            if (user == null || !user.isEnabled() || passwordChanged) {
                SecurityContextHolder.clearContext();
                if (session != null) {
                    session.invalidate();
                }
            } else {
                if (session != null && session.getAttribute(PASSWORD_HASH_SESSION_ATTRIBUTE) == null) {
                    session.setAttribute(PASSWORD_HASH_SESSION_ATTRIBUTE, user.getPassword());
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
