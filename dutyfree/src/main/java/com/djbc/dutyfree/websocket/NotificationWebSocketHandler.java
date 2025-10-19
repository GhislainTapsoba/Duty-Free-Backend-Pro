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
public class NotificationWebSocketHandler extends TextWebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.put(session.getId(), session);
        log.info("Notification WebSocket connection established: {}", session.getId());

        Map<String, Object> message = Map.of(
                "type", "CONNECTION_ESTABLISHED",
                "message", "Connected to Notifications WebSocket",
                "sessionId", session.getId()
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(message)));
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        String payload = message.getPayload();
        log.info("Received notification message from {}: {}", session.getId(), payload);

        Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
        String type = (String) messageData.get("type");

        switch (type) {
            case "PING":
                sendPong(session);
                break;
            case "SUBSCRIBE_USER":
                handleSubscribeUser(session, messageData);
                break;
            case "MARK_READ":
                handleMarkRead(session, messageData);
                break;
            default:
                log.warn("Unknown notification message type: {}", type);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        sessions.remove(session.getId());
        log.info("Notification WebSocket connection closed: {} with status: {}", session.getId(), status);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("Notification WebSocket transport error for session {}: {}",
                session.getId(), exception.getMessage());
        sessions.remove(session.getId());
    }

    /**
     * Send notification to all connected clients
     */
    public void broadcastNotification(String notificationType, String title, String message, String severity) {
        Map<String, Object> notification = Map.of(
                "type", "NOTIFICATION",
                "notificationType", notificationType,
                "title", title,
                "message", message,
                "severity", severity,
                "timestamp", System.currentTimeMillis()
        );

        broadcastMessage(notification);
    }

    /**
     * Send notification to specific user
     */
    public void sendToUser(Long userId, String notificationType, String title, String message, String severity) {
        Map<String, Object> notification = Map.of(
                "type", "NOTIFICATION",
                "notificationType", notificationType,
                "title", title,
                "message", message,
                "severity", severity,
                "timestamp", System.currentTimeMillis()
        );

        sessions.values().forEach(session -> {
            try {
                Object userIdAttr = session.getAttributes().get("userId");
                if (userIdAttr != null && userIdAttr.equals(userId)) {
                    session.sendMessage(new TextMessage(objectMapper.writeValueAsString(notification)));
                }
            } catch (IOException e) {
                log.error("Error sending notification to session {}: {}", session.getId(), e.getMessage());
            }
        });
    }

    /**
     * Notify about low stock
     */
    public void notifyLowStock(String productName, Integer currentStock) {
        String message = String.format("Product '%s' is low on stock. Current stock: %d units",
                productName, currentStock);
        broadcastNotification("LOW_STOCK", "Low Stock Alert", message, "WARNING");
    }

    /**
     * Notify about expiring stock
     */
    public void notifyExpiringStock(String productName, String expiryDate) {
        String message = String.format("Product '%s' is expiring soon on %s", productName, expiryDate);
        broadcastNotification("EXPIRING_STOCK", "Expiring Stock Alert", message, "WARNING");
    }

    /**
     * Notify about sommier alert
     */
    public void notifySommierAlert(String sommierNumber, Integer daysOpen) {
        String message = String.format("Sommier '%s' has been open for %d days and needs clearing",
                sommierNumber, daysOpen);
        broadcastNotification("SOMMIER_ALERT", "Sommier Clearing Alert", message, "ERROR");
    }

    /**
     * Notify about system event
     */
    public void notifySystemEvent(String eventType, String message) {
        broadcastNotification("SYSTEM", eventType, message, "INFO");
    }

    /**
     * Notify about cash register variance
     */
    public void notifyCashVariance(String registerNumber, String variance) {
        String message = String.format("Cash register '%s' has a variance of %s", registerNumber, variance);
        broadcastNotification("CASH_VARIANCE", "Cash Variance Detected", message, "WARNING");
    }

    private void sendPong(WebSocketSession session) throws IOException {
        Map<String, Object> pong = Map.of(
                "type", "PONG",
                "timestamp", System.currentTimeMillis()
        );
        session.sendMessage(new TextMessage(objectMapper.writeValueAsString(pong)));
    }

    private void handleSubscribeUser(WebSocketSession session, Map<String, Object> messageData) {
        Object userId = messageData.get("userId");
        if (userId != null) {
            session.getAttributes().put("userId", userId);
            log.info("Session {} subscribed to user notifications {}", session.getId(), userId);
        }
    }

    private void handleMarkRead(WebSocketSession session, Map<String, Object> messageData) {
        Object notificationId = messageData.get("notificationId");
        log.info("Marking notification {} as read for session {}", notificationId, session.getId());
        // TODO: Implement mark as read logic if you have a notification persistence layer
    }

    private void broadcastMessage(Map<String, Object> message) {
        String jsonMessage;
        try {
            jsonMessage = objectMapper.writeValueAsString(message);
        } catch (Exception e) {
            log.error("Error serializing notification message: {}", e.getMessage());
            return;
        }

        sessions.values().forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(jsonMessage));
                }
            } catch (IOException e) {
                log.error("Error broadcasting notification to session {}: {}", session.getId(), e.getMessage());
            }
        });
    }
}