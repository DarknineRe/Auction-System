package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.AdminBidActionResponse;
import com.example.project.model.BidAction;

@Component
public class AdminBidActionMapper {

    public AdminBidActionResponse toResponse(BidAction bid) {
        Long biddingId = bid.getBidding() == null ? null : bid.getBidding().getId();
        Long userId = bid.getUser() == null ? null : bid.getUser().getId();
        Long voidedById = bid.getVoidedBy() == null ? null : bid.getVoidedBy().getId();

        return new AdminBidActionResponse(
                bid.getId(),
                biddingId,
                userId,
                bid.getAmount(),
                bid.getTimestamp(),
                bid.getStatus(),
                bid.getVoidedAt(),
                voidedById,
                bid.getVoidReason());
    }
}