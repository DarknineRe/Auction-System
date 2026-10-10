package com.example.project.controller.web;

import java.util.Date;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import org.springframework.ui.Model;

import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.service.BidActionService;
import com.example.project.service.BiddingService;
import com.example.project.service.CommentService;
import com.example.project.service.PaymentService;
import com.example.project.service.UserService;

/** Gathers everything the bidding-detail page shows, so the controller only routes. */
@Component
public class BiddingDetailModelAssembler {

    private final BiddingService biddingService;
    private final BidActionService bidActionService;
    private final CommentService commentService;
    private final PaymentService paymentService;
    private final UserService userService;
    private final WebSupport web;

    public BiddingDetailModelAssembler(BiddingService biddingService, BidActionService bidActionService,
            CommentService commentService, PaymentService paymentService, UserService userService,
            WebSupport web) {
        this.biddingService = biddingService;
        this.bidActionService = bidActionService;
        this.commentService = commentService;
        this.paymentService = paymentService;
        this.userService = userService;
        this.web = web;
    }

    public void populate(Model model, Long biddingId, Authentication authentication) {
        Bidding bidding = biddingService.getBiddingById(biddingId);
        model.addAttribute("bidding", bidding);
        model.addAttribute("now", new Date());
        model.addAttribute("artworks", bidding.getArtworks());
        model.addAttribute("bids", bidActionService.getBidsByBidding(biddingId));
        model.addAttribute("comments", commentService.getCommentsByBiddingId(biddingId));
        User currentUser = web.isAuthenticated(authentication)
                ? userService.getCurrentUser(authentication.getName())
                : null;
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("payment", currentUser == null
                ? null
                : paymentService.findPaymentForParticipant(biddingId, currentUser.getId()).orElse(null));
    }
}
