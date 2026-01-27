package com.example.demo.realtime;

import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

/**
 * Publishes WebSocket events to clients.
 */
@Service
public class WsEventPublisher {

    private final SimpMessagingTemplate messagingTemplate;

    public WsEventPublisher(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void publishTableEvent(String type, Long tableId) {
        messagingTemplate.convertAndSend("/topic/tables", WsEvent.table(type, tableId));
    }

    public void publishInvoiceEvent(String type, Long invoiceId, Long tableId) {
        messagingTemplate.convertAndSend("/topic/invoices", WsEvent.invoice(type, invoiceId, tableId));
    }
}
