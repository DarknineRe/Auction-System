package com.example.project.service.state;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.example.project.model.Bidding;

@Component
public class BiddingStateResolver {

    private final Map<Bidding.Status, BiddingState> states = new EnumMap<>(Bidding.Status.class);

    public BiddingStateResolver(List<BiddingState> stateList) {
        for (BiddingState state : stateList) {
            states.put(state.getStatus(), state);
        }
    }

    public BiddingState resolve(Bidding.Status status) {
        BiddingState state = states.get(status);
        if (state == null) {
            throw new IllegalStateException("No state registered for " + status);
        }
        return state;
    }
}