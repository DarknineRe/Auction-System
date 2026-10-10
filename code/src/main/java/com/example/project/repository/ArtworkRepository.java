package com.example.project.repository;

import java.util.List;
import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.project.model.Artwork;

import jakarta.persistence.LockModeType;

@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findBySellerprofile_SellprofileId(Long sellProfileId);
    List<Artwork> findBySellerprofile_User_Id(Long userId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Artwork a where a.id in :ids order by a.id")
    List<Artwork> findAllByIdForUpdate(@Param("ids") Collection<Long> ids);
}
