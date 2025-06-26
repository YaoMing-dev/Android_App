// app/src/main/java/com/example/newtrade/websocket/WebSocketManager.java
package com.example.newtrade.websocket;

import android.util.Log;
import com.example.newtrade.models.Message;
import com.example.newtrade.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static WebSocketManager instance;

    private WebSocketClient webSocketClient;
    private WebSocketListener listener;
    private Gson gson = new Gson();
    private boolean isConnected = false;
    private Long currentUserId;

    public interface WebSocketListener {
        void onMessageReceived(Message message);
        void onTyping(Long userId, boolean isTyping);
        void onUserStatusChanged(Long userId, boolean isOnline);
        void onConnectionChanged(boolean connected);
        void onError(String error);
    }

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void connect(Long userId, WebSocketListener listener) {
        this.currentUserId = userId;
        this.listener = listener;

        try {
            String wsUrl = Constants.WS_BASE_URL + "/ws";
            URI serverUri = URI.create(wsUrl);

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshakedata) {
                    Log.d(TAG, "✅ WebSocket connected");
                    isConnected = true;
                    if (listener != null) {
                        listener.onConnectionChanged(true);
                    }

                    // Subscribe to user's conversations
                    subscribeToUserChannels();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 WebSocket message: " + message);
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "❌ WebSocket closed: " + reason);
                    isConnected = false;
                    if (listener != null) {
                        listener.onConnectionChanged(false);
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "❌ WebSocket error", ex);
                    isConnected = false;
                    if (listener != null) {
                        listener.onError(ex.getMessage());
                    }
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Error connecting to WebSocket", e);
            if (listener != null) {
                listener.onError("Connection failed: " + e.getMessage());
            }
        }
    }

    private void subscribeToUserChannels() {
        if (currentUserId != null) {
            // Subscribe to user's personal channel
            JsonObject subscribe = new JsonObject();
            subscribe.addProperty("action", "subscribe");
            subscribe.addProperty("channel", "/topic/user/" + currentUserId);
            sendMessage(subscribe.toString());
        }
    }

    public void subscribeToConversation(Long conversationId) {
        if (isConnected && conversationId != null) {
            JsonObject subscribe = new JsonObject();
            subscribe.addProperty("action", "subscribe");
            subscribe.addProperty("channel", "/topic/conversation/" + conversationId);
            sendMessage(subscribe.toString());

            Log.d(TAG, "📡 Subscribed to conversation: " + conversationId);
        }
    }

    public void sendChatMessage(Long conversationId, String messageText) {
        if (!isConnected || conversationId == null || currentUserId == null) {
            Log.w(TAG, "⚠️ Cannot send message - not connected or missing data");
            return;
        }

        JsonObject messageObj = new JsonObject();
        messageObj.addProperty("action", "send_message");
        messageObj.addProperty("conversationId", conversationId);
        messageObj.addProperty("senderId", currentUserId);
        messageObj.addProperty("content", messageText);
        messageObj.addProperty("type", "TEXT");

        sendMessage(messageObj.toString());
        Log.d(TAG, "📤 Sent chat message to conversation: " + conversationId);
    }

    public void sendTypingIndicator(Long conversationId, boolean isTyping) {
        if (!isConnected || conversationId == null || currentUserId == null) return;

        JsonObject typing = new JsonObject();
        typing.addProperty("action", "typing");
        typing.addProperty("conversationId", conversationId);
        typing.addProperty("senderId", currentUserId);
        typing.addProperty("isTyping", isTyping);

        sendMessage(typing.toString());
    }

    private void sendMessage(String message) {
        if (webSocketClient != null && isConnected) {
            webSocketClient.send(message);
        }
    }

    private void handleIncomingMessage(String rawMessage) {
        try {
            JsonObject json = gson.fromJson(rawMessage, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type) {
                case "NEW_MESSAGE":
                    handleNewMessage(json);
                    break;
                case "TYPING":
                    handleTypingIndicator(json);
                    break;
                case "USER_STATUS":
                    handleUserStatus(json);
                    break;
                default:
                    Log.d(TAG, "⚠️ Unknown message type: " + type);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing WebSocket message", e);
        }
    }

    private void handleNewMessage(JsonObject json) {
        try {
            Message message = gson.fromJson(json, Message.class);
            if (listener != null) {
                listener.onMessageReceived(message);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing new message", e);
        }
    }

    private void handleTypingIndicator(JsonObject json) {
        try {
            Long userId = json.get("senderId").getAsLong();
            boolean isTyping = json.get("isTyping").getAsBoolean();

            if (listener != null) {
                listener.onTyping(userId, isTyping);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing typing indicator", e);
        }
    }

    private void handleUserStatus(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            boolean isOnline = json.get("isOnline").getAsBoolean();

            if (listener != null) {
                listener.onUserStatusChanged(userId, isOnline);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing user status", e);
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        isConnected = false;
        currentUserId = null;
        listener = null;
        Log.d(TAG, "🔌 WebSocket disconnected");
    }

    public boolean isConnected() {
        return isConnected;
    }
}