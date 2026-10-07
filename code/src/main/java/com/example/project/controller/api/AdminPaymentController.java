package com.example.project.controller.api;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.dto.request.CancelPaymentRequest;
import com.example.project.dto.response.PaymentResponse;
import com.example.project.mapper.PaymentMapper;
import com.example.project.model.Payment;
import com.example.project.service.AdminPaymentService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/admin/payments")
@PreAuthorize("hasRole('ADMIN')")
public class AdminPaymentController {

    private final AdminPaymentService adminPaymentService;
    private final PaymentMapper paymentMapper;

    public AdminPaymentController(AdminPaymentService adminPaymentService, PaymentMapper paymentMapper) {
        this.adminPaymentService = adminPaymentService;
        this.paymentMapper = paymentMapper;
    }

    @GetMapping
    public ResponseEntity<PagedModel<PaymentResponse>> getPayments(
            @RequestParam(required = false) Payment.Status status,
            @PageableDefault(size = 10, sort = "id") Pageable pageable) {
        Page<PaymentResponse> page = adminPaymentService.getPayments(status, pageable)
                .map(paymentMapper::toResponse);

        return ResponseEntity.ok(new PagedModel<>(page));
    }

    @GetMapping("/{paymentId}")
    public ResponseEntity<PaymentResponse> getPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentMapper.toResponse(adminPaymentService.getPayment(paymentId)));
    }

    @PostMapping("/{paymentId}/cancel")
    public ResponseEntity<PaymentResponse> cancelPayment(
            @PathVariable Long paymentId,
            @Valid @RequestBody CancelPaymentRequest request) {
        return ResponseEntity.ok(paymentMapper.toResponse(
                adminPaymentService.cancelPayment(paymentId, request.reason())));
    }
}
