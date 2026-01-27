package com.example.demo.realtime;

import java.time.Instant;

/**
 * WebSocket event payload.
 */
public record WsEvent(String type, Long tableId, Long invoiceId, Instant timestamp) {
    public static WsEvent table(String type, Long tableId) {
        return new WsEvent(type, tableId, null, Instant.now());
    }

    public static WsEvent invoice(String type, Long invoiceId, Long tableId) {
        return new WsEvent(type, tableId, invoiceId, Instant.now());
    }
}
