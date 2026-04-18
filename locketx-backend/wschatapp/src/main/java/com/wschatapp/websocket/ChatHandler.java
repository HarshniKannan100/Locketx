package com.wschatapp.websocket;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.wschatapp.service.TempImageService;
import com.wschatapp.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.socket.*;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import com.wschatapp.model.ChatMessage;

public class ChatHandler extends TextWebSocketHandler {

    @Autowired
    private TempImageService tempImageService;
    // 🔑 Store userId → session
    Map<Long, List<WebSocketSession>> userSessions = new ConcurrentHashMap<>();
    private ObjectMapper objectMapper = new ObjectMapper();
    Map<Long, String> userNames = new ConcurrentHashMap<>();
    public Map<Long, List<WebSocketSession>> getUserSessions() {
        return userSessions;
    }

    public void setUserSessions(Map<Long, List<WebSocketSession>> userSessions) {
        this.userSessions = userSessions;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String query = session.getUri().getQuery();

        String token = null;

        for (String param : query.split("&")) {
            if (param.startsWith("token=")) {
                token = param.substring(6);
            }
        }

        Long userId = JwtUtil.validateToken(token);

        if (userId == null) {
            session.close();
            return;
        }
        if (userSessions.containsKey(userId)) {
            System.out.println("User already connected: " + userId);
        }
        userSessions.computeIfAbsent(userId, k -> new ArrayList<>()).add(session);
        broadcastUsers();
        System.out.println("User connected: " + userId);
        System.out.println("Current users: " + userSessions.keySet());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        // Convert JSON → object
        ChatMessage chatMessage = objectMapper.readValue(message.getPayload(), ChatMessage.class);

        for (Long receiverId : chatMessage.getReceiverIds()) {

            List<WebSocketSession> sessions = userSessions.get(receiverId);

            if (sessions != null) {
                for (WebSocketSession s : sessions) {
                    if (s.isOpen()) {
                        s.sendMessage(new TextMessage(objectMapper.writeValueAsString(chatMessage)));
                    }
                }
            }
        }
    }

    public Map<Long, String> getUserNames() {
        return userNames;
    }

    public void setUserNames(Map<Long, String> userNames) {
        this.userNames = userNames;
    }

    public TempImageService getTempImageService() {
        return tempImageService;
    }

    public void setTempImageService(TempImageService tempImageService) {
        this.tempImageService = tempImageService;
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {

        userSessions.forEach((userId, sessions) -> {
            sessions.remove(session);

            // If user fully disconnected
            if (sessions.isEmpty()) {

                // 🔥 delete all images of this user
                tempImageService.deleteUserImages(userId);

                System.out.println("Deleted images for user: " + userId);
            }
        });

        userSessions.entrySet().removeIf(entry -> entry.getValue().isEmpty());
    }

    private void broadcastUsers() throws Exception {

        List<Long> users = new ArrayList<>(userSessions.keySet());

        Map<String, Object> message = new HashMap<>();
        message.put("type", "USERS");
        message.put("users", users);

        String json = objectMapper.writeValueAsString(message);

        for (List<WebSocketSession> sessions : userSessions.values()) {
            for (WebSocketSession session : sessions) {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(json));
                }
            }
        }
    }
    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
}