package com.photography.agent.model;

import java.time.LocalDateTime;

public class ChatResponse {

    private String message;
    private String sessionId;
    private LocalDateTime timestamp;

    public ChatResponse() {}

    public ChatResponse(String message, String sessionId) {
        this.message = message;
        this.sessionId = sessionId;
        this.timestamp = LocalDateTime.now();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getSessionId() {
        return sessionId;
    }

    public void setSessionId(String sessionId) {
        this.sessionId = sessionId;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
