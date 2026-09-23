package com.example.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.Bidding;


@Repository
public interface BiddingRepository extends JpaRepository<Bidding, Long> {
    List<Bidding> findByArtwork_ArtworkID(Long artworkID);
    List<Bidding> findByOwner_UserID(Long userID);

}
