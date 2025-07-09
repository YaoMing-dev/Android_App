// app/src/main/java/com/example/newtrade/models/NotificationResponse.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class NotificationResponse {

    @SerializedName("id")
    private Long id;

    @SerializedName("title")
    private String title;

    @SerializedName("message")
    private String message;

    @SerializedName("type")
    private NotificationType type;

    @SerializedName("referenceId")
    private Long referenceId;

    @SerializedName("isRead")
    private Boolean isRead;

    @SerializedName("createdAt")
    private String createdAt;

    // Enum cho notification types theo backend
    public enum NotificationType {
        @SerializedName("MESSAGE")
        MESSAGE,    // New messages

        @SerializedName("OFFER")
        OFFER,      // Price offers

        @SerializedName("TRANSACTION")
        TRANSACTION,// Listing updates

        @SerializedName("REVIEW")
        REVIEW,     // Reviews

        @SerializedName("GENERAL")
        GENERAL     // Promotions
    }

    // Constructors
    public NotificationResponse() {}

    public NotificationResponse(Long id, String title, String message, NotificationType type,
                                Long referenceId, Boolean isRead, String createdAt) {
        this.id = id;
        this.title = title;
        this.message = message;
        this.type = type;
        this.referenceId = referenceId;
        this.isRead = isRead;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public NotificationType getType() { return type; }
    public void setType(NotificationType type) { this.type = type; }

    public Long getReferenceId() { return referenceId; }
    public void setReferenceId(Long referenceId) { this.referenceId = referenceId; }

    public Boolean getIsRead() { return isRead; }
    public void setIsRead(Boolean isRead) { this.isRead = isRead; }

    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    // Utility methods
    public boolean isUnread() {
        return isRead == null || !isRead;
    }

    public String getFormattedTime() {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }

        try {
            // Parse ISO format from backend: "2024-01-15T10:30:00"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(createdAt);

            if (date != null) {
                long diffInMillis = System.currentTimeMillis() - date.getTime();
                long diffInHours = diffInMillis / (60 * 60 * 1000);
                long diffInDays = diffInHours / 24;

                if (diffInHours < 1) {
                    long diffInMinutes = diffInMillis / (60 * 1000);
                    return diffInMinutes <= 1 ? "Just now" : diffInMinutes + " minutes ago";
                } else if (diffInHours < 24) {
                    return diffInHours + " hours ago";
                } else if (diffInDays < 7) {
                    return diffInDays + " days ago";
                } else {
                    SimpleDateFormat outputFormat = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
                    return outputFormat.format(date);
                }
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return createdAt;
    }

    public String getTypeDisplayName() {
        if (type == null) return "Notification";

        switch (type) {
            case MESSAGE:
                return "New Message";
            case OFFER:
                return "Price Offer";
            case TRANSACTION:
                return "Transaction Update";
            case REVIEW:
                return "New Review";
            case GENERAL:
                return "General";
            default:
                return "Notification";
        }
    }

    @Override
    public String toString() {
        return "NotificationResponse{" +
                "id=" + id +
                ", title='" + title + '\'' +
                ", message='" + message + '\'' +
                ", type=" + type +
                ", referenceId=" + referenceId +
                ", isRead=" + isRead +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}