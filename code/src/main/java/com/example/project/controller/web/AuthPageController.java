package com.example.project.controller.web;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Login and registration pages. */
@Controller
@Validated
public class AuthPageController {

    private final UserService userService;
    private final SessionAuthenticator sessionAuthenticator;

    public AuthPageController(UserService userService, SessionAuthenticator sessionAuthenticator) {
        this.userService = userService;
        this.sessionAuthenticator = sessionAuthenticator;
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam @NotBlank @Size(max = 255) String name,
            @RequestParam @NotBlank @Size(max = 320) String email,
            @RequestParam @NotBlank @Size(min = 8, max = 72) String password,
            @RequestParam @NotBlank String confirmPassword,
            @RequestParam(required = false) @Size(max = 100) String phone,
            @RequestParam(required = false) @Size(max = 1000) String address,
            HttpServletRequest request,
            HttpServletResponse response) {
        if (!password.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Passwords do not match");
        }
        userService.registerUser(name, email, password, phone, address);
        sessionAuthenticator.signIn(email, password, request, response);
        return "redirect:/";
    }
}
