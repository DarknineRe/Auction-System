package com.example.project.service.implementation;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.BidAction;
import com.example.project.repository.BidActionRepository;
import com.example.project.repository.BiddingRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.BidActionService;

@Service
public class BidActionServiceImpl implements BidActionService {

    private final BidActionRepository bidActionRepository;
    private final BiddingRepository biddingRepository;
    private final UserRepository userRepository;

    public BidActionServiceImpl(BidActionRepository bidActionRepository,
            BiddingRepository biddingRepository,
            UserRepository userRepository) {
        this.bidActionRepository = bidActionRepository;
        this.biddingRepository = biddingRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidAction> getBidsByBidding(Long biddingID) {
        requireBidding(biddingID);
        return bidActionRepository.findByBidding_IdOrderByAmountDesc(biddingID);
    }

    @Override
    @Transactional(readOnly = true)
    public BidAction getHighestBid(Long biddingID) {
        requireBidding(biddingID);
        return bidActionRepository.findTopByBidding_IdOrderByAmountDesc(biddingID)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No bids yet for bidding: " + biddingID));
    }

    @Override
    @Transactional(readOnly = true)
    public List<BidAction> getBidsByUser(Long userID) {
        if (!userRepository.existsById(userID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found: " + userID);
        }
        return bidActionRepository.findByUser_Id(userID);
    }

    private void requireBidding(Long biddingID) {
        if (!biddingRepository.existsById(biddingID)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Bidding not found: " + biddingID);
        }
    }
}