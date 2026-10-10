package com.example.project.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.example.project.model.Bidding;

public interface AuctionListingService {
    default Bidding createListing(Long sellerUserId, String title, String imageUrl,
            BigDecimal startingPrice, BigDecimal minimumBidIncrement, Date startDate, Date endDate) {
        return createListing(sellerUserId, title == null ? List.of() : List.of(title),
                imageUrl == null ? List.of() : List.of(imageUrl),
                startingPrice, minimumBidIncrement, startDate, endDate);
    }

    Bidding createListing(Long sellerUserId, List<String> titles, List<String> imageUrls,
            BigDecimal startingPrice, BigDecimal minimumBidIncrement, Date startDate, Date endDate);
}
