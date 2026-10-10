package com.example.project.service.state;

import org.springframework.stereotype.Component;

import com.example.project.model.Bidding;

@Component
public class ActiveBiddingState implements BiddingState {

    @Override
    public Bidding.Status getStatus() {
        return Bidding.Status.ACTIVE;
    }

    @Override
    public boolean acceptsBids() {
        return true;
    }

    @Override
    public boolean canMoveTo(Bidding.Status target) {
        return target == Bidding.Status.CLOSED || target == Bidding.Status.CANCELLED;
    }
}
