package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;

public class Message {
    @SerializedName("id")
    private Long id;

    @SerializedName("conversation_id")
    private Long conversationId;

    @SerializedName("sender_id")
    private Long senderId;

    @SerializedName("content")
    private String content;

    @SerializedName("message_type")
    private String messageType; // text, image, location

    @SerializedName("status")
    private String status; // sent, delivered, read

    @SerializedName("created_at")
    private Long createdAt;

    @SerializedName("sender_name")
    private String senderName;

    @SerializedName("sender_avatar")
    private String senderAvatar;

    // Location data for GPS sharing
    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("location_name")
    private String locationName;

    // Constructors
    public Message() {}

    public Message(String content, Long senderId, Long conversationId) {
        this.content = content;
        this.senderId = senderId;
        this.conversationId = conversationId;
        this.messageType = "text";
        this.status = "sent";
        this.createdAt = System.currentTimeMillis();
    }

    // Static method to create location message
    public static Message createLocationMessage(Long senderId, Long conversationId,
                                              double latitude, double longitude, String locationName) {
        Message message = new Message();
        message.setSenderId(senderId);
        message.setConversationId(conversationId);
        message.setMessageType("location");
        message.setLatitude(latitude);
        message.setLongitude(longitude);
        message.setLocationName(locationName);
        message.setContent("📍 Shared location");
        message.setStatus("sent");
        message.setCreatedAt(System.currentTimeMillis());
        return message;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getSenderId() { return senderId; }
    public void setSenderId(Long senderId) { this.senderId = senderId; }

    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public Long getCreatedAt() { return createdAt; }
    public void setCreatedAt(Long createdAt) { this.createdAt = createdAt; }

    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getSenderAvatar() { return senderAvatar; }
    public void setSenderAvatar(String senderAvatar) { this.senderAvatar = senderAvatar; }

    public Double getLatitude() { return latitude; }
    public void setLatitude(Double latitude) { this.latitude = latitude; }

    public Double getLongitude() { return longitude; }
    public void setLongitude(Double longitude) { this.longitude = longitude; }

    public String getLocationName() { return locationName; }
    public void setLocationName(String locationName) { this.locationName = locationName; }

    // Helper methods
    public boolean isLocationMessage() {
        return "location".equals(messageType);
    }

    public boolean isImageMessage() {
        return "image".equals(messageType);
    }

    public boolean isTextMessage() {
        return "text".equals(messageType) || messageType == null;
    }
}
