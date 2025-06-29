package com.example.newtrade.websocket;

import android.util.Log;
import com.example.newtrade.models.Message;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

public class RealtimeWebSocketService {
    private static final String TAG = "RealtimeWebSocketService";
    private static final String WS_URL = "ws://10.0.2.2:8080/ws"; // Emulator localhost
    private static RealtimeWebSocketService instance;
    private WebSocketClient webSocket;
    private Gson gson = new Gson();
    private boolean isConnected = false;

    // Listeners
    private WebSocketListener webSocketListener;
    private ChatListener chatListener;

    public interface WebSocketListener {
        void onWebSocketConnected();
        void onWebSocketDisconnected();
        void onWebSocketError(String error);
    }

    public interface ChatListener {
        void onMessageReceived(Message message);
        void onTypingIndicator(Long userId, boolean isTyping);
        void onMessageStatusUpdated(Long messageId, String status);
    }

    public interface LocationListener {
        void onLocationUpdate(double latitude, double longitude);
        void onNearbyUsersUpdate(java.util.List<Map<String, Object>> users);
    }

    private RealtimeWebSocketService() {}

    public static synchronized RealtimeWebSocketService getInstance() {
        if (instance == null) {
            instance = new RealtimeWebSocketService();
        }
        return instance;
    }

    public void setWebSocketListener(WebSocketListener listener) {
        this.webSocketListener = listener;
    }

    public void setChatListener(ChatListener listener) {
        this.chatListener = listener;
    }

    public void connect(String token) {
        if (isConnected) {
            Log.d(TAG, "Already connected");
            return;
        }

        try {
            String wsUrl = WS_URL + "?token=" + token;
            URI uri = URI.create(wsUrl);

            webSocket = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    Log.d(TAG, "✅ WebSocket Connected");
                    if (webSocketListener != null) {
                        webSocketListener.onWebSocketConnected();
                    }
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 Received: " + message);
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    Log.d(TAG, "❌ WebSocket Closed: " + reason);
                    if (webSocketListener != null) {
                        webSocketListener.onWebSocketDisconnected();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    isConnected = false;
                    Log.e(TAG, "⚠️ WebSocket Error", ex);
                    if (webSocketListener != null) {
                        webSocketListener.onWebSocketError(ex.getMessage());
                    }
                }
            };

            webSocket.connect();
            Log.d(TAG, "🔄 Connecting to WebSocket...");

        } catch (Exception e) {
            Log.e(TAG, "❌ WebSocket connection error", e);
            if (webSocketListener != null) {
                webSocketListener.onWebSocketError(e.getMessage());
            }
        }
    }

    public void disconnect() {
        if (webSocket != null) {
            webSocket.close();
            webSocket = null;
        }
        isConnected = false;
    }

    public boolean isConnected() {
        return isConnected && webSocket != null && webSocket.isOpen();
    }

    public void sendMessage(Long conversationId, String content, String messageType) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot send message - WebSocket not connected");
            return;
        }

        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("type", "message");
            messageData.put("conversation_id", conversationId);
            messageData.put("content", content);
            messageData.put("message_type", messageType);
            messageData.put("timestamp", System.currentTimeMillis());

            String json = gson.toJson(messageData);
            webSocket.send(json);
            Log.d(TAG, "✅ Message sent: " + content);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending message", e);
        }
    }

    public void sendLocationMessage(Long conversationId, double latitude, double longitude, String address) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot send location - WebSocket not connected");
            return;
        }

        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("type", "location");
            locationData.put("conversation_id", conversationId);
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("address", address);
            locationData.put("timestamp", System.currentTimeMillis());

            String json = gson.toJson(locationData);
            webSocket.send(json);
            Log.d(TAG, "✅ Location sent: " + address);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending location", e);
        }
    }

    public void sendTypingIndicator(Long conversationId, boolean isTyping) {
        if (!isConnected()) return;

        try {
            Map<String, Object> typingData = new HashMap<>();
            typingData.put("type", "typing");
            typingData.put("conversation_id", conversationId);
            typingData.put("is_typing", isTyping);

            String json = gson.toJson(typingData);
            webSocket.send(json);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending typing indicator", e);
        }
    }

    public void joinConversation(Long conversationId) {
        if (!isConnected()) return;

        try {
            Map<String, Object> joinData = new HashMap<>();
            joinData.put("type", "join_conversation");
            joinData.put("conversation_id", conversationId);

            String json = gson.toJson(joinData);
            webSocket.send(json);
            Log.d(TAG, "✅ Joined conversation: " + conversationId);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error joining conversation", e);
        }
    }

    public void leaveConversation(Long conversationId) {
        if (!isConnected()) return;

        try {
            Map<String, Object> leaveData = new HashMap<>();
            leaveData.put("type", "leave_conversation");
            leaveData.put("conversation_id", conversationId);

            String json = gson.toJson(leaveData);
            webSocket.send(json);
            Log.d(TAG, "✅ Left conversation: " + conversationId);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error leaving conversation", e);
        }
    }

    public void reconnect() {
        if (webSocket != null && !isConnected) {
            connect(null);
        }
    }

    public void cleanup() {
        disconnect();
        webSocketListener = null;
        chatListener = null;
    }

    public void addLocationListener(LocationListener listener) {
        this.locationListener = listener;
    }

    public void removeLocationListener(LocationListener listener) {
        if (this.locationListener == listener) {
            this.locationListener = null;
        }
    }

    public void sendLocationUpdate(double latitude, double longitude) {
        if (isConnected && webSocket != null) {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("type", "location_update");
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);

            String message = gson.toJson(locationData);
            webSocket.send(message);
            Log.d(TAG, "Location update sent: " + latitude + ", " + longitude);
        }
    }

    public void requestNearbyUsers(double latitude, double longitude, double radius) {
        if (isConnected && webSocket != null) {
            Map<String, Object> requestData = new HashMap<>();
            requestData.put("type", "request_nearby_users");
            requestData.put("latitude", latitude);
            requestData.put("longitude", longitude);
            requestData.put("radius", radius);

            String message = gson.toJson(requestData);
            webSocket.send(message);
            Log.d(TAG, "Nearby users request sent");
        }
    }

    private void handleIncomingMessage(String messageJson) {
        try {
            JsonObject jsonObject = JsonParser.parseString(messageJson).getAsJsonObject();
            String type = jsonObject.get("type").getAsString();

            switch (type) {
                case "message":
                case "location":
                    handleChatMessage(jsonObject);
                    break;
                case "typing":
                    handleTypingIndicator(jsonObject);
                    break;
                case "message_status":
                    handleMessageStatus(jsonObject);
                    break;
                default:
                    Log.d(TAG, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling incoming message", e);
        }
    }

    private void handleChatMessage(JsonObject jsonObject) {
        if (chatListener == null) return;

        try {
            Message message = gson.fromJson(jsonObject, Message.class);
            chatListener.onMessageReceived(message);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing chat message", e);
        }
    }

    private void handleTypingIndicator(JsonObject jsonObject) {
        if (chatListener == null) return;

        try {
            Long userId = jsonObject.get("user_id").getAsLong();
            boolean isTyping = jsonObject.get("is_typing").getAsBoolean();
            chatListener.onTypingIndicator(userId, isTyping);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing typing indicator", e);
        }
    }

    private void handleMessageStatus(JsonObject jsonObject) {
        if (chatListener == null) return;

        try {
            Long messageId = jsonObject.get("message_id").getAsLong();
            String status = jsonObject.get("status").getAsString();
            chatListener.onMessageStatusUpdated(messageId, status);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing message status", e);
        }
    }
}
