package com.example.project.service.state;

import com.example.project.model.Bidding;

public interface BiddingState {
    Bidding.Status getStatus();

    boolean acceptsBids();

    boolean canMoveTo(Bidding.Status target);
}
