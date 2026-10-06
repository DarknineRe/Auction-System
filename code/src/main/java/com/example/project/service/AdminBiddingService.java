package com.example.project.service;

import com.example.project.model.Bidding;

public interface AdminBiddingService {
    Bidding cancelBidding(Long biddingId);

    Bidding closeBidding(Long biddingId);
}
