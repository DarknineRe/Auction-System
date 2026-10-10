package com.example.project.service.implementation;

import java.util.Date;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.service.PaymentService;

@Service
public class BiddingExpiryProcessor {

    private final BiddingRepository biddingRepository;
    private final BidActionRepository bidActionRepository;
    private final PaymentService paymentService;

    public BiddingExpiryProcessor(
            BiddingRepository biddingRepository,
            BidActionRepository bidActionRepository,
            PaymentService paymentService) {
        this.biddingRepository = biddingRepository;
        this.bidActionRepository = bidActionRepository;
        this.paymentService = paymentService;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean closeExpiredBidding(Long biddingId) {
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingId).orElse(null);
        if (bidding == null || bidding.getStatus() != Bidding.Status.ACTIVE
                || bidding.getEndDate() == null || !bidding.getEndDate().before(new Date())) {
            return false;
        }
        bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(biddingId, BidAction.Status.VALID)
                .ifPresent(topBid -> {
                    bidding.setWinner(topBid.getUser());
                    bidding.setLastBid(topBid.getAmount());
                });
        bidding.setStatus(Bidding.Status.CLOSED);
        Bidding saved = biddingRepository.save(bidding);
        paymentService.createForClosedBidding(saved);
        return true;
    }
}
