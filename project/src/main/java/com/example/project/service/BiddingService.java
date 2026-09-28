package com.example.project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;

//READ: ทำเป็น interface in case if adding mroe serviuce later, might undo ts
public interface BiddingService {
    Bidding createBidding(List<Long> artworkIDs, Long ownerID, BigDecimal startingPrice, LocalDateTime startDate, LocalDateTime endDate);

    Bidding getBiddingById(Long biddingID);

    List<Bidding> getAllBiddings();

    BidAction placeBid(Long biddingID, Long userID, BigDecimal amount);
}