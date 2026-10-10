package com.example.project.repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
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
    Page<Bidding> findByOwner_Id(Long userID, Pageable pageable);
    Page<Bidding> findByOwner_IdAndStatus(Long userID, Bidding.Status status, Pageable pageable);
    Page<Bidding> findByWinner_Id(Long userID, Pageable pageable);
    Page<Bidding> findByStatus(Bidding.Status status, Pageable pageable);
    boolean existsByArtworks_Id(Long artworkID);
    // Row lock (SELECT ... FOR UPDATE) so bids, voids and status changes on one bidding run one at a time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Bidding b where b.id = :id")
    Optional<Bidding> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Bidding> findByStatusAndEndDateBefore(Bidding.Status status, Date now);

    @Query("select b.id from Bidding b where b.status = :status and b.endDate < :now")
    List<Long> findIdsByStatusAndEndDateBefore(
            @Param("status") Bidding.Status status, @Param("now") Date now);
    boolean existsByArtworks_IdAndStatus(Long artworkID, Bidding.Status status);

    @Query("select avg(b.sellerRating) from Bidding b where b.owner.id = :ownerId and b.sellerRating is not null")
    Double averageSellerRatingByOwnerId(@Param("ownerId") Long ownerId);
}