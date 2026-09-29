package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

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
import com.example.project.dto.response.BidActionResponse;
import com.example.project.dto.response.BiddingResponse;
import com.example.project.mapper.BiddingMapper;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.service.BiddingService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/biddings")
public class BiddingController {

    private final BiddingService biddingService;
    private final BiddingMapper biddingMapper;

    public BiddingController(BiddingService biddingService, BiddingMapper biddingMapper) {
        this.biddingService = biddingService;
        this.biddingMapper = biddingMapper;
    }

    @PostMapping
    public ResponseEntity<BiddingResponse> createBidding(@Valid @RequestBody CreateBiddingRequest request) {
        Bidding bidding = biddingService.createBidding(
                request.artworkIds(),
                request.ownerId(),
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
            @Valid @RequestBody PlaceBidRequest request) {
        BidAction bidAction = biddingService.placeBid(
                biddingId,
                request.userId(),
                request.amount());

        return ResponseEntity.status(HttpStatus.CREATED).body(biddingMapper.toResponse(bidAction));
    }
}
