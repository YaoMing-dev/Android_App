package com.example.newtrade.models;

public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String messageText; // Compatibility with backend
    private String messageType;
    private Boolean isRead;
    private String createdAt;
    private String timestamp;

    // Constructors
    public Message() {}

    public Message(Long conversationId, Long senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageText = content; // For backend compatibility
        this.isRead = false;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getContent() { return content; }
    public void setContent(String content) {
        this.content = content;
        this.messageText = content; // Keep in sync
    }

    public String getMessageText() { return messageText; }
    public void setMessageText(String messageText) {
        this.messageText = messageText;
        this.content = messageText; // Keep in sync
    }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
        this.timestamp = createdAt; // Keep in sync
    }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
        this.createdAt = timestamp; // Keep in sync
    }

    // Helper methods
    public boolean isFromCurrentUser(Long currentUserId) {
        return senderId != null && senderId.equals(currentUserId);
    }

    public String getDisplayTime() {
        return timestamp != null ? timestamp : createdAt;
    }
}