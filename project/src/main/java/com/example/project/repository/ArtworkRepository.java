package com.example.project.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.Artwork;


@Repository
public interface ArtworkRepository extends JpaRepository<Artwork, Long> {
    List<Artwork> findBySellerprofile_SellprofileId(Long sellProfileId);
    List<Artwork> findBySellerprofile_User_Id(Long userId);
}
