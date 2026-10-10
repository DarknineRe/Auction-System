package com.example.project.service.implementation;

import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger log = LoggerFactory.getLogger(BiddingClosingServiceImpl.class);

    private final BiddingRepository biddingRepository;
    private final BidActionRepository bidActionRepository;
    private final PaymentService paymentService;
    private final BiddingExpiryProcessor expiryProcessor;

    public BiddingClosingServiceImpl(BiddingRepository biddingRepository, BidActionRepository bidActionRepository,
            PaymentService paymentService, BiddingExpiryProcessor expiryProcessor) {
        this.biddingRepository = biddingRepository;
        this.bidActionRepository = bidActionRepository;
        this.paymentService = paymentService;
        this.expiryProcessor = expiryProcessor;
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
    public int closeExpiredBiddings() {
        List<Long> expiredIds =
                biddingRepository.findIdsByStatusAndEndDateBefore(Bidding.Status.ACTIVE, new Date());
        int closed = 0;
        for (Long biddingId : expiredIds) {
            try {
                if (expiryProcessor.closeExpiredBidding(biddingId)) {
                    closed++;
                }
            } catch (RuntimeException e) {
                log.error("Failed to close expired bidding {}", biddingId, e);
            }
        }
        return closed;
    }
}
