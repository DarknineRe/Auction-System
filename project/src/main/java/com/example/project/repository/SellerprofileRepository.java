package com.example.project.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.model.Sellerprofile;

@Repository
public interface SellerprofileRepository extends JpaRepository<Sellerprofile, Long> {
    Optional<Sellerprofile> findByUser_Id(Long userID);
    boolean existsByUser_Id(Long userID);
}