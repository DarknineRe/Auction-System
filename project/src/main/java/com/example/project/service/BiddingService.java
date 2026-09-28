package com.example.project.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.example.project.dto.response.BidActionResponse;
import com.example.project.dto.response.BiddingResponse;

//READ: ทำเป็น interface in case if adding mroe serviuce later, might undo ts
public interface BiddingService {
    BiddingResponse createBidding(List<Long> artworkIDs, Long ownerID, BigDecimal startingPrice, LocalDateTime startDate, LocalDateTime endDate);

    BiddingResponse getBiddingById(Long biddingID);

    List<BiddingResponse> getAllBiddings();

    BidActionResponse placeBid(Long biddingID, Long userID, BigDecimal amount);
}