// app/src/main/java/com/example/newtrade/models/Conversation.java
package com.example.newtrade.models;

import com.google.gson.annotations.SerializedName;
import java.util.Date;

public class Conversation {

    @SerializedName("id")
    private Long id;

    @SerializedName("product")
    private Product product;

    @SerializedName("buyer")
    private User buyer;

    @SerializedName("seller")
    private User seller;

    @SerializedName("lastMessage")
    private Message lastMessage;

    @SerializedName("lastMessagePreview")
    private String lastMessagePreview;

    @SerializedName("unreadCount")
    private Integer unreadCount;

    @SerializedName("isActive")
    private Boolean isActive;

    @SerializedName("createdAt")
    private Date createdAt;

    @SerializedName("updatedAt")
    private Date updatedAt;

    // Constructors
    public Conversation() {}

    public Conversation(Product product, User buyer, User seller) {
        this.product = product;
        this.buyer = buyer;
        this.seller = seller;
        this.isActive = true;
        this.unreadCount = 0;
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Product getProduct() {
        return product;
    }

    public void setProduct(Product product) {
        this.product = product;
    }

    public User getBuyer() {
        return buyer;
    }

    public void setBuyer(User buyer) {
        this.buyer = buyer;
    }

    public User getSeller() {
        return seller;
    }

    public void setSeller(User seller) {
        this.seller = seller;
    }

    public Message getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(Message lastMessage) {
        this.lastMessage = lastMessage;
    }

    public String getLastMessagePreview() {
        return lastMessagePreview;
    }

    public void setLastMessagePreview(String lastMessagePreview) {
        this.lastMessagePreview = lastMessagePreview;
    }

    public Integer getUnreadCount() {
        return unreadCount != null ? unreadCount : 0;
    }

    public void setUnreadCount(Integer unreadCount) {
        this.unreadCount = unreadCount;
    }

    public Boolean getIsActive() {
        return isActive != null ? isActive : true;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Utility methods
    public User getOtherUser(Long currentUserId) {
        if (buyer != null && buyer.getId().equals(currentUserId)) {
            return seller;
        } else if (seller != null && seller.getId().equals(currentUserId)) {
            return buyer;
        }
        return null;
    }

    public String getOtherUserName(Long currentUserId) {
        User otherUser = getOtherUser(currentUserId);
        return otherUser != null ? otherUser.getDisplayNameOrEmail() : "Unknown User";
    }

    public String getProductTitle() {
        return product != null ? product.getTitle() : "Product not available";
    }

    public String getProductImageUrl() {
        return product != null ? product.getFirstImageUrl() : null;
    }

    public boolean hasUnreadMessages() {
        return getUnreadCount() > 0;
    }

    public String getLastMessageText() {
        if (lastMessagePreview != null && !lastMessagePreview.isEmpty()) {
            return lastMessagePreview;
        } else if (lastMessage != null) {
            return lastMessage.getMessageText();
        }
        return "No messages yet";
    }

    @Override
    public String toString() {
        return "Conversation{" +
                "id=" + id +
                ", product=" + getProductTitle() +
                ", buyer=" + (buyer != null ? buyer.getDisplayName() : "null") +
                ", seller=" + (seller != null ? seller.getDisplayName() : "null") +
                ", unreadCount=" + unreadCount +
                '}';
    }
}