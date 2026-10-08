package com.example.project.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.BidAction;

public interface BidActionService {
    List<BidAction> getBidsByBidding(Long biddingID);

    BidAction getHighestBid(Long biddingID);

    List<BidAction> getBidsByUser(Long userID);

    Page<BidAction> getBidsByUser(Long userID, Pageable pageable);
}