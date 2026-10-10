package com.example.project.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.project.service.BiddingClosingService;

@Component
public class BiddingExpiryScheduler {

    private static final Logger log = LoggerFactory.getLogger(BiddingExpiryScheduler.class);

    private final BiddingClosingService biddingClosingService;

    public BiddingExpiryScheduler(BiddingClosingService biddingClosingService) {
        this.biddingClosingService = biddingClosingService;
    }

    @Scheduled(fixedDelayString = "${app.bidding.expiry-check-ms:60000}")
    public void closeExpiredBiddings() {
        int closed = biddingClosingService.closeExpiredBiddings();
        if (closed > 0) {
            log.info("Closed {} expired bidding(s)", closed);
        }
    }
}
