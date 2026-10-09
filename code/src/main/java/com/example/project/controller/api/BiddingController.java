package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

import com.example.project.dto.request.CreateBiddingRequest;
import com.example.project.dto.request.PlaceBidRequest;
import com.example.project.dto.request.RateSellerRequest;
import com.example.project.dto.request.UpdateBiddingRequest;
import com.example.project.dto.response.BidActionResponse;
import com.example.project.dto.response.BiddingResponse;
import com.example.project.dto.response.PublicSellerprofileResponse;
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
                request.minimumBidIncrement(),
                request.startDate(),
                request.endDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(biddingMapper.toResponse(bidding));
    }

    @GetMapping
    public ResponseEntity<PagedModel<BiddingResponse>> getBiddings(
            @RequestParam(required = false) Bidding.Status status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<BiddingResponse> page = biddingService.getBiddings(status, pageable)
                .map(biddingMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/mine")
    public ResponseEntity<PagedModel<BiddingResponse>> getMyBiddings(
            Authentication authentication,
            @RequestParam(required = false) Bidding.Status status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        User owner = userService.getCurrentUser(authentication.getName());
        Page<BiddingResponse> page = biddingService.getBiddingsByOwner(owner.getId(), status, pageable)
                .map(biddingMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/won")
    public ResponseEntity<PagedModel<BiddingResponse>> getWonBiddings(
            Authentication authentication,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        User winner = userService.getCurrentUser(authentication.getName());
        Page<BiddingResponse> page = biddingService.getBiddingsWonBy(winner.getId(), pageable)
                .map(biddingMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{biddingId}")
    public ResponseEntity<BiddingResponse> getBiddingById(@PathVariable Long biddingId) {
        return ResponseEntity.ok(biddingMapper.toResponse(biddingService.getBiddingById(biddingId)));
    }

    @PutMapping("/{biddingId}")
    public ResponseEntity<BiddingResponse> updateBidding(
            @PathVariable Long biddingId,
            Authentication authentication,
            @Valid @RequestBody UpdateBiddingRequest request) {
        User owner = userService.getCurrentUser(authentication.getName());
        Bidding bidding = biddingService.updateBidding(
                biddingId,
                owner.getId(),
                request.startingPrice(),
                request.minimumBidIncrement(),
                request.startDate(),
                request.endDate());

        return ResponseEntity.ok(biddingMapper.toResponse(bidding));
    }

    @PostMapping("/{biddingId}/cancel")
    public ResponseEntity<BiddingResponse> cancelBidding(
            @PathVariable Long biddingId,
            Authentication authentication) {
        User owner = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(biddingMapper.toResponse(biddingService.cancelBidding(biddingId, owner.getId())));
    }

    @PostMapping("/{biddingId}/bids")
    public ResponseEntity<BidActionResponse> placeBid(
            @PathVariable Long biddingId,
            Authentication authentication,
            @Valid @RequestBody PlaceBidRequest request) {
        User bidder = userService.getCurrentUser(authentication.getName());
        BidAction bidAction = biddingService.placeBid(
                biddingId,
                authentication.getName(),
                request.amount());

        return ResponseEntity.status(HttpStatus.CREATED).body(biddingMapper.toResponse(bidAction));
    }

    @PostMapping("/{biddingId}/seller-rating")
    public ResponseEntity<PublicSellerprofileResponse> rateSeller(
            @PathVariable Long biddingId,
            Authentication authentication,
            @Valid @RequestBody RateSellerRequest request) {
        User user = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(sellerprofileMapper.toPublicResponse(
                sellerprofileService.rateSeller(biddingId, user.getId(), request.score())));
    }
}
