// app/src/main/java/com/example/newtrade/websocket/ChatWebSocketManager.java
package com.example.newtrade.websocket;

import android.util.Log;

import com.example.newtrade.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashSet;
import java.util.Set;

public class ChatWebSocketManager {
    private static final String TAG = "ChatWebSocketManager";
    private static ChatWebSocketManager instance;

    private WebSocketClient webSocketClient;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private Long currentUserId;
    private Long currentConversationId;
    private Gson gson = new Gson();

    // Listeners
    private Set<ChatListener> chatListeners = new HashSet<>();

    public interface ChatListener {
        void onConnected();
        void onDisconnected();
        void onMessageReceived(JsonObject message);
        void onTypingIndicator(Long userId, boolean isTyping);
        void onError(String error);
    }

    public static synchronized ChatWebSocketManager getInstance() {
        if (instance == null) {
            instance = new ChatWebSocketManager();
        }
        return instance;
    }

    private ChatWebSocketManager() {}

    public void connect(Long userId) {
        if (isConnected || isConnecting || userId == null || userId <= 0) {
            Log.d(TAG, "Already connected/connecting or invalid userId");
            return;
        }

        this.currentUserId = userId;
        isConnecting = true;

        try {
            // ✅ FIXED: Use correct WebSocket URL from backend
            String wsUrl = Constants.WS_BASE_URL + "/ws"; // ws://10.0.2.2:8080/ws
            URI serverUri = URI.create(wsUrl);

            Log.d(TAG, "🔄 Connecting to: " + wsUrl + " for user: " + userId);

            webSocketClient = new WebSocketClient(serverUri, new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "✅ WebSocket connected");
                    isConnected = true;
                    isConnecting = false;
                    notifyConnected();

                    // ✅ Join conversation if available
                    if (currentConversationId != null) {
                        joinConversation(currentConversationId);
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 Received: " + message);
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "❌ WebSocket closed: " + code + " - " + reason);
                    isConnected = false;
                    isConnecting = false;
                    notifyDisconnected();

                    // Auto reconnect if not manually closed
                    if (code != 1000 && currentUserId != null) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "❌ WebSocket error", ex);
                    isConnected = false;
                    isConnecting = false;
                    notifyError("Connection error: " + ex.getMessage());
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to create WebSocket connection", e);
            isConnecting = false;
            notifyError("Failed to connect: " + e.getMessage());
        }
    }

    private void scheduleReconnect() {
        // Reconnect after 3 seconds
        new Thread(() -> {
            try {
                Thread.sleep(3000);
                if (!isConnected && currentUserId != null) {
                    Log.d(TAG, "🔄 Attempting to reconnect...");
                    connect(currentUserId);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Reconnect interrupted", e);
            }
        }).start();
    }

    public void joinConversation(Long conversationId) {
        this.currentConversationId = conversationId;

        if (!isConnected) {
            Log.w(TAG, "Not connected, will join when connected");
            return;
        }

        // ✅ NEW: Use STOMP format for joining conversation
        JsonObject joinMessage = new JsonObject();
        joinMessage.addProperty("type", "JOIN_CONVERSATION");
        joinMessage.addProperty("conversationId", conversationId);
        joinMessage.addProperty("userId", currentUserId);

        sendStompMessage(Constants.JOIN_CONVERSATION + conversationId, joinMessage.toString());
        Log.d(TAG, "📡 Joined conversation: " + conversationId);
    }

    public void leaveConversation() {
        if (!isConnected || currentConversationId == null) return;

        JsonObject leaveMessage = new JsonObject();
        leaveMessage.addProperty("type", "LEAVE_CONVERSATION");
        leaveMessage.addProperty("conversationId", currentConversationId);
        leaveMessage.addProperty("userId", currentUserId);

        sendStompMessage(Constants.LEAVE_CONVERSATION + currentConversationId, leaveMessage.toString());
        Log.d(TAG, "📡 Left conversation: " + currentConversationId);

        currentConversationId = null;
    }

    public void sendChatMessage(String messageText) {
        if (!isConnected || currentConversationId == null || messageText.trim().isEmpty()) {
            Log.w(TAG, "Cannot send - not connected, no conversation, or empty message");
            return;
        }

        // ✅ NEW: Use backend message format
        JsonObject message = new JsonObject();
        message.addProperty("type", "MESSAGE");
        message.addProperty("senderId", currentUserId);
        message.addProperty("conversationId", currentConversationId);
        message.addProperty("content", messageText.trim());
        message.addProperty("messageType", "TEXT");
        message.addProperty("timestamp", System.currentTimeMillis());

        sendStompMessage(Constants.SEND_MESSAGE, message.toString());
        Log.d(TAG, "📤 Sent message to conversation: " + currentConversationId);
    }

    private void sendStompMessage(String destination, String body) {
        if (webSocketClient != null && isConnected) {
            try {
                // ✅ STOMP message format
                String stompMessage = "SEND\n" +
                        "destination:" + destination + "\n" +
                        "content-type:application/json\n" +
                        "\n" +
                        body + "\0";

                webSocketClient.send(stompMessage);
            } catch (Exception e) {
                Log.e(TAG, "Error sending STOMP message", e);
                notifyError("Failed to send message");
            }
        }
    }

    private void handleIncomingMessage(String rawMessage) {
        try {
            // ✅ Handle STOMP message format
            if (rawMessage.startsWith("MESSAGE")) {
                String[] parts = rawMessage.split("\n\n", 2);
                if (parts.length > 1) {
                    String body = parts[1].replace("\0", "");
                    JsonObject json = gson.fromJson(body, JsonObject.class);

                    String type = json.has("type") ? json.get("type").getAsString() : "";

                    switch (type) {
                        case "MESSAGE":
                            notifyMessageReceived(json);
                            break;
                        case "TYPING":
                            if (json.has("senderId") && json.has("isTyping")) {
                                Long userId = json.get("senderId").getAsLong();
                                boolean isTyping = json.get("isTyping").getAsBoolean();
                                notifyTypingIndicator(userId, isTyping);
                            }
                            break;
                        case "USER_JOINED":
                            Log.d(TAG, "✅ User joined conversation");
                            break;
                        default:
                            Log.d(TAG, "Unknown message type: " + type);
                            break;
                    }
                }
            } else if (rawMessage.startsWith("CONNECTED")) {
                Log.d(TAG, "✅ STOMP connected");
            } else if (rawMessage.startsWith("ERROR")) {
                Log.e(TAG, "❌ STOMP error: " + rawMessage);
                notifyError("STOMP error");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing message: " + rawMessage, e);
        }
    }

    // ===== LISTENER MANAGEMENT =====

    public void addChatListener(ChatListener listener) {
        chatListeners.add(listener);
    }

    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    private void notifyConnected() {
        for (ChatListener listener : new HashSet<>(chatListeners)) {
            try {
                listener.onConnected();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyDisconnected() {
        for (ChatListener listener : new HashSet<>(chatListeners)) {
            try {
                listener.onDisconnected();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyMessageReceived(JsonObject message) {
        for (ChatListener listener : new HashSet<>(chatListeners)) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyTypingIndicator(Long userId, boolean isTyping) {
        for (ChatListener listener : new HashSet<>(chatListeners)) {
            try {
                listener.onTypingIndicator(userId, isTyping);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyError(String error) {
        for (ChatListener listener : new HashSet<>(chatListeners)) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    // ===== PUBLIC METHODS =====

    public void disconnect() {
        if (webSocketClient != null) {
            try {
                leaveConversation(); // Leave current conversation first
                webSocketClient.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing WebSocket", e);
            }
            webSocketClient = null;
        }
        isConnected = false;
        isConnecting = false;
        currentConversationId = null;
        Log.d(TAG, "🔌 Disconnected");
    }

    public boolean isConnected() {
        return isConnected;
    }

    public Long getCurrentConversationId() {
        return currentConversationId;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }
}