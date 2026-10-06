package com.example.project.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.project.model.Bidding;

import jakarta.persistence.LockModeType;


@Repository
public interface BiddingRepository extends JpaRepository<Bidding, Long> {
    List<Bidding> findByArtworks_Id(Long artworkID);
    List<Bidding> findByOwner_Id(Long userID);
    boolean existsByArtworks_Id(Long artworkID);
    // Row lock (SELECT ... FOR UPDATE) so bids, voids and status changes on one bidding run one at a time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bidding b where b.id = :id")
    Optional<Bidding> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Bidding> findByStatusAndEndDateBefore(Bidding.Status status, Date now);
    boolean existsByArtworks_IdAndStatus(Long artworkID, Bidding.Status status);
}