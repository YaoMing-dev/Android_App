// app/src/main/java/com/example/newtrade/models/Message.java
package com.example.newtrade.models;

import java.time.LocalDateTime;

public class Message {
    private Long id;
    private Long conversationId;
    private Long senderId;
    private String content;
    private String messageType;
    private String createdAt;
    private String timestamp;
    private boolean isRead;
    private String imageUrl; // ✅ NEW - For image messages
    private String senderName; // ✅ NEW - For display

    // Constructors
    public Message() {}

    public Message(Long id, Long conversationId, Long senderId, String content, String messageType, String createdAt) {
        this.id = id;
        this.conversationId = conversationId;
        this.senderId = senderId;
        this.content = content;
        this.messageType = messageType;
        this.createdAt = createdAt;
        this.isRead = false;
    }

    // ===== GETTERS AND SETTERS =====

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

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getMessageType() {
        return messageType;
    }

    public void setMessageType(String messageType) {
        this.messageType = messageType;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public void setIsRead(boolean isRead) {
        this.isRead = isRead;
    }

    public boolean getIsRead() {
        return isRead;
    }

    // ✅ NEW: Image message support
    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    // ✅ NEW: Sender name for display
    public String getSenderName() {
        return senderName;
    }

    public void setSenderName(String senderName) {
        this.senderName = senderName;
    }

    // ===== UTILITY METHODS =====

    public boolean isImageMessage() {
        return "IMAGE".equals(messageType) && imageUrl != null && !imageUrl.isEmpty();
    }

    public boolean isTextMessage() {
        return "TEXT".equals(messageType);
    }

    @Override
    public String toString() {
        return "Message{" +
                "id=" + id +
                ", conversationId=" + conversationId +
                ", senderId=" + senderId +
                ", content='" + content + '\'' +
                ", messageType='" + messageType + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", isRead=" + isRead +
                ", imageUrl='" + imageUrl + '\'' +
                ", senderName='" + senderName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Message message = (Message) o;
        return id != null && id.equals(message.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : 0;
    }
}