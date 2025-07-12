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

    // ✅ NEW: Promotion-specific fields - FR-4.2.1
    @SerializedName("promotionType")
    private String promotionType;

    @SerializedName("promoCode")
    private String promoCode;

    @SerializedName("imageUrl")
    private String imageUrl;

    @SerializedName("actionUrl")
    private String actionUrl;

    // ✅ UPDATED: Enum with all notification types including promotions - FR-4.2.1
    public enum NotificationType {
        @SerializedName("MESSAGE")
        MESSAGE,    // New messages - FR-4.2.1 ✅

        @SerializedName("OFFER")
        OFFER,      // Price offers - FR-4.2.1 ✅

        @SerializedName("TRANSACTION")
        TRANSACTION,// Transaction updates

        @SerializedName("REVIEW")
        REVIEW,     // Reviews

        @SerializedName("GENERAL")
        GENERAL,    // General notifications

        @SerializedName("PROMOTION")
        PROMOTION,  // Promotions - FR-4.2.1 ✅

        @SerializedName("LISTING_UPDATE")
        LISTING_UPDATE, // Listing updates - FR-4.2.1 ✅

        @SerializedName("SYSTEM_ALERT")
        SYSTEM_ALERT,   // System alerts

        @SerializedName("REMINDER")
        REMINDER        // Reminders
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

    // ✅ NEW: Constructor with promotion fields
    public NotificationResponse(Long id, String title, String message, NotificationType type,
                                Long referenceId, Boolean isRead, String createdAt,
                                String promotionType, String promoCode, String imageUrl) {
        this(id, title, message, type, referenceId, isRead, createdAt);
        this.promotionType = promotionType;
        this.promoCode = promoCode;
        this.imageUrl = imageUrl;
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

    // ✅ NEW: Promotion field getters and setters
    public String getPromotionType() { return promotionType; }
    public void setPromotionType(String promotionType) { this.promotionType = promotionType; }

    public String getPromoCode() { return promoCode; }
    public void setPromoCode(String promoCode) { this.promoCode = promoCode; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public String getActionUrl() { return actionUrl; }
    public void setActionUrl(String actionUrl) { this.actionUrl = actionUrl; }

    // Utility methods
    public boolean isUnread() {
        return isRead == null || !isRead;
    }

    // ✅ UPDATED: Enhanced time formatting
    public String getFormattedTime() {
        if (createdAt == null || createdAt.isEmpty()) {
            return "";
        }

        try {
            // Parse ISO format from backend: "2024-01-15T10:30:00"
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault());
            Date date = inputFormat.parse(createdAt.trim());

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

    // ✅ UPDATED: Enhanced type display names with promotions
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
            case PROMOTION:             // ✅ NEW
                return "Promotion";     // ✅ NEW
            case LISTING_UPDATE:        // ✅ NEW
                return "Listing Update"; // ✅ NEW
            case SYSTEM_ALERT:
                return "System Alert";
            case REMINDER:
                return "Reminder";
            default:
                return "Notification";
        }
    }

    // ✅ NEW: Get promotion display text
    public String getPromotionDisplayText() {
        if (type != NotificationType.PROMOTION) {
            return "";
        }

        StringBuilder display = new StringBuilder();

        if (promotionType != null) {
            switch (promotionType.toUpperCase()) {
                case "DISCOUNT":
                    display.append("💰 Discount");
                    break;
                case "FLASH_SALE":
                    display.append("⚡ Flash Sale");
                    break;
                case "NEW_FEATURE":
                    display.append("🆕 New Feature");
                    break;
                case "LOCATION":
                    display.append("📍 Local Deal");
                    break;
                default:
                    display.append("🎁 Special Offer");
                    break;
            }
        }

        if (promoCode != null && !promoCode.isEmpty()) {
            display.append(" • Code: ").append(promoCode);
        }

        return display.toString();
    }

    // ✅ NEW: Check if notification has promotion code
    public boolean hasPromoCode() {
        return promoCode != null && !promoCode.isEmpty();
    }

    // ✅ NEW: Check if notification is promotion
    public boolean isPromotion() {
        return type == NotificationType.PROMOTION;
    }

    // ✅ NEW: Check if notification is listing update
    public boolean isListingUpdate() {
        return type == NotificationType.LISTING_UPDATE;
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
                ", promotionType='" + promotionType + '\'' +
                ", promoCode='" + promoCode + '\'' +
                '}';
    }
}