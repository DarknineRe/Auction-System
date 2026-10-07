package com.example.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.project.model.Sellerprofile;

@Repository
public interface SellerprofileRepository extends JpaRepository<Sellerprofile, Long> {
    Optional<Sellerprofile> findByUser_Id(Long userID);
    boolean existsByUser_Id(Long userID);

    // Single-column updates, so concurrent sales and ratings cannot overwrite each other's counters.
    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Sellerprofile s set s.salecount = s.salecount + :delta where s.sellprofileId = :id")
    int addToSalecount(@Param("id") Long sellerProfileId, @Param("delta") int delta);

    @Modifying(flushAutomatically = true, clearAutomatically = true)
    @Query("update Sellerprofile s set s.rating = :rating where s.sellprofileId = :id")
    int updateRating(@Param("id") Long sellerProfileId, @Param("rating") double rating);
}
