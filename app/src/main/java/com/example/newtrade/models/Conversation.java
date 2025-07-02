// app/src/main/java/com/example/newtrade/models/Conversation.java
package com.example.newtrade.models;

public class Conversation {
    private Long id;
    private Long productId;
    private String productTitle;
    private String productImageUrl;
    private Long buyerId;
    private String buyerName;
    private String buyerAvatar;
    private Long sellerId;
    private String sellerName;
    private String sellerAvatar;
    private String lastMessage;
    private String lastMessageTime;
    private String lastMessageSender;
    private Integer unreadCount;
    private boolean isActive;
    private String createdAt;
    private String updatedAt;

    // For display purposes
    private String otherUserName;
    private String otherUserAvatar;
    private Long otherUserId;

    // Constructors
    public Conversation() {}

    // ===== GETTERS AND SETTERS =====

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductTitle() {
        return productTitle;
    }

    public void setProductTitle(String productTitle) {
        this.productTitle = productTitle;
    }

    public String getProductImageUrl() {
        return productImageUrl;
    }

    public void setProductImageUrl(String productImageUrl) {
        this.productImageUrl = productImageUrl;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public String getBuyerName() {
        return buyerName;
    }

    public void setBuyerName(String buyerName) {
        this.buyerName = buyerName;
    }

    public String getBuyerAvatar() {
        return buyerAvatar;
    }

    public void setBuyerAvatar(String buyerAvatar) {
        this.buyerAvatar = buyerAvatar;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerName() {
        return sellerName;
    }

    public void setSellerName(String sellerName) {
        this.sellerName = sellerName;
    }

    public String getSellerAvatar() {
        return sellerAvatar;
    }

    public void setSellerAvatar(String sellerAvatar) {
        this.sellerAvatar = sellerAvatar;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public String getLastMessageSender() {
        return lastMessageSender;
    }

    public void setLastMessageSender(String lastMessageSender) {
        this.lastMessageSender = lastMessageSender;
    }

    public Integer getUnreadCount() {
        return unreadCount != null ? unreadCount : 0;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }

    public String getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(String updatedAt) {
        this.updatedAt = updatedAt;
    }

    // ===== DISPLAY HELPER METHODS =====

    public String getOtherUserName() {
        return otherUserName;
    }

    public void setOtherUserName(String otherUserName) {
        this.otherUserName = otherUserName;
    }

    public String getOtherUserAvatar() {
        return otherUserAvatar;
    }

    public void setOtherUserAvatar(String otherUserAvatar) {
        this.otherUserAvatar = otherUserAvatar;
    }

    public Long getOtherUserId() {
        return otherUserId;
    }

    public void setOtherUserId(Long otherUserId) {
        this.otherUserId = otherUserId;
    }

    // ===== UTILITY METHODS =====

    public void setupOtherUserInfo(Long currentUserId) {
        if (currentUserId != null) {
            if (currentUserId.equals(buyerId)) {
                // Current user is buyer, other user is seller
                setOtherUserId(sellerId);
                setOtherUserName(sellerName);
                setOtherUserAvatar(sellerAvatar);
            } else {
                // Current user is seller, other user is buyer
                setOtherUserId(buyerId);
                setOtherUserName(buyerName);
                setOtherUserAvatar(buyerAvatar);
            }
        }
    }

    public boolean hasUnreadMessages() {
        return getUnreadCount() > 0;
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", productId=" + productId +
                ", productTitle='" + productTitle + '\'' +
                ", lastMessage='" + lastMessage + '\'' +
                ", unreadCount=" + unreadCount +
                ", otherUserName='" + otherUserName + '\'' +
                '}';
    }
}