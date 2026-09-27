package com.example.project.controller.api;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.CreateBiddingRequest;
import com.example.project.dto.request.PlaceBidRequest;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.service.BiddingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/biddings")
public class BiddingController {

    private final BiddingService biddingService;

    public BiddingController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @PostMapping
    public ResponseEntity<Bidding> createBidding(@Valid @RequestBody CreateBiddingRequest request) {
        Bidding bidding = biddingService.createBidding(
                request.artworkIds(),
                request.ownerId(),
                request.startingPrice(),
                request.startDate(),
                request.endDate());

        return ResponseEntity.status(HttpStatus.CREATED).body(bidding);
    }

    @GetMapping
    public ResponseEntity<List<Bidding>> getAllBiddings() {
        return ResponseEntity.ok(biddingService.getAllBiddings());
    }

    @GetMapping("/{biddingId}")
    public ResponseEntity<Bidding> getBiddingById(@PathVariable Long biddingId) {
        return ResponseEntity.ok(biddingService.getBiddingById(biddingId));
    }

    @PostMapping("/{biddingId}/bids")
    public ResponseEntity<BidAction> placeBid(
            @PathVariable Long biddingId,
            @Valid @RequestBody PlaceBidRequest request) {
        BidAction bidAction = biddingService.placeBid(
                biddingId,
                request.userId(),
                request.amount());

        return ResponseEntity.status(HttpStatus.CREATED).body(bidAction);
    }
}
