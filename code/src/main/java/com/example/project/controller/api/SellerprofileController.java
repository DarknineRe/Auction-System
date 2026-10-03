package com.example.project.controller.api;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.SellerprofileRequest;
import com.example.project.dto.response.SellerprofileResponse;
import com.example.project.mapper.SellerprofileMapper;
import com.example.project.model.Sellerprofile;
import com.example.project.service.SellerprofileService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/seller-profiles")
public class SellerprofileController {

    private final SellerprofileService sellerprofileService;
    private final SellerprofileMapper sellerprofileMapper;

    public SellerprofileController(
            SellerprofileService sellerprofileService,
            SellerprofileMapper sellerprofileMapper) {
        this.sellerprofileService = sellerprofileService;
        this.sellerprofileMapper = sellerprofileMapper;
    }
    // รับ request สมัคร Sellerprofile ของผู้ใช้ที่ล็อกอินอยู่
    @PostMapping
    public ResponseEntity<SellerprofileResponse> createSellerProfile(
            Authentication authentication,
            @Valid @RequestBody SellerprofileRequest request) {
        Sellerprofile sellerprofile = sellerprofileService.createSellerProfile(
                authentication.getName(),
                request.bankaccount());

        return ResponseEntity.status(HttpStatus.CREATED).body(sellerprofileMapper.toResponse(sellerprofile));
    }
    // แสดง Sellerprofile ของผู้ใช้ที่ล็อกอินอยู่ โดยไม่รับ user ID จาก request
    @GetMapping("/me")
    public ResponseEntity<SellerprofileResponse> getCurrentSellerProfile(Authentication authentication) {
        Sellerprofile sellerprofile = sellerprofileService.getCurrentSellerProfile(authentication.getName());
        return ResponseEntity.ok(sellerprofileMapper.toResponse(sellerprofile));
    }
    // แก้เลขบัญชีของ Sellerprofile ของผู้ใช้ที่ล็อกอินอยู่
    @PutMapping("/me")
    public ResponseEntity<SellerprofileResponse> updateCurrentSellerProfile(
            Authentication authentication,
            @Valid @RequestBody SellerprofileRequest request) {
        Sellerprofile sellerprofile = sellerprofileService.updateCurrentSellerProfile(
                authentication.getName(),
                request.bankaccount());

        return ResponseEntity.ok(sellerprofileMapper.toResponse(sellerprofile));
    }
}