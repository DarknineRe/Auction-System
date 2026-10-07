package com.example.project.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.BidActionResponse;
import com.example.project.dto.response.BiddingResponse;
import com.example.project.model.BidAction;
import com.example.project.model.Bidding;

@Component
public class BiddingMapper {

    public BiddingResponse toResponse(Bidding bidding) {
        List<Long> artworkIds = bidding.getArtworks().stream()
                .map(artwork -> artwork.getId())
                .collect(Collectors.toList());
        Long ownerId = bidding.getOwner() == null ? null : bidding.getOwner().getId();
        Long winnerId = bidding.getWinner() == null ? null : bidding.getWinner().getId();

        return new BiddingResponse(
                bidding.getId(),
                artworkIds,
                ownerId,
                bidding.getStartingPrice(),
                bidding.getLastBid(),
                bidding.getStartDate(),
                bidding.getEndDate(),
                bidding.getStatus(),
                winnerId,
                bidding.getSellerRating());
                
    }

    public BidActionResponse toResponse(BidAction bidAction) {
        Long biddingId = bidAction.getBidding() == null ? null : bidAction.getBidding().getId();
        Long userId = bidAction.getUser() == null ? null : bidAction.getUser().getId();

        return new BidActionResponse(
                bidAction.getId(),
                biddingId,
                userId,
                bidAction.getAmount(),
                bidAction.getTimestamp());
    }
}