package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.response.BidActionResponse;
import com.example.project.mapper.BiddingMapper;
import com.example.project.model.User;
import com.example.project.service.BidActionService;
import com.example.project.service.UserService;

@RestController
@RequestMapping("/api/v1")
public class BidActionController {

    private final BidActionService bidActionService;
    private final UserService userService;
    private final BiddingMapper biddingMapper;

    public BidActionController(BidActionService bidActionService,
            UserService userService,
            BiddingMapper biddingMapper) {
        this.bidActionService = bidActionService;
        this.userService = userService;
        this.biddingMapper = biddingMapper;
    }

    @GetMapping("/biddings/{biddingId}/bids")
    public ResponseEntity<List<BidActionResponse>> getBidsByBidding(@PathVariable Long biddingId) {
        return ResponseEntity.ok(bidActionService.getBidsByBidding(biddingId).stream()
                .map(biddingMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/biddings/{biddingId}/bids/highest")
    public ResponseEntity<BidActionResponse> getHighestBid(@PathVariable Long biddingId) {
        return ResponseEntity.ok(biddingMapper.toResponse(bidActionService.getHighestBid(biddingId)));
    }

    @GetMapping("/users/me/bids")
    public ResponseEntity<List<BidActionResponse>> getMyBids(Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());

        return ResponseEntity.ok(bidActionService.getBidsByUser(user.getId()).stream()
                .map(biddingMapper::toResponse)
                .collect(Collectors.toList()));
    }
}