// app/src/main/java/com/example/newtrade/models/Message.java
package com.example.newtrade.models;

import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String messageText;
    private String messageType;
    private String messageStatus;
    private String createdAt;
    private boolean isRead;

    // Constructor
    public Message() {}

    public Message(Long id, Long conversationId, Long senderId, String messageText,
                   String messageType, String messageStatus, String createdAt, boolean isRead) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.messageText = messageText;
        this.messageType = messageType;
        this.messageStatus = messageStatus;
        this.createdAt = createdAt;
        this.isRead = isRead;
    }

    // Factory method from Map
    public static Message fromMap(Map<String, Object> messageData) {
        Message message = new Message();

        try {
            message.id = ((Number) messageData.get("id")).longValue();
            message.conversationId = ((Number) messageData.get("conversationId")).longValue();
            message.senderId = ((Number) messageData.get("senderId")).longValue();
            message.messageText = (String) messageData.get("messageText");
            message.messageType = (String) messageData.get("messageType");
            message.messageStatus = (String) messageData.get("messageStatus");
            message.createdAt = (String) messageData.get("createdAt");
            message.isRead = Boolean.TRUE.equals(messageData.get("isRead"));
        } catch (Exception e) {
            // Handle parsing errors
        }

        return message;
    }

    // Factory method from JsonObject
    public static Message fromJsonObject(JsonObject data) {
        Message message = new Message();

        try {
            if (data.has("id")) message.id = data.get("id").getAsLong();
            if (data.has("conversationId")) message.conversationId = data.get("conversationId").getAsLong();
            if (data.has("senderId")) message.senderId = data.get("senderId").getAsLong();
            if (data.has("messageText")) message.messageText = data.get("messageText").getAsString();
            if (data.has("messageType")) message.messageType = data.get("messageType").getAsString();
            if (data.has("messageStatus")) message.messageStatus = data.get("messageStatus").getAsString();
            if (data.has("createdAt")) message.createdAt = data.get("createdAt").getAsString();
            if (data.has("isRead")) message.isRead = data.get("isRead").getAsBoolean();
        } catch (Exception e) {
            // Handle parsing errors
        }

        return message;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getConversationId() {
        return conversationId;
    }

    public void setConversationId(Long conversationId) {
        this.conversationId = conversationId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getMessageText() {
        return messageText;
    }

    public void setMessageText(String messageText) {
        this.messageText = messageText;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(String messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    // Utility methods
    public String getFormattedTime() {
        if (createdAt == null) return "";

        try {
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            SimpleDateFormat outputFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);
            return date != null ? outputFormat.format(date) : "";
        } catch (Exception e) {
            return createdAt;
        }
    }

    public boolean isTextMessage() {
        return "TEXT".equals(messageType);
    }

    public boolean isImageMessage() {
        return "IMAGE".equals(messageType);
    }

    public boolean isOfferMessage() {
        return "OFFER".equals(messageType);
    }

    public boolean isSent() {
        return "SENT".equals(messageStatus);
    }

    public boolean isDelivered() {
        return "DELIVERED".equals(messageStatus);
    }

    public boolean isMessageRead() {
        return "READ".equals(messageStatus);
    }
}