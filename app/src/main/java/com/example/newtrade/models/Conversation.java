// File: app/src/main/java/com/example/newtrade/models/Conversation.java
package com.example.newtrade.models;

public class Conversation {
    private Long id;
    private String otherUserName;
    private String lastMessage;
    private String lastMessageTime;
    private String otherUserAvatar;
    private boolean hasUnreadMessages;
    private boolean isOnline;

    // Constructors
    public Conversation() {}

    public Conversation(Long id, String otherUserName, String lastMessage,
                        String lastMessageTime, String otherUserAvatar,
                        boolean hasUnreadMessages, boolean isOnline) {
        this.id = id;
        this.otherUserName = otherUserName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.otherUserAvatar = otherUserAvatar;
        this.hasUnreadMessages = hasUnreadMessages;
        this.isOnline = isOnline;
    }

    // Getters and Setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getOtherUserName() { return otherUserName; }
    public void setOtherUserName(String otherUserName) { this.otherUserName = otherUserName; }

    public String getLastMessage() { return lastMessage; }
    public void setLastMessage(String lastMessage) { this.lastMessage = lastMessage; }

    public String getLastMessageTime() { return lastMessageTime; }
    public void setLastMessageTime(String lastMessageTime) { this.lastMessageTime = lastMessageTime; }

    public String getOtherUserAvatar() { return otherUserAvatar; }
    public void setOtherUserAvatar(String otherUserAvatar) { this.otherUserAvatar = otherUserAvatar; }

    public boolean isHasUnreadMessages() { return hasUnreadMessages; }
    public void setHasUnreadMessages(boolean hasUnreadMessages) { this.hasUnreadMessages = hasUnreadMessages; }

    public boolean isOnline() { return isOnline; }
    public void setOnline(boolean online) { isOnline = online; }
}