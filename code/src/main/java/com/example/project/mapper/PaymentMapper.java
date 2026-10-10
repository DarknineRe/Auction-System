package com.example.project.mapper;

import org.springframework.stereotype.Component;

import com.example.project.dto.response.PaymentResponse;
import com.example.project.model.Payment;
import com.example.project.model.Sellerprofile;

@Component
public class PaymentMapper {

    public PaymentResponse toResponse(Payment payment) {
        Sellerprofile seller = payment.getSellerprofile();

        return new PaymentResponse(
                payment.getId(),
                payment.getBidding().getId(),
                payment.getBuyer().getId(),
                seller.getUser().getId(),
                seller.getSellprofileId(),
                seller.getBankaccount(),
                payment.getAmount(),
                payment.getStatus(),
                payment.getCreatedAt(),
                payment.getDueDate(),
                payment.getSlipUrl(),
                payment.getPaidAt(),
                payment.getConfirmedAt(),
                payment.getRejectReason(),
                payment.getShippingAddress(),
                payment.getCarrier(),
                payment.getTrackingNumber(),
                payment.getTrackingUrl(),
                payment.getShippingNote(),
                payment.getShippedAt(),
                payment.getCompletedAt(),
                payment.getCancelledAt(),
                payment.getCancelReason());
    }
}
