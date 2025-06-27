// ✅ FIXED: Thêm missing methods
package com.example.newtrade.models;

public class Conversation {
    private Long id;
    private String lastMessage;
    private Long productId;
    private Long buyerId;
    private Long sellerId;

    // ✅ Thêm các field mới cần thiết
    private String otherUserName;
    private String lastMessageTime;
    private String productTitle;
    private int unreadCount;

    public Conversation() {}

    // Getters and setters hiện tại
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public Long getProductId() { return productId; }
    public void setProductId(Long productId) { this.productId = productId; }

    public Long getBuyerId() { return buyerId; }
    public void setBuyerId(Long buyerId) { this.buyerId = buyerId; }

    public Long getSellerId() { return sellerId; }
    public void setSellerId(Long sellerId) { this.sellerId = sellerId; }

    // ✅ Thêm các method mới bị thiếu
    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getProductTitle() { return productTitle; }
    public int getUnreadCount() { return unreadCount; }
    public void setUnreadCount(int unreadCount) { this.unreadCount = unreadCount; }
    public void setProductTitle(String productTitle) { this.productTitle = productTitle; }
}