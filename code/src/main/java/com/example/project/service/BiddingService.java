package com.example.project.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;

//READ: ทำเป็น interface in case if adding mroe serviuce later, might undo ts
public interface BiddingService {
    Bidding createBidding(List<Long> artworkIDs, Long ownerID, BigDecimal startingPrice, Date startDate, Date endDate);

    Bidding getBiddingById(Long biddingID);

    List<Bidding> getAllBiddings();
    
    Page<Bidding> getAllBiddings(Pageable pageable);

    BidAction placeBid(Long biddingID, String actorEmail, BigDecimal amount);
}