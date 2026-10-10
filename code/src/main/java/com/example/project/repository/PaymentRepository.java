package com.example.project.repository;

import java.util.Collection;
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

import com.example.project.model.Payment;

import jakarta.persistence.LockModeType;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    Optional<Payment> findByBidding_Id(Long biddingID);

    boolean existsByBidding_Id(Long biddingID);

    Page<Payment> findByBuyer_Id(Long buyerID, Pageable pageable);

    Page<Payment> findByBuyer_IdAndStatus(Long buyerID, Payment.Status status, Pageable pageable);

    List<Payment> findByBuyer_IdAndBidding_IdIn(Long buyerID, Collection<Long> biddingIDs);

    Page<Payment> findBySellerprofile_User_Id(Long sellerUserID, Pageable pageable);

    Page<Payment> findBySellerprofile_User_IdAndStatus(Long sellerUserID, Payment.Status status, Pageable pageable);

    Page<Payment> findByStatus(Payment.Status status, Pageable pageable);

    @Query("select p.bidding.id from Payment p where p.id = :id")
    Optional<Long> findBiddingIdById(@Param("id") Long id);

    // Row lock so status changes on one payment run one at a time.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from Payment p where p.id = :id")
    Optional<Payment> findByIdForUpdate(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    List<Payment> findByStatusAndDueDateBefore(Payment.Status status, Date now);

    @Query("select p.id from Payment p where p.status = :status and p.dueDate < :now")
    List<Long> findIdsByStatusAndDueDateBefore(
            @Param("status") Payment.Status status, @Param("now") Date now);
}
