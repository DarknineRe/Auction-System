package com.example.project.repository;

import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.project.model.*;;


@Repository
public class BiddingRepository extends JpaRepository<Bidding, Long> {
    List<Bidding> findByArtwork_ArtworkID(Long artworkID);
    List<Bidding> findByOwner_UserID(Long userID);

}
