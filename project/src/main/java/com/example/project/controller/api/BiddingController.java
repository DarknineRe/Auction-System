package com.example.project.controller.api;

import java.util.List;

import jakarta.validation.Valid;

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
import com.example.project.service.BiddingService;

@RestController
@RequestMapping("/api/v1/biddings")
public class BiddingController {

    private final BiddingService biddingService;

    public BiddingController(BiddingService biddingService) {
        this.biddingService = biddingService;
    }

    @PostMapping
    public ResponseEntity<BiddingResponse> create(@Valid @RequestBody CreateBiddingRequest request) {
        BiddingResponse response = biddingService.createBidding(
                request.artworkIds(),
                request.ownerId(),
                request.startingPrice(),
                request.startDate(),
                request.endDate());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public List<BiddingResponse> getAll() {
        return biddingService.getAllBiddings();
    }

    @GetMapping("/{id}")
    public BiddingResponse getOne(@PathVariable Long id) {
        return biddingService.getBiddingById(id);
    }

    @PostMapping("/{id}/bids")
    public ResponseEntity<BidActionResponse> placeBid(@PathVariable Long id, @Valid @RequestBody PlaceBidRequest request) {
        BidActionResponse response = biddingService.placeBid(id, request.userId(), request.amount());
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}