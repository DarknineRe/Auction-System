package com.example.project.controller.web;

import java.util.Date;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.model.Bidding;
import com.example.project.service.BiddingService;

@Controller
public class HomePageController {

    private static final int HOME_PAGE_SIZE = 12;
    private static final Sort AUCTIONS_ENDING_FIRST = Sort.by(Sort.Direction.ASC, "endDate");

    private final BiddingService biddingService;

    public HomePageController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @GetMapping({ "/", "/home" })
    public String home(@RequestParam(defaultValue = "0") int page, Model model) {
        Page<Bidding> biddings = biddingService.getBiddings(
                Bidding.Status.ACTIVE, PageRequest.of(Math.max(0, page), HOME_PAGE_SIZE, AUCTIONS_ENDING_FIRST));
        model.addAttribute("biddings", biddings);
        model.addAttribute("now", new Date());
        return "home";
    }
}
