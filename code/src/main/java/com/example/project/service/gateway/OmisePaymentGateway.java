package com.example.project.service.gateway;

import java.math.BigDecimal;
import java.net.http.HttpClient;
import java.time.Duration;
import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

// PromptPay QR payments through Opn Payments (Omise): https://docs.opn.ooo/promptpay
@Component
public class OmisePaymentGateway implements PaymentGateway {

    private static final Logger log = LoggerFactory.getLogger(OmisePaymentGateway.class);
    private static final ParameterizedTypeReference<Map<String, Object>> JSON_OBJECT =
            new ParameterizedTypeReference<>() {
            };

    private final RestClient restClient;
    private final boolean enabled;

    public OmisePaymentGateway(
            @Value("${app.payment.omise.secret-key:}") String secretKey,
            @Value("${app.payment.omise.base-url:https://api.omise.co}") String baseUrl) {
        this.enabled = !secretKey.isBlank();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build());
        requestFactory.setReadTimeout(Duration.ofSeconds(15));
        this.restClient = RestClient.builder()
                .baseUrl(baseUrl)
                .requestFactory(requestFactory)
                .defaultHeaders(headers -> headers.setBasicAuth(secretKey.trim(), ""))
                .build();
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public GatewayCharge createPromptPayCharge(Long paymentId, BigDecimal amount) {
        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("amount", String.valueOf(PaymentGateway.toMinorUnits(amount)));
        form.add("currency", CURRENCY);
        form.add("source[type]", "promptpay");
        form.add("metadata[payment_id]", String.valueOf(paymentId));
        try {
            return toCharge(restClient.post()
                    .uri("/charges")
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(form)
                    .retrieve()
                    .body(JSON_OBJECT));
        } catch (RestClientException e) {
            throw unavailable("create a charge for payment " + paymentId, e);
        }
    }

    @Override
    public GatewayCharge getCharge(String chargeId) {
        try {
            return toCharge(restClient.get()
                    .uri("/charges/{id}", chargeId)
                    .retrieve()
                    .body(JSON_OBJECT));
        } catch (RestClientException e) {
            throw unavailable("read charge " + chargeId, e);
        }
    }

    private GatewayCharge toCharge(Map<String, Object> body) {
        if (body == null || !(body.get("id") instanceof String id)) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "The payment provider sent an invalid response");
        }
        Map<?, ?> image = child(child(child(body, "source"), "scannable_code"), "image");
        return new GatewayCharge(
                id,
                toStatus(body.get("status")),
                body.get("amount") instanceof Number amount ? amount.longValue() : -1,
                body.get("currency") instanceof String currency ? currency.toUpperCase(Locale.ROOT) : null,
                toPaymentId(child(body, "metadata").get("payment_id")),
                image.get("download_uri") instanceof String uri ? uri : null,
                toDate(body.get("expires_at")));
    }

    private static Map<?, ?> child(Map<?, ?> parent, String key) {
        return parent.get(key) instanceof Map<?, ?> map ? map : Map.of();
    }

    private static GatewayCharge.Status toStatus(Object status) {
        if ("successful".equals(status)) {
            return GatewayCharge.Status.SUCCESSFUL;
        }
        return "pending".equals(status) ? GatewayCharge.Status.PENDING : GatewayCharge.Status.FAILED;
    }

    private static Long toPaymentId(Object value) {
        try {
            return value == null ? null : Long.valueOf(value.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static Date toDate(Object value) {
        try {
            return value instanceof String text ? Date.from(Instant.parse(text)) : null;
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private ResponseStatusException unavailable(String action, RestClientException e) {
        String detail = e instanceof RestClientResponseException response
                ? response.getStatusCode() + " " + response.getResponseBodyAsString()
                : e.getMessage();
        log.error("Omise failed to {}: {}", action, detail);
        return new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                "The payment provider could not process the request. Please try again.");
    }
}
