package com.example.project.controller.web;

import java.util.Optional;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Sellerprofile;
import com.example.project.service.SellerprofileService;

/** Looks up the signed-in user's seller profile, treating "not found" as an empty result. */
@Component
public class CurrentSellerProfile {

    private final SellerprofileService sellerprofileService;

    public CurrentSellerProfile(SellerprofileService sellerprofileService) {
        this.sellerprofileService = sellerprofileService;
    }

    public Optional<Sellerprofile> find(String email) {
        try {
            return Optional.of(sellerprofileService.getCurrentSellerProfile(email));
        } catch (ResponseStatusException exception) {
            if (exception.getStatusCode() == HttpStatus.NOT_FOUND) {
                return Optional.empty();
            }
            throw exception;
        }
    }
}
