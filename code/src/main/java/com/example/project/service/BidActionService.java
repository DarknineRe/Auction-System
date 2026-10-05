package com.example.project.service;

import java.util.List;

import com.example.project.model.BidAction;

public interface BidActionService {
    List<BidAction> getBidsByBidding(Long biddingID);

    BidAction getHighestBid(Long biddingID);

    List<BidAction> getBidsByUser(Long userID);
}