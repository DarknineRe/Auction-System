package com.example.project.controller.web;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.dto.request.UpdateBiddingRequest;
import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.service.AuctionListingService;
import com.example.project.service.BiddingService;

import jakarta.validation.constraints.Positive;

/** The seller's own auctions: list, create, edit and cancel. */
@Controller
@Validated
public class AuctionPageController {

    private static final String SELLER_SETUP_REDIRECT = "redirect:/seller/settings?auctionRequired";

    private final BiddingService biddingService;
    private final AuctionListingService auctionListingService;
    private final CurrentSellerProfile currentSellerProfile;
    private final WebSupport web;

    public AuctionPageController(BiddingService biddingService, AuctionListingService auctionListingService,
            CurrentSellerProfile currentSellerProfile, WebSupport web) {
        this.biddingService = biddingService;
        this.auctionListingService = auctionListingService;
        this.currentSellerProfile = currentSellerProfile;
        this.web = web;
    }

    @GetMapping("/my-auctions")
    public String myAuctions(
            Authentication authentication, @RequestParam(defaultValue = "0") int page, Model model) {
        User user = web.currentUser(authentication);
        Page<Bidding> biddings = biddingService.getBiddingsByOwner(user.getId(), null, web.newestFirst(page));
        model.addAttribute("biddings", biddings.getContent());
        model.addAttribute("tablePage", biddings);
        return web.workspace(model, "my-auctions", "My auctions");
    }

    @GetMapping("/auctions/new")
    public String createAuctionForm(Authentication authentication, Model model) {
        if (currentSellerProfile.find(authentication.getName()).isEmpty()) {
            return SELLER_SETUP_REDIRECT;
        }
        return web.workspace(model, "new-auction", "Create an auction");
    }

    @PostMapping("/auctions")
    public String createAuction(
            Authentication authentication,
            @RequestParam(required = false) List<String> title,
            @RequestParam(required = false) List<String> imageUrl,
            @RequestParam @Positive BigDecimal startingPrice,
            @RequestParam @Positive BigDecimal minimumBidIncrement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        if (currentSellerProfile.find(authentication.getName()).isEmpty()) {
            return SELLER_SETUP_REDIRECT;
        }
        List<String> titles = title == null ? List.of() : title;
        List<String> imageUrls = imageUrl == null ? List.of() : imageUrl;
        if (titles.stream().allMatch(value -> value == null || value.trim().isEmpty())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "At least one artwork title is required");
        }
        User owner = web.currentUser(authentication);
        Bidding bidding = auctionListingService.createListing(
                owner.getId(), titles, imageUrls, startingPrice, minimumBidIncrement,
                web.toDate(startDate), web.toDate(endDate));
        return "redirect:/biddings/" + bidding.getId() + "?success";
    }

    @GetMapping("/auctions/{biddingId}/edit")
    public String editAuctionForm(@PathVariable Long biddingId, Authentication authentication, Model model) {
        User owner = web.currentUser(authentication);
        Bidding bidding = biddingService.getBiddingById(biddingId);
        if (!bidding.getOwner().getId().equals(owner.getId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Only the auction owner can edit it");
        }
        model.addAttribute("bidding", bidding);
        return web.workspace(model, "edit-auction", "Edit auction");
    }

    @PostMapping("/auctions/{biddingId}/edit")
    public String editAuction(
            @PathVariable Long biddingId,
            Authentication authentication,
            @RequestParam @Positive BigDecimal startingPrice,
            @RequestParam @Positive BigDecimal minimumBidIncrement,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        User owner = web.currentUser(authentication);
        web.validate(new UpdateBiddingRequest(
                startingPrice, minimumBidIncrement, web.toDate(startDate), web.toDate(endDate)));
        biddingService.updateBidding(biddingId, owner.getId(), startingPrice, minimumBidIncrement,
                web.toDate(startDate), web.toDate(endDate));
        return "redirect:/my-auctions?success";
    }

    @PostMapping("/auctions/{biddingId}/cancel")
    public String cancelAuction(@PathVariable Long biddingId, Authentication authentication) {
        User owner = web.currentUser(authentication);
        biddingService.cancelBidding(biddingId, owner.getId());
        return "redirect:/my-auctions?success";
    }
}
