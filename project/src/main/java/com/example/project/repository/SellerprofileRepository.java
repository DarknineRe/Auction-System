package com.example.project.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.project.domain.entity.Sellerprofile;

@Repository
public interface SellerprofileRepository extends JpaRepository<Sellerprofile, Long> {
}