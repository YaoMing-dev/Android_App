// app/src/main/java/com/example/newtrade/models/Message.java
// ✅ FIXED - Add messageType as String
package com.example.newtrade.models;

public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String senderName;
    private String content;
    private String messageType; // ✅ FIX: Change to String instead of enum
    private String timestamp;
    private boolean isRead;

    // Constructors
    public Message() {}

    public Message(Long conversationId, Long senderId, String content) {
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = "TEXT"; // Default type
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
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }

    public boolean isRead() { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    // Helper methods
    public boolean isFromCurrentUser(Long currentUserId) {
        return senderId != null && senderId.equals(currentUserId);
    }

    public boolean isTextMessage() {
        return "TEXT".equals(messageType);
    }

    public boolean isImageMessage() {
        return "IMAGE".equals(messageType);
    }
}