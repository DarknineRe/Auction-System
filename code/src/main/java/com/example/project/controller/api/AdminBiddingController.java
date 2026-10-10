package com.example.project.controller.api;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.response.BiddingResponse;
import com.example.project.mapper.BiddingMapper;
import com.example.project.service.AdminBiddingService;

@RestController
@RequestMapping("/api/v1/admin/biddings")
@PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
public class AdminBiddingController {

    private final AdminBiddingService adminBiddingService;
    private final BiddingMapper biddingMapper;

    public AdminBiddingController(AdminBiddingService adminBiddingService, BiddingMapper biddingMapper) {
        this.adminBiddingService = adminBiddingService;
        this.biddingMapper = biddingMapper;
    }

    @PostMapping("/{biddingId}/cancel")
    public ResponseEntity<BiddingResponse> cancelBidding(@PathVariable Long biddingId) {
        return ResponseEntity.ok(biddingMapper.toResponse(adminBiddingService.cancelBidding(biddingId)));
    }

    @PostMapping("/{biddingId}/close")
    public ResponseEntity<BiddingResponse> closeBidding(@PathVariable Long biddingId) {
        return ResponseEntity.ok(biddingMapper.toResponse(adminBiddingService.closeBidding(biddingId)));
    }
}