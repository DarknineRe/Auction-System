package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.CreateBiddingRequest;
import com.example.project.dto.request.PlaceBidRequest;
import com.example.project.dto.request.RateSellerRequest;
import com.example.project.dto.response.BidActionResponse;
import com.example.project.dto.response.BiddingResponse;
import com.example.project.dto.response.SellerprofileResponse;
import com.example.project.mapper.BiddingMapper;
import com.example.project.mapper.SellerprofileMapper;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.model.User;
import com.example.project.service.BiddingService;
import com.example.project.service.SellerprofileService;
import com.example.project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/biddings")
public class BiddingController {

    private final BiddingService biddingService;
    private final UserService userService;
    private final BiddingMapper biddingMapper;
    private final SellerprofileService sellerprofileService;
    private final SellerprofileMapper sellerprofileMapper;

    public BiddingController(BiddingService biddingService, UserService userService, BiddingMapper biddingMapper,
            SellerprofileService sellerprofileService, SellerprofileMapper sellerprofileMapper) {
        this.biddingService = biddingService;
        this.userService = userService;
        this.biddingMapper = biddingMapper;
        this.sellerprofileService = sellerprofileService;
        this.sellerprofileMapper = sellerprofileMapper;
    }

    @PostMapping
    public ResponseEntity<BiddingResponse> createBidding(
            Authentication authentication,
            @Valid @RequestBody CreateBiddingRequest request) {
        User owner = userService.getCurrentUser(authentication.getName());
        Bidding bidding = biddingService.createBidding(
                request.artworkIds(),
                owner.getId(),
                request.startingPrice(),
                request.startDate(),
                request.endDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(biddingMapper.toResponse(bidding));
    }

    @GetMapping
    public ResponseEntity<List<BiddingResponse>> getAllBiddings() {
        return ResponseEntity.ok(biddingService.getAllBiddings().stream()
                .map(biddingMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{biddingId}")
    public ResponseEntity<BiddingResponse> getBiddingById(@PathVariable Long biddingId) {
        return ResponseEntity.ok(biddingMapper.toResponse(biddingService.getBiddingById(biddingId)));
    }

    @PostMapping("/{biddingId}/bids")
    public ResponseEntity<BidActionResponse> placeBid(
            @PathVariable Long biddingId,
            Authentication authentication,
            @Valid @RequestBody PlaceBidRequest request) {
        User bidder = userService.getCurrentUser(authentication.getName());
        BidAction bidAction = biddingService.placeBid(
                biddingId,
                bidder.getId(),
                request.amount());

        return ResponseEntity.status(HttpStatus.CREATED).body(biddingMapper.toResponse(bidAction));
    }

    @PostMapping("/{biddingId}/seller-rating")
    public ResponseEntity<SellerprofileResponse> rateSeller(
            @PathVariable Long biddingId,
            Authentication authentication,
            @Valid @RequestBody RateSellerRequest request) {
        User user = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(sellerprofileMapper.toResponse(
                sellerprofileService.rateSeller(biddingId, user.getId(), request.score())));
    }
}
