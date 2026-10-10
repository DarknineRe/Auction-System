package com.example.project.config;

import java.util.function.Supplier;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import com.example.project.model.User;
import com.example.project.repository.UserRepository;

final class CurrentAdminAuthorizationManager implements AuthorizationManager<RequestAuthorizationContext> {

    private final UserRepository userRepository;

    CurrentAdminAuthorizationManager(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public AuthorizationDecision authorize(
            Supplier<? extends Authentication> authentication, RequestAuthorizationContext context) {
        Authentication currentAuthentication = authentication.get();
        if (currentAuthentication == null
                || !currentAuthentication.isAuthenticated()
                || currentAuthentication instanceof AnonymousAuthenticationToken) {
            return new AuthorizationDecision(false);
        }

        boolean isCurrentAdmin = userRepository.findByEmail(currentAuthentication.getName())
                .filter(User::isEnabled)
                .map(User::getRole)
                .map(role -> role == User.Role.ADMIN || role == User.Role.SUPER_ADMIN)
                .orElse(false);
        return new AuthorizationDecision(isCurrentAdmin);
    }
}
