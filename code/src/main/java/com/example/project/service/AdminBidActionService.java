package com.example.project.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.BidAction;

public interface AdminBidActionService {
    List<BidAction> getAllBids(Long biddingId);

    Page<BidAction> getAllBids(Long biddingId, Pageable pageable);

    BidAction voidBid(String actorEmail, Long bidId, String reason);
}