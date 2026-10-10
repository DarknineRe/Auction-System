package com.example.project.controller.api;

import java.util.Map;
import java.util.regex.Pattern;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.project.service.PaymentService;

@RestController
@RequestMapping("/api/v1/webhooks")
public class PaymentWebhookController {

    private static final Pattern CHARGE_ID = Pattern.compile("chrg_[A-Za-z0-9_]{1,80}");

    private final PaymentService paymentService;

    public PaymentWebhookController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Anyone can call this endpoint, so only the charge id is taken from the body;
    // the charge itself is then read back from the provider.
    @PostMapping("/omise")
    public ResponseEntity<Void> omise(@RequestBody Map<String, Object> event) {
        if (event.get("data") instanceof Map<?, ?> data
                && "charge".equals(data.get("object"))
                && data.get("id") instanceof String chargeId
                && CHARGE_ID.matcher(chargeId).matches()) {
            paymentService.handleGatewayCharge(chargeId);
        }
        return ResponseEntity.ok().build();
    }
}
