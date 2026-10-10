package com.example.project.controller.web;

import java.util.List;
import java.util.Optional;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.dto.request.SellerprofileRequest;
import com.example.project.model.Sellerprofile;
import com.example.project.model.User;
import com.example.project.service.ArtworkService;
import com.example.project.service.SellerprofileService;

import jakarta.validation.constraints.NotBlank;

/** Seller profile pages (own profile, settings and the public view). */
@Controller
@Validated
public class SellerPageController {

    private final SellerprofileService sellerprofileService;
    private final ArtworkService artworkService;
    private final CurrentSellerProfile currentSellerProfile;
    private final WebSupport web;

    public SellerPageController(SellerprofileService sellerprofileService, ArtworkService artworkService,
            CurrentSellerProfile currentSellerProfile, WebSupport web) {
        this.sellerprofileService = sellerprofileService;
        this.artworkService = artworkService;
        this.currentSellerProfile = currentSellerProfile;
        this.web = web;
    }

    @GetMapping("/seller/profile")
    public String mySellerProfile(Authentication authentication, Model model) {
        User user = web.currentUser(authentication);
        Optional<Sellerprofile> profile = currentSellerProfile.find(authentication.getName());
        model.addAttribute("sellerProfile", profile.orElse(null));
        model.addAttribute("seller", user);
        model.addAttribute("artworks", profile.isEmpty()
                ? List.of()
                : artworkService.getArtworksBySeller(user.getId()));
        return web.workspace(model, "my-seller-profile", "My seller profile");
    }

    @GetMapping("/seller/settings")
    public String sellerSettings(Authentication authentication, Model model) {
        model.addAttribute("sellerProfile", currentSellerProfile.find(authentication.getName()).orElse(null));
        return web.workspace(model, "seller-settings", "Seller profile");
    }

    @PostMapping("/seller/settings")
    public String saveSellerSettings(
            Authentication authentication, @RequestParam @NotBlank String bankaccount) {
        web.validate(new SellerprofileRequest(bankaccount));
        if (currentSellerProfile.find(authentication.getName()).isEmpty()) {
            sellerprofileService.createSellerProfile(authentication.getName(), bankaccount.trim());
        } else {
            sellerprofileService.updateCurrentSellerProfile(authentication.getName(), bankaccount.trim());
        }
        return "redirect:/seller/profile?success";
    }

    @GetMapping("/sellers/{userId}")
    public String seller(@PathVariable Long userId, Model model) {
        model.addAttribute("sellerProfile", sellerprofileService.getSellerProfileByUserId(userId));
        model.addAttribute("artworks", artworkService.getArtworksBySeller(userId));
        return web.workspace(model, "seller-profile", "Seller profile");
    }
}
