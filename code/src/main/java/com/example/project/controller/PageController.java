package com.example.project.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/register")
    public String register() {
        return "register";
    }

    @GetMapping("/biddings/{id}")
    public String biddingDetail() {
        return "bidding-detail";
    }

    @GetMapping("/profile")
    public String profile() {
        return "profile";
    }
}