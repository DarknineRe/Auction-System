package com.example.project.service;

import java.util.Date;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;

//READ: ทำเป็น interface in case if adding mroe serviuce later, might undo ts
public interface BiddingService {
    Bidding createBidding(List<Long> artworkIDs, Long ownerID, Double startingPrice, Date startDate, Date endDate);

    Bidding getBiddingById(Long biddingID);

    Page<Bidding> getBiddings(Bidding.Status status, Pageable pageable);

    BidAction placeBid(Long biddingID, Long userID, Double amount);

    Bidding updateBidding(Long biddingID, Long ownerID, Double startingPrice, Date startDate, Date endDate);

    Bidding cancelBidding(Long biddingID, Long ownerID);

    Page<Bidding> getBiddingsByOwner(Long ownerID, Bidding.Status status, Pageable pageable);

    Page<Bidding> getBiddingsWonBy(Long winnerID, Pageable pageable);
}