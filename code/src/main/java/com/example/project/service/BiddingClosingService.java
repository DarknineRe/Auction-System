package com.example.project.service;

import com.example.project.model.Bidding;

public interface BiddingClosingService {
    Bidding close(Bidding bidding);

    int closeExpiredBiddings();
}
