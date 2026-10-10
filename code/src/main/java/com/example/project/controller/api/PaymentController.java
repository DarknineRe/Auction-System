package com.example.project.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.RejectPaymentRequest;
import com.example.project.dto.request.ShipPaymentRequest;
import com.example.project.dto.request.SubmitPaymentSlipRequest;
import com.example.project.dto.response.PaymentResponse;
import com.example.project.mapper.PaymentMapper;
import com.example.project.model.Payment;
import com.example.project.model.User;
import com.example.project.service.PaymentService;
import com.example.project.service.UserService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/payments")
public class PaymentController {

    private final PaymentService paymentService;
    private final UserService userService;
    private final PaymentMapper paymentMapper;

    public PaymentController(PaymentService paymentService, UserService userService, PaymentMapper paymentMapper) {
        this.paymentService = paymentService;
        this.userService = userService;
        this.paymentMapper = paymentMapper;
    }

    @GetMapping("/purchases")
    public ResponseEntity<PagedModel<PaymentResponse>> getPurchases(
            Authentication authentication,
            @RequestParam(required = false) Payment.Status status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        User buyer = userService.getCurrentUser(authentication.getName());
        Page<PaymentResponse> page = paymentService.getPurchases(buyer.getId(), status, pageable)
                .map(paymentMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/sales")
    public ResponseEntity<PagedModel<PaymentResponse>> getSales(
            Authentication authentication,
            @RequestParam(required = false) Payment.Status status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        User seller = userService.getCurrentUser(authentication.getName());
        Page<PaymentResponse> page = paymentService.getSales(seller.getId(), status, pageable)
                .map(paymentMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(paymentService.getPayment(paymentId, user.getId())));
    }

    @PostMapping("/{paymentId}/slip")
    public ResponseEntity<PaymentResponse> submitSlip(
            @PathVariable Long paymentId,
            Authentication authentication,
            @Valid @RequestBody SubmitPaymentSlipRequest request) {
        User buyer = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(
                paymentService.submitSlip(paymentId, buyer.getId(), request.slipUrl())));
    }

    @PostMapping("/{paymentId}/qr")
    public ResponseEntity<PaymentResponse> startQrPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {
        User buyer = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(
                paymentService.startQrPayment(paymentId, buyer.getId())));
    }

    // Polled by the payment page while a QR code is shown; checks the provider before answering.
    @GetMapping("/{paymentId}/qr/status")
    public ResponseEntity<PaymentResponse> getQrPaymentStatus(
            @PathVariable Long paymentId,
            Authentication authentication) {
        User user = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(
                paymentService.refreshQrPayment(paymentId, user.getId())));
    }

    @PostMapping("/{paymentId}/confirm")
    public ResponseEntity<PaymentResponse> confirmPayment(
            @PathVariable Long paymentId,
            Authentication authentication) {
        User seller = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(
                paymentService.confirmPayment(paymentId, seller.getId())));
    }

    @PostMapping("/{paymentId}/reject")
    public ResponseEntity<PaymentResponse> rejectPayment(
            @PathVariable Long paymentId,
            Authentication authentication,
            @Valid @RequestBody RejectPaymentRequest request) {
        User seller = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(
                paymentService.rejectPayment(paymentId, seller.getId(), request.reason())));
    }

    @PostMapping("/{paymentId}/ship")
    public ResponseEntity<PaymentResponse> ship(
            @PathVariable Long paymentId,
            Authentication authentication,
            @Valid @RequestBody ShipPaymentRequest request) {
        User seller = userService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(paymentMapper.toResponse(paymentService.ship(
                paymentId,
                seller.getId(),
                request.carrier(),
                request.trackingNumber(),
                request.trackingUrl(),
                request.shippingNote())));
    }
}
