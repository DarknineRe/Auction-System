package com.example.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.Bidding;


@Repository
public interface BiddingRepository extends JpaRepository<Bidding, Long> {
    List<Bidding> findByArtworks_Id(Long artworkID);
    List<Bidding> findByOwner_Id(Long userID);
}