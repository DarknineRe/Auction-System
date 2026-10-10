package com.example.project.controller.web;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.service.UserService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** The signed-in user's own profile and password. */
@Controller
@Validated
public class AccountPageController {

    private final UserService userService;
    private final WebSupport web;

    public AccountPageController(UserService userService, WebSupport web) {
        this.userService = userService;
        this.web = web;
    }

    @GetMapping("/profile")
    public String profile(Authentication authentication, Model model) {
        model.addAttribute("user", web.currentUser(authentication));
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 255) String name,
            @RequestParam(required = false) @Size(max = 100) String phone,
            @RequestParam(required = false) @Size(max = 1000) String address) {
        userService.updateCurrentUser(authentication.getName(), name, phone, address);
        return "redirect:/profile?profileUpdated";
    }

    @PostMapping("/profile/password")
    public String changePassword(
            Authentication authentication,
            @RequestParam @NotBlank String currentPassword,
            @RequestParam @NotBlank @Size(min = 8, max = 72) String newPassword,
            @RequestParam @NotBlank String confirmPassword) {
        if (!newPassword.equals(confirmPassword)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new passwords do not match");
        }
        userService.changePassword(authentication.getName(), currentPassword, newPassword);
        return "redirect:/profile?passwordUpdated";
    }
}
