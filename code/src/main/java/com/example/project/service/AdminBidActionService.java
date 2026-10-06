package com.example.project.service;

import java.util.List;

import com.example.project.model.BidAction;

public interface AdminBidActionService {
    List<BidAction> getAllBids(Long biddingId);

    BidAction voidBid(String actorEmail, Long bidId, String reason);
}