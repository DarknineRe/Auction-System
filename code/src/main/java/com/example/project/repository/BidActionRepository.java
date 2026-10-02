package com.example.project.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.BidAction;

@Repository
public interface BidActionRepository extends JpaRepository<BidAction, Long> {
    List<BidAction> findByBidding_IdOrderByAmountDesc(Long biddingID);
    Optional<BidAction> findTopByBidding_IdOrderByAmountDesc(Long biddingID);
    List<BidAction> findByUser_Id(Long userID);
}