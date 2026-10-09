package com.example.project.service;

import java.math.BigDecimal;
import java.util.Date;

import com.example.project.model.Bidding;

public interface AuctionListingService {
    Bidding createListing(Long sellerUserId, String title, String imageUrl,
            BigDecimal startingPrice, BigDecimal minimumBidIncrement, Date startDate, Date endDate);
}
