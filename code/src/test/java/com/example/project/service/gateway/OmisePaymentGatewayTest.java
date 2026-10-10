package com.example.project.service.gateway;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.InetSocketAddress;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import com.sun.net.httpserver.HttpServer;

class OmisePaymentGatewayTest {

    private static final String CHARGE_JSON = """
            {"object":"charge","id":"chrg_test_123","amount":150050,"currency":"THB","status":"%s",
             "paid":false,"expires_at":"2026-10-11T10:00:00Z","metadata":{"payment_id":"12"},
             "source":{"object":"source","type":"promptpay","scannable_code":{"object":"barcode",
               "image":{"object":"document","download_uri":"https://qr.test/code.svg"}}}}
            """;

    private HttpServer server;
    private OmisePaymentGateway gateway;
    private final AtomicReference<String> requestBody = new AtomicReference<>();
    private final AtomicReference<String> authorization = new AtomicReference<>();

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        server.start();
        gateway = new OmisePaymentGateway("skey_test_abc", "http://127.0.0.1:" + server.getAddress().getPort());
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void createsPromptPayChargeInSatangAndReadsQrCode() {
        respond("/charges", 200, CHARGE_JSON.formatted("pending"));

        GatewayCharge charge = gateway.createPromptPayCharge(12L, new BigDecimal("1500.5000"));

        String form = URLDecoder.decode(requestBody.get(), StandardCharsets.UTF_8);
        assertTrue(form.contains("amount=150050"), form);
        assertTrue(form.contains("currency=THB"), form);
        assertTrue(form.contains("source[type]=promptpay"), form);
        assertTrue(form.contains("metadata[payment_id]=12"), form);
        assertEquals("Basic " + Base64.getEncoder().encodeToString("skey_test_abc:".getBytes(StandardCharsets.UTF_8)),
                authorization.get());
        assertEquals("chrg_test_123", charge.id());
        assertEquals(GatewayCharge.Status.PENDING, charge.status());
        assertEquals(150050, charge.amountMinor());
        assertEquals("THB", charge.currency());
        assertEquals(12L, charge.paymentId());
        assertEquals("https://qr.test/code.svg", charge.qrImageUrl());
        assertEquals(Date.from(Instant.parse("2026-10-11T10:00:00Z")), charge.expiresAt());
    }

    @Test
    void readsSuccessfulCharge() {
        respond("/charges/chrg_test_123", 200, CHARGE_JSON.formatted("successful"));

        assertEquals(GatewayCharge.Status.SUCCESSFUL, gateway.getCharge("chrg_test_123").status());
    }

    @Test
    void providerErrorBecomesBadGateway() {
        respond("/charges", 401, "{\"object\":\"error\",\"code\":\"authentication_failure\"}");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> gateway.createPromptPayCharge(12L, BigDecimal.TEN));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
    }

    @Test
    void isDisabledWithoutSecretKey() {
        assertFalse(new OmisePaymentGateway(" ", "http://127.0.0.1:1").isEnabled());
        assertTrue(gateway.isEnabled());
    }

    private void respond(String path, int status, String json) {
        server.createContext(path, exchange -> {
            requestBody.set(new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            authorization.set(exchange.getRequestHeaders().getFirst("Authorization"));
            byte[] body = json.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().add("Content-Type", "application/json");
            exchange.sendResponseHeaders(status, body.length);
            exchange.getResponseBody().write(body);
            exchange.close();
        });
    }
}
