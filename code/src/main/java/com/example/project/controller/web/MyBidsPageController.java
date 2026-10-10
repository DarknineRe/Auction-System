package com.example.project.controller.web;

import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.Payment;
import com.example.project.model.User;
import com.example.project.service.BidActionService;
import com.example.project.service.BiddingService;
import com.example.project.service.PaymentService;

/** The bidder's own bids and the auctions they won. */
@Controller
public class MyBidsPageController {

    private final BidActionService bidActionService;
    private final BiddingService biddingService;
    private final PaymentService paymentService;
    private final WebSupport web;

    public MyBidsPageController(BidActionService bidActionService, BiddingService biddingService,
            PaymentService paymentService, WebSupport web) {
        this.bidActionService = bidActionService;
        this.biddingService = biddingService;
        this.paymentService = paymentService;
        this.web = web;
    }

    @GetMapping("/my-bids")
    public String myBids(
            Authentication authentication,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "0") int wonPage,
            Model model) {
        User user = web.currentUser(authentication);
        Page<BidAction> bids = bidActionService.getBidsByUser(user.getId(), web.newestFirst(page));
        Page<Bidding> wonBiddings = biddingService.getBiddingsWonBy(user.getId(), web.newestFirst(wonPage));
        model.addAttribute("bids", bids.getContent());
        model.addAttribute("bidsPage", bids);
        model.addAttribute("wonBiddings", wonBiddings.getContent());
        model.addAttribute("wonBiddingsPage", wonBiddings);
        Map<Long, Payment> wonPayments = paymentService
                .getPurchasesForBiddings(user.getId(), wonBiddings.map(Bidding::getId).getContent())
                .stream()
                .collect(Collectors.toMap(payment -> payment.getBidding().getId(), Function.identity()));
        model.addAttribute("wonPayments", wonPayments);
        return web.workspace(model, "my-bids", "My bids");
    }
}
