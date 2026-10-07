package com.example.project.service;

import com.example.project.model.Sellerprofile;

public interface SellerprofileService {
    Sellerprofile createSellerProfile(String email, String bankaccount);

    Sellerprofile getCurrentSellerProfile(String email);

    Sellerprofile updateCurrentSellerProfile(String email, String bankaccount);
}
