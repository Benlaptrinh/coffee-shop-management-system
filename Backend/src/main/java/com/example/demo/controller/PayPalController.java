package com.example.demo.controller;

import java.math.BigDecimal;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.demo.service.PayPalService;

/**
 * REST Controller for PayPal payment processing.
 */
@RestController
@RequestMapping("/api/paypal")
public class PayPalController {

    private final PayPalService payPalService;

    public PayPalController(PayPalService payPalService) {
        this.payPalService = payPalService;
    }

    /**
     * Create a PayPal order for payment.
     *
     * @param request Order request with amount, currency, description, invoiceId
     * @return Order with approval URL
     */
    @PostMapping("/create-order")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> createOrder(@RequestBody Map<String, Object> request) {
        BigDecimal amount = new BigDecimal(request.get("amount").toString());
        String currency = (String) request.getOrDefault("currency", "USD");
        String description = (String) request.get("description");
        String invoiceId = (String) request.get("invoiceId");

        Map<String, Object> result = payPalService.createOrder(amount, currency, description, invoiceId);
        return ResponseEntity.ok(result);
    }

    /**
     * Capture a PayPal order after customer approval.
     *
     * @param orderId PayPal order ID
     * @return Capture result
     */
    @PostMapping("/capture-order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> captureOrder(@PathVariable String orderId) {
        Map<String, Object> result = payPalService.captureOrder(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Get order details.
     *
     * @param orderId PayPal order ID
     * @return Order details
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Map<String, Object>> getOrder(@PathVariable String orderId) {
        Map<String, Object> result = payPalService.getOrder(orderId);
        return ResponseEntity.ok(result);
    }

    /**
     * Refund a payment.
     *
     * @param request Refund request with captureId, amount, reason
     * @return Refund result
     */
    @PostMapping("/refund")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> refund(@RequestBody Map<String, Object> request) {
        String captureId = (String) request.get("captureId");
        BigDecimal amount = request.get("amount") != null ? new BigDecimal(request.get("amount").toString()) : null;
        String reason = (String) request.get("reason");

        Map<String, Object> result = payPalService.refund(captureId, amount, reason);
        return ResponseEntity.ok(result);
    }

    /**
     * Handle PayPal webhook notifications.
     *
     * @param headers Webhook headers
     * @param body Webhook body
     * @return Success response
     */
    @PostMapping("/webhook")
    public ResponseEntity<Map<String, String>> handleWebhook(
            @RequestHeader Map<String, String> headers,
            @RequestBody String body) {
        // Get webhook ID from configuration or database
        String webhookId = "YOUR_WEBHOOK_ID";

        // Verify webhook signature
        boolean isValid = payPalService.verifyWebhookSignature(webhookId, headers, body);

        if (!isValid) {
            return ResponseEntity.status(401).body(Map.of("status", "invalid signature"));
        }

        // Process webhook event
        // You would parse the body and handle different event types
        // such as PAYMENT.CAPTURE.COMPLETED, PAYMENT.CAPTURE.DENIED, etc.

        return ResponseEntity.ok(Map.of("status", "received"));
    }
}
