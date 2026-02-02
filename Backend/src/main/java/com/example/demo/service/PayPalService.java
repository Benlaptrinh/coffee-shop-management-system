package com.example.demo.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.net.http.HttpRequest;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.example.demo.config.PayPalConfig;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for PayPal payment processing.
 */
@Service
public class PayPalService {

    private static final Logger log = LoggerFactory.getLogger(PayPalService.class);

    private final PayPalConfig payPalConfig;
    private final ObjectMapper objectMapper;

    // Cache for access token
    private String accessToken;
    private long tokenExpiry;

    public PayPalService(PayPalConfig payPalConfig, ObjectMapper objectMapper) {
        this.payPalConfig = payPalConfig;
        this.objectMapper = objectMapper;
    }

    /**
     * Get access token for PayPal API.
     *
     * @return Access token
     */
    private String getAccessToken() {
        // Check if token is still valid
        if (accessToken != null && System.currentTimeMillis() < tokenExpiry) {
            return accessToken;
        }

        try {
            // Create authentication header
            String auth = payPalConfig.getClientId() + ":" + payPalConfig.getClientSecret();
            String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes());

            // Create request body
            String body = "grant_type=client_credentials";

            // Send request
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(payPalConfig.getApiBaseUrl() + "/v1/oauth2/token"))
                    .header("Authorization", "Basic " + encodedAuth)
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body))
                    .build();

            var client = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get PayPal access token: " + response.body());
            }

            // Parse response
            JsonNode jsonNode = objectMapper.readTree(response.body());
            accessToken = jsonNode.get("access_token").asText();
            int expiresIn = jsonNode.get("expires_in").asInt();
            tokenExpiry = System.currentTimeMillis() + (expiresIn - 60) * 1000; // 60 seconds buffer

            return accessToken;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get PayPal access token", e);
        }
    }

    /**
     * Create a PayPal order.
     *
     * @param amount The amount to charge
     * @param currency The currency code (e.g., "USD", "VND")
     * @param description The order description
     * @param invoiceId Your internal invoice ID
     * @return Order creation result with approval URL
     */
    public Map<String, Object> createOrder(BigDecimal amount, String currency, String description, String invoiceId) {
        try {
            String token = getAccessToken();

            log.info("Creating PayPal order: amount={}, currency={}, description={}, invoiceId={}",
                    amount, currency, description, invoiceId);

            // Format amount for PayPal (must have up to 10 digits, optional 2 decimal places)
            String formattedAmount = formatAmount(amount, currency);

            // Create request body
            Map<String, Object> body = new HashMap<>();
            body.put("intent", "CAPTURE");

            Map<String, Object> purchaseUnit = new HashMap<>();
            purchaseUnit.put("description", description);
            purchaseUnit.put("invoice_id", invoiceId);

            Map<String, Object> amountObj = new HashMap<>();
            amountObj.put("currency_code", currency);
            amountObj.put("value", formattedAmount);
            purchaseUnit.put("amount", amountObj);

            body.put("purchase_units", new Object[]{purchaseUnit});

            Map<String, Object> applicationContext = new HashMap<>();
            applicationContext.put("brand_name", "QuanCaPhe Pro");
            applicationContext.put("landing_page", "NO_PREFERENCE");
            applicationContext.put("user_action", "PAY_NOW");
            applicationContext.put("return_url", payPalConfig.getMode().equals("sandbox")
                    ? "http://localhost:5173/paypal/callback"
                    : "https://your-domain.com/paypal/callback");
            applicationContext.put("cancel_url", payPalConfig.getMode().equals("sandbox")
                    ? "http://localhost:5173/paypal/cancel"
                    : "https://your-domain.com/paypal/cancel");

            body.put("application_context", applicationContext);

            // Send request
            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(payPalConfig.getApiBaseUrl() + "/v2/checkout/orders"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            var client = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new RuntimeException("Failed to create PayPal order: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();

            result.put("orderId", jsonNode.get("id").asText());
            result.put("status", jsonNode.get("status").asText());

            // Find approval URL
            String approvalUrl = null;
            for (JsonNode link : jsonNode.get("links")) {
                if ("approve".equals(link.get("rel").asText())) {
                    approvalUrl = link.get("href").asText();
                    break;
                }
            }
            result.put("approvalUrl", approvalUrl);

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to create PayPal order", e);
        }
    }

    /**
     * Format amount for PayPal API requirements.
     * PayPal requires: no currency symbols, max 10 digits, optional 2 decimal places.
     */
    private String formatAmount(BigDecimal amount, String currency) {
        // For USD, use 2 decimal places
        if ("USD".equalsIgnoreCase(currency)) {
            DecimalFormat df = new DecimalFormat("0.00", new DecimalFormatSymbols(Locale.US));
            return df.format(amount);
        }
        // For VND and other currencies without decimals
        return amount.setScale(0, java.math.RoundingMode.HALF_UP).toString();
    }

    /**
     * Capture a PayPal order (after customer approves).
     *
     * @param orderId The PayPal order ID
     * @return Capture result
     */
    public Map<String, Object> captureOrder(String orderId) {
        try {
            String token = getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(payPalConfig.getApiBaseUrl() + "/v2/checkout/orders/" + orderId + "/capture"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString("{}"))
                    .build();

            var client = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new RuntimeException("Failed to capture PayPal order: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();

            result.put("orderId", jsonNode.get("id").asText());
            result.put("status", jsonNode.get("status").asText());

            // Get transaction details
            if (jsonNode.has("purchase_units") && jsonNode.get("purchase_units").size() > 0) {
                JsonNode purchaseUnit = jsonNode.get("purchase_units").get(0);
                if (purchaseUnit.has("payments")) {
                    JsonNode payments = purchaseUnit.get("payments");
                    if (payments.has("captures") && payments.get("captures").size() > 0) {
                        JsonNode capture = payments.get("captures").get(0);
                        result.put("transactionId", capture.get("id").asText());
                        result.put("transactionStatus", capture.get("status").asText());
                    }
                }
            }

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to capture PayPal order", e);
        }
    }

    /**
     * Get order details.
     *
     * @param orderId The PayPal order ID
     * @return Order details
     */
    public Map<String, Object> getOrder(String orderId) {
        try {
            String token = getAccessToken();

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(payPalConfig.getApiBaseUrl() + "/v2/checkout/orders/" + orderId))
                    .header("Authorization", "Bearer " + token)
                    .GET()
                    .build();

            var client = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                throw new RuntimeException("Failed to get PayPal order: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();

            result.put("orderId", jsonNode.get("id").asText());
            result.put("status", jsonNode.get("status").asText());

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to get PayPal order", e);
        }
    }

    /**
     * Create a refund for a captured payment.
     *
     * @param captureId The PayPal capture ID
     * @param amount The amount to refund (null for full refund)
     * @param reason The refund reason
     * @return Refund result
     */
    public Map<String, Object> refund(String captureId, BigDecimal amount, String reason) {
        try {
            String token = getAccessToken();

            Map<String, Object> body = new HashMap<>();
            if (amount != null) {
                body.put("amount", Map.of(
                        "value", amount.toString(),
                        "currency_code", "USD"
                ));
            }
            if (reason != null) {
                body.put("note_to_payer", reason);
            }

            String jsonBody = objectMapper.writeValueAsString(body);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(payPalConfig.getApiBaseUrl() + "/v2/payments/captures/" + captureId + "/refund"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            var client = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200 && response.statusCode() != 201) {
                throw new RuntimeException("Failed to refund: " + response.body());
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            Map<String, Object> result = new HashMap<>();

            result.put("refundId", jsonNode.get("id").asText());
            result.put("status", jsonNode.get("status").asText());

            return result;
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException("Failed to refund payment", e);
        }
    }

    /**
     * Verify PayPal webhook signature.
     *
     * @param webhookId The webhook ID
     * @param headers The webhook headers
     * @param body The webhook body
     * @return true if signature is valid
     */
    public boolean verifyWebhookSignature(String webhookId, Map<String, String> headers, String body) {
        try {
            String token = getAccessToken();

            Map<String, Object> bodyMap = new HashMap<>();
            bodyMap.put("auth_algo", headers.get("paypal-auth-algo"));
            bodyMap.put("cert_url", headers.get("paypal-cert-url"));
            bodyMap.put("transmission_id", headers.get("paypal-transmission-id"));
            bodyMap.put("transmission_sig", headers.get("paypal-transmission-sig"));
            bodyMap.put("transmission_time", headers.get("paypal-transmission-time"));
            bodyMap.put("webhook_id", webhookId);
            bodyMap.put("webhook_event", objectMapper.readTree(body));

            String jsonBody = objectMapper.writeValueAsString(bodyMap);

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(java.net.URI.create(payPalConfig.getApiBaseUrl() + "/v1/notifications/verify-webhook-signature"))
                    .header("Authorization", "Bearer " + token)
                    .header("Content-Type", "application/json")
                    .POST(java.net.http.HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            var client = java.net.http.HttpClient.newHttpClient();
            var response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                return false;
            }

            JsonNode jsonNode = objectMapper.readTree(response.body());
            return "SUCCESS".equals(jsonNode.get("verification_status").asText());
        } catch (IOException | InterruptedException e) {
            return false;
        }
    }
}

