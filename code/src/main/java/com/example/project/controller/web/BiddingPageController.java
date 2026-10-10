package com.example.project.controller.web;

import java.math.BigDecimal;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.model.User;
import com.example.project.service.BiddingService;
import com.example.project.service.SellerprofileService;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

/** The bidding detail page, placing bids and rating the seller. */
@Controller
@Validated
public class BiddingPageController {

    private final BiddingService biddingService;
    private final SellerprofileService sellerprofileService;
    private final BiddingDetailModelAssembler detailAssembler;
    private final WebSupport web;

    public BiddingPageController(BiddingService biddingService, SellerprofileService sellerprofileService,
            BiddingDetailModelAssembler detailAssembler, WebSupport web) {
        this.biddingService = biddingService;
        this.sellerprofileService = sellerprofileService;
        this.detailAssembler = detailAssembler;
        this.web = web;
    }

    @GetMapping("/biddings/{biddingId}")
    public String biddingDetail(@PathVariable Long biddingId, Authentication authentication, Model model) {
        detailAssembler.populate(model, biddingId, authentication);
        return "bidding-detail";
    }

    @PostMapping("/biddings/{biddingId}/bids")
    public String placeBid(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @Positive BigDecimal amount) {
        biddingService.placeBid(biddingId, authentication.getName(), amount);
        return "redirect:/biddings/" + biddingId + "?bidPlaced";
    }

    @PostMapping("/biddings/{biddingId}/seller-rating")
    public String rateSeller(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @Min(1) @Max(5) Integer score) {
        User user = web.currentUser(authentication);
        sellerprofileService.rateSeller(biddingId, user.getId(), score);
        return "redirect:/biddings/" + biddingId + "?ratingSubmitted";
    }
}
