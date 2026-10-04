package com.example.project.service.implementation;

import java.util.Locale;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.example.project.model.Sellerprofile;
import com.example.project.model.User;
import com.example.project.repository.SellerprofileRepository;
import com.example.project.repository.UserRepository;
import com.example.project.service.SellerprofileService;

@Service
public class SellerprofileServiceImpl implements SellerprofileService {

    private final SellerprofileRepository sellerprofileRepository;
    private final UserRepository userRepository;

    public SellerprofileServiceImpl(
            SellerprofileRepository sellerprofileRepository,
            UserRepository userRepository) {
        this.sellerprofileRepository = sellerprofileRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public Sellerprofile createSellerProfile(String email, String bankaccount) {
        validateBankaccount(bankaccount);
        User user = findUserByEmail(email);
        if (sellerprofileRepository.existsByUser_Id(user.getId())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Seller profile already exists");
        }

        Sellerprofile sellerprofile = new Sellerprofile();
        sellerprofile.setUser(user);
        sellerprofile.setBankaccount(bankaccount.trim());
        return sellerprofileRepository.save(sellerprofile);
    }

    @Override
    @Transactional(readOnly = true)
    public Sellerprofile getCurrentSellerProfile(String email) {
        User user = findUserByEmail(email);
        return findSellerProfileByUserId(user.getId());
    }

    @Override
    @Transactional
    public Sellerprofile updateCurrentSellerProfile(String email, String bankaccount) {
        validateBankaccount(bankaccount);
        User user = findUserByEmail(email);
        Sellerprofile sellerprofile = findSellerProfileByUserId(user.getId());
        sellerprofile.setBankaccount(bankaccount.trim());
        return sellerprofileRepository.save(sellerprofile);
    }

    private User findUserByEmail(String email) {
        return userRepository.findByEmail(email.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    private Sellerprofile findSellerProfileByUserId(Long userId) {
        return sellerprofileRepository.findByUser_Id(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Seller profile not found"));
    }

    private void validateBankaccount(String bankaccount) {
        if (bankaccount == null || bankaccount.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Bank account is required");
        }
    }
}
