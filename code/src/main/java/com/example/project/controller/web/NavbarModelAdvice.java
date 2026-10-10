package com.example.project.controller.web;

import java.util.List;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import com.example.project.dto.response.NotificationResponse;
import com.example.project.service.NotificationService;
import com.example.project.service.UserService;

import jakarta.servlet.http.HttpServletRequest;

// Supplies what the shared navbar fragment needs on every server-rendered page.
@ControllerAdvice(basePackages = "com.example.project.controller.web")
public class NavbarModelAdvice {

    private final UserService userService;
    private final NotificationService notificationService;

    public NavbarModelAdvice(UserService userService, NotificationService notificationService) {
        this.userService = userService;
        this.notificationService = notificationService;
    }

    @ModelAttribute("currentPath")
    public String currentPath(HttpServletRequest request) {
        return request.getRequestURI();
    }

    @ModelAttribute("notifications")
    public List<NotificationResponse> notifications(Authentication authentication, HttpServletRequest request) {
        // Form posts redirect straight away, so only page views pay for the lookup.
        if (!"GET".equals(request.getMethod())
                || authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            return List.of();
        }
        return notificationService.getNotifications(
                userService.getCurrentUser(authentication.getName()).getId());
    }
}
