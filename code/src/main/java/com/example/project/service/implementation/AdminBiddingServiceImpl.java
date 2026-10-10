package com.example.project.service.implementation;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Bidding;
import com.example.project.repository.BiddingRepository;
import com.example.project.service.AdminBiddingService;
import com.example.project.service.BiddingClosingService;
import com.example.project.service.state.BiddingStateResolver;

@Service
public class AdminBiddingServiceImpl implements AdminBiddingService {

    private final BiddingRepository biddingRepository;
    private final BiddingStateResolver stateResolver;
    private final BiddingClosingService biddingClosingService;

    public AdminBiddingServiceImpl(BiddingRepository biddingRepository, BiddingStateResolver stateResolver,
            BiddingClosingService biddingClosingService) {
        this.biddingRepository = biddingRepository;
        this.stateResolver = stateResolver;
        this.biddingClosingService = biddingClosingService;
    }

    @Override
    @Transactional
    public Bidding cancelBidding(Long biddingId) {
        return changeStatus(biddingId, Bidding.Status.CANCELLED);
    }

    @Override
    @Transactional
    public Bidding closeBidding(Long biddingId) {
        return biddingClosingService.close(findTransitionable(biddingId, Bidding.Status.CLOSED));
    }

    private Bidding changeStatus(Long biddingId, Bidding.Status target) {
        Bidding bidding = findTransitionable(biddingId, target);
        bidding.setStatus(target);
        return biddingRepository.save(bidding);
    }

    private Bidding findTransitionable(Long biddingId, Bidding.Status target) {
        Bidding bidding = biddingRepository.findByIdForUpdate(biddingId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Bidding not found: " + biddingId));

        if (!stateResolver.resolve(bidding.getStatus()).canMoveTo(target)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Cannot change bidding from " + bidding.getStatus() + " to " + target);
        }
        return bidding;
    }
}