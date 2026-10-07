package com.example.project.service.implementation;

import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.project.model.BidAction;
import com.example.project.model.Bidding;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.service.BiddingClosingService;
import com.example.project.service.PaymentService;

@Service
public class BiddingClosingServiceImpl implements BiddingClosingService {

    private final BiddingRepository biddingRepository;
    private final BidActionRepository bidActionRepository;
    private final PaymentService paymentService;

    public BiddingClosingServiceImpl(BiddingRepository biddingRepository, BidActionRepository bidActionRepository,
            PaymentService paymentService) {
        this.biddingRepository = biddingRepository;
        this.bidActionRepository = bidActionRepository;
        this.paymentService = paymentService;
    }

    // The sale is counted when its payment completes, not here.
    @Override
    @Transactional
    public Bidding close(Bidding bidding) {
        bidActionRepository
                .findTopByBidding_IdAndStatusOrderByAmountDesc(bidding.getId(), BidAction.Status.VALID)
                .ifPresent(topBid -> {
                    bidding.setWinner(topBid.getUser());
                    bidding.setLastBid(topBid.getAmount());
                });
        bidding.setStatus(Bidding.Status.CLOSED);
        Bidding saved = biddingRepository.save(bidding);
        paymentService.createForClosedBidding(saved);
        return saved;
    }

    @Override
    @Transactional
    public int closeExpiredBiddings() {
        List<Bidding> expired = biddingRepository.findByStatusAndEndDateBefore(Bidding.Status.ACTIVE, new Date());
        expired.forEach(this::close);
        return expired.size();
    }
}
