package com.example.project.controller.api;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.VoidBidRequest;
import com.example.project.dto.response.AdminBidActionResponse;
import com.example.project.mapper.AdminBidActionMapper;
import com.example.project.service.AdminBidActionService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminBidActionController {

    private final AdminBidActionService adminBidActionService;
    private final AdminBidActionMapper adminBidActionMapper;

    public AdminBidActionController(AdminBidActionService adminBidActionService,
            AdminBidActionMapper adminBidActionMapper) {
        this.adminBidActionService = adminBidActionService;
        this.adminBidActionMapper = adminBidActionMapper;
    }

    @GetMapping("/biddings/{biddingId}/bids")
    public ResponseEntity<List<AdminBidActionResponse>> getAllBids(@PathVariable Long biddingId) {
        return ResponseEntity.ok(adminBidActionService.getAllBids(biddingId).stream()
                .map(adminBidActionMapper::toResponse)
                .collect(Collectors.toList()));
    }

    @PostMapping("/bids/{bidId}/void")
    public ResponseEntity<AdminBidActionResponse> voidBid(
            Authentication authentication,
            @PathVariable Long bidId,
            @Valid @RequestBody VoidBidRequest request) {
        return ResponseEntity.ok(adminBidActionMapper.toResponse(
                adminBidActionService.voidBid(authentication.getName(), bidId, request.reason())));
    }
}