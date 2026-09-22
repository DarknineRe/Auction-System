package com.example.project.service;

import java.util.List;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;

public interface BiddingService {
    Bidding createBidding(Long artworkID, Long ownerID, Double startingPrice,java.time.LocalDate startDate, java.time.LocalDate endDate);

    Bidding getBiddingById(Long biddingID);

    List<Bidding> getAllBiddings();
    
    BidAction placeBid(Long biddingID, Long userID, Double amount);
}
