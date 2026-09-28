package com.example.project.mapper;

import java.util.List;

import org.springframework.stereotype.Component;

import com.example.project.domain.entity.Artwork;
import com.example.project.domain.entity.Bidding;
import com.example.project.dto.response.BiddingResponse;

@Component
public class BiddingMapper {

    public BiddingResponse toResponse(Bidding bidding) {
        List<Long> artworkIds = bidding.getArtworks().stream()
                .map(Artwork::getId)
                .toList();
        return new BiddingResponse(
                bidding.getId(),
                bidding.getOwner().getId(),
                bidding.getStartingPrice(),
                bidding.getLastBid(),
                bidding.getStartDate(),
                bidding.getEndDate(),
                artworkIds);
    }
}