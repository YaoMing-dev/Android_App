// app/src/main/java/com/example/newtrade/models/Message.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Message {

    @SerializedName("id")
    private Long id;

    @SerializedName("conversationId")
    private Long conversationId;

    @SerializedName("messageText")
    private String messageText;

    @SerializedName("sender")
    private User sender;

    @SerializedName("messageType")
    private String messageType; // "TEXT", "IMAGE", "OFFER"

    @SerializedName("isRead")
    private Boolean isRead;

    // ✅ MATCH DATABASE: image_url field
    @SerializedName("imageUrl")
    private String imageUrl;

    // ✅ MATCH DATABASE: message_status field
    @SerializedName("messageStatus")
    private String messageStatus; // "SENT", "DELIVERED", "READ"

    @SerializedName("createdAt")
    private Date createdAt;

    // Constructors
    public Message() {}

    // Getters and Setters (simplified)
    public Long getId() { return id; }
    public String getMessageText() { return messageText; }
    public User getSender() { return sender; }
    public String getMessageType() { return messageType; }
    public Boolean getIsRead() { return isRead != null ? isRead : false; }
    public String getImageUrl() { return imageUrl; }
    public String getMessageStatus() { return messageStatus; }
    public Date getCreatedAt() { return createdAt; }

    // Utility methods
    public boolean isTextMessage() { return "TEXT".equals(messageType); }
    public boolean isImageMessage() { return "IMAGE".equals(messageType); }
    public String getSenderName() { return sender != null ? sender.getDisplayNameOrEmail() : "Unknown"; }
}