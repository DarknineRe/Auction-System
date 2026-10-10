package com.example.project.controller.web;

import org.springframework.data.domain.Page;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.project.dto.request.RejectPaymentRequest;
import com.example.project.dto.request.ShipPaymentRequest;
import com.example.project.dto.request.SubmitPaymentSlipRequest;
import com.example.project.model.Payment;
import com.example.project.model.User;
import com.example.project.service.PaymentService;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/** Purchases, sales and the payment workflow (slip, confirm, reject, ship). */
@Controller
@Validated
public class PaymentPageController {

    private final PaymentService paymentService;
    private final WebSupport web;

    public PaymentPageController(PaymentService paymentService, WebSupport web) {
        this.paymentService = paymentService;
        this.web = web;
    }

    @GetMapping("/purchases")
    public String purchases(
            Authentication authentication, @RequestParam(defaultValue = "0") int page, Model model) {
        User user = web.currentUser(authentication);
        Page<Payment> payments = paymentService.getPurchases(user.getId(), null, web.newestFirst(page));
        return paymentList(model, payments, "purchases", "My purchases");
    }

    @GetMapping("/sales")
    public String sales(
            Authentication authentication, @RequestParam(defaultValue = "0") int page, Model model) {
        User user = web.currentUser(authentication);
        Page<Payment> payments = paymentService.getSales(user.getId(), null, web.newestFirst(page));
        return paymentList(model, payments, "sales", "My sales");
    }

    @GetMapping("/payments/{paymentId}")
    public String payment(@PathVariable Long paymentId, Authentication authentication, Model model) {
        User user = web.currentUser(authentication);
        model.addAttribute("payment", paymentService.getPayment(paymentId, user.getId()));
        model.addAttribute("currentUserId", user.getId());
        return web.workspace(model, "payment-detail", "Payment details");
    }

    @PostMapping("/payments/{paymentId}/slip")
    public String submitPaymentSlip(
            @PathVariable Long paymentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 2048) String slipUrl) {
        web.validate(new SubmitPaymentSlipRequest(slipUrl));
        User buyer = web.currentUser(authentication);
        paymentService.submitSlip(paymentId, buyer.getId(), slipUrl.trim());
        return paymentSuccess(paymentId);
    }

    @PostMapping("/payments/{paymentId}/confirm")
    public String confirmPayment(@PathVariable Long paymentId, Authentication authentication) {
        User seller = web.currentUser(authentication);
        paymentService.confirmPayment(paymentId, seller.getId());
        return paymentSuccess(paymentId);
    }

    @PostMapping("/payments/{paymentId}/reject")
    public String rejectPayment(
            @PathVariable Long paymentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 500) String reason) {
        web.validate(new RejectPaymentRequest(reason));
        User seller = web.currentUser(authentication);
        paymentService.rejectPayment(paymentId, seller.getId(), reason.trim());
        return paymentSuccess(paymentId);
    }

    @PostMapping("/payments/{paymentId}/ship")
    public String shipPayment(
            @PathVariable Long paymentId,
            Authentication authentication,
            @RequestParam @NotBlank @Size(max = 100) String carrier,
            @RequestParam @NotBlank @Size(max = 100) String trackingNumber,
            @RequestParam @NotBlank @Size(max = 2048) String trackingUrl,
            @RequestParam(required = false) @Size(max = 1000) String shippingNote) {
        User seller = web.currentUser(authentication);
        web.validate(new ShipPaymentRequest(carrier, trackingNumber, trackingUrl, shippingNote));
        paymentService.ship(paymentId, seller.getId(), carrier.trim(), trackingNumber.trim(),
                trackingUrl.trim(), shippingNote);
        return paymentSuccess(paymentId);
    }

    private String paymentList(Model model, Page<Payment> payments, String page, String title) {
        model.addAttribute("payments", payments.getContent());
        model.addAttribute("tablePage", payments);
        return web.workspace(model, page, title);
    }

    private static String paymentSuccess(Long paymentId) {
        return "redirect:/payments/" + paymentId + "?success";
    }
}
