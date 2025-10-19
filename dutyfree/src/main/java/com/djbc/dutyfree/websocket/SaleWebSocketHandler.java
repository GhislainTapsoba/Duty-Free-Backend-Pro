package com.djbc.dutyfree.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class SaleWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("WebSocket connection established: {}", session.getId());

        // Send welcome message
        Map<String, Object> message = Map.of(
                "type", "CONNECTION_ESTABLISHED",
                "message", "Connected to Sales WebSocket",
                "sessionId", session.getId()
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received message from {}: {}", session.getId(), payload);

        // Parse message
        Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
        String type = (String) messageData.get("type");

        // Handle different message types
        switch (type) {
            case "PING":
                sendPong(session);
                break;
            case "SUBSCRIBE_REGISTER":
                handleSubscribeRegister(session, messageData);
                break;
            default:
                log.warn("Unknown message type: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("WebSocket connection closed: {} with status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket transport error for session {}: {}", session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    /**
     * Broadcast sale event to all connected clients
     */
    public void broadcastSaleEvent(String eventType, Object data) {
        Map<String, Object> message = Map.of(
                "type", eventType,
                "data", data,
                "timestamp", System.currentTimeMillis()
        );

        broadcastMessage(message);
    }

    /**
     * Send sale notification to specific cash register
     */
    public void sendToRegister(Long cashRegisterId, String eventType, Object data) {
        Map<String, Object> message = Map.of(
                "type", eventType,
                "cashRegisterId", cashRegisterId,
                "data", data,
                "timestamp", System.currentTimeMillis()
        );

        // Find sessions subscribed to this register
        sessions.values().forEach(session -> {
            try {
                // Check if session is subscribed to this register
                Object registerIdAttr = session.getAttributes().get("cashRegisterId");
                if (registerIdAttr != null && registerIdAttr.equals(cashRegisterId)) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
                }
            } catch (IOException e) {
                log.error("Error sending message to session {}: {}", session.getId(), e.getMessage());
            }
        });
    }

    private void sendPong(WebSocketSession session) throws IOException {
        Map<String, Object> pong = Map.of(
                "type", "PONG",
                "timestamp", System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
    }

    private void handleSubscribeRegister(WebSocketSession session, Map<String, Object> messageData) {
        Object cashRegisterId = messageData.get("cashRegisterId");
        if (cashRegisterId != null) {
            session.getAttributes().put("cashRegisterId", cashRegisterId);
            log.info("Session {} subscribed to cash register {}", session.getId(), cashRegisterId);
        }
    }

    private void broadcastMessage(Map<String, Object> message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing message: {}", e.getMessage());
            return;
        }

        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (IOException e) {
                log.error("Error broadcasting message to session {}: {}", session.getId(), e.getMessage());
            }
        });
    }

    /**
     * Notify about new sale
     */
    public void notifyNewSale(Long cashRegisterId, Object saleData) {
        sendToRegister(cashRegisterId, "NEW_SALE", saleData);
    }

    /**
     * Notify about completed sale
     */
    public void notifyCompletedSale(Long cashRegisterId, Object saleData) {
        sendToRegister(cashRegisterId, "SALE_COMPLETED", saleData);
    }

    /**
     * Notify about cancelled sale
     */
    public void notifyCancelledSale(Long cashRegisterId, Object saleData) {
        sendToRegister(cashRegisterId, "SALE_CANCELLED", saleData);
    }
}