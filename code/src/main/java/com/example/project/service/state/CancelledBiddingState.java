package com.example.project.service.state;

import org.springframework.stereotype.Component;

import com.example.project.model.Bidding;

@Component
public class CancelledBiddingState implements BiddingState {

    @Override
    public Bidding.Status getStatus() {
        return Bidding.Status.CANCELLED;
    }

    @Override
    public boolean acceptsBids() {
        return false;
    }

    @Override
    public boolean canMoveTo(Bidding.Status target) {
        return false;
    }
}