// app/src/main/java/com/example/newtrade/websocket/RealtimeWebSocketService.java
package com.example.newtrade.websocket;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.newtrade.models.Message;
import com.example.newtrade.utils.Constants;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RealtimeWebSocketService {
    private static final String TAG = "RealtimeWebSocketService";

    // ✅ FIX: Singleton instance
    private static RealtimeWebSocketService instance;
    private static final Object lock = new Object();

    // WebSocket connection
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private Long currentUserId;

    // STOMP protocol
    private boolean isStompConnected = false;
    private static final String STOMP_CONNECT = "CONNECT";
    private static final String STOMP_CONNECTED = "CONNECTED";
    private static final String STOMP_SUBSCRIBE = "SUBSCRIBE";
    private static final String STOMP_MESSAGE = "MESSAGE";
    private static final String STOMP_ERROR = "ERROR";

    // Reconnection
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY = 3000; // 3 seconds
    private int reconnectAttempts = 0;
    private Handler reconnectHandler = new Handler(Looper.getMainLooper());

    // Listeners
    private List<WebSocketListener> listeners = new ArrayList<>();
    private List<ChatListener> chatListeners = new ArrayList<>();
    private List<LocationListener> locationListeners = new ArrayList<>();

    // Gson for JSON parsing
    private Gson gson = new Gson();

    // ✅ FIX: Private constructor for Singleton
    private RealtimeWebSocketService() {
        // Private constructor
    }

    // ✅ FIX: getInstance method
    public static RealtimeWebSocketService getInstance() {
        if (instance == null) {
            synchronized (lock) {
                if (instance == null) {
                    instance = new RealtimeWebSocketService();
                }
            }
        }
        return instance;
    }

    // Interfaces for listeners
    public interface WebSocketListener {
        void onConnectionChanged(boolean connected);
        void onError(String error);
    }

    public interface ChatListener {
        void onMessageReceived(Message message);
        void onTypingIndicator(Long userId, boolean isTyping);
    }

    public interface LocationListener {
        void onLocationUpdate(Long userId, double latitude, double longitude);
        void onNearbyUsersUpdate(Map<Long, Object> nearbyUsers);
    }

    // ✅ FIX: Connect method with proper error handling
    public void connect(Long userId, WebSocketListener listener) {
        this.currentUserId = userId;

        if (listener != null) {
            addListener(listener);
        }

        if (isConnected || isConnecting) {
            Log.d(TAG, "Already connected or connecting");
            return;
        }

        if (userId == null || userId <= 0) {
            Log.w(TAG, "Invalid user ID, skipping WebSocket connection");
            return;
        }

        isConnecting = true;
        reconnectAttempts = 0;

        try {
            String wsUrl = Constants.WS_BASE_URL + "/ws";
            Log.d(TAG, "🔄 Connecting to WebSocket: " + wsUrl);

            URI serverUri = URI.create(wsUrl);

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "✅ WebSocket connected");
                    isConnected = true;
                    isConnecting = false;
                    reconnectAttempts = 0;

                    // Send STOMP CONNECT frame
                    sendStompConnect();

                    // Notify listeners
                    notifyConnectionChanged(true);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 Received: " + message);
                    handleStompMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "❌ WebSocket closed: " + reason + " (Code: " + code + ")");
                    isConnected = false;
                    isConnecting = false;
                    isStompConnected = false;

                    // Notify listeners
                    notifyConnectionChanged(false);

                    // Attempt reconnection if not manually closed
                    if (remote && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "❌ WebSocket error: " + ex.getMessage());
                    isConnecting = false;
                    notifyError("WebSocket error: " + ex.getMessage());
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to connect WebSocket", e);
            isConnecting = false;
            notifyError("Failed to connect: " + e.getMessage());
        }
    }

    // ✅ FIX: Disconnect method
    public void disconnect() {
        try {
            if (webSocketClient != null) {
                webSocketClient.close();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error disconnecting WebSocket", e);
        }

        isConnected = false;
        isConnecting = false;
        isStompConnected = false;
        webSocketClient = null;

        // Clear all listeners
        listeners.clear();
        chatListeners.clear();
        locationListeners.clear();

        Log.d(TAG, "✅ WebSocket disconnected and cleaned up");
    }

    // STOMP protocol methods
    private void sendStompConnect() {
        StringBuilder frame = new StringBuilder();
        frame.append(STOMP_CONNECT).append("\n");
        frame.append("accept-version:1.0,1.1,1.2").append("\n");
        frame.append("heart-beat:10000,10000").append("\n");
        if (currentUserId != null) {
            frame.append("user-id:").append(currentUserId).append("\n");
        }
        frame.append("\n\0");

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(frame.toString());
            Log.d(TAG, "📤 STOMP CONNECT sent");
        }
    }

    private void handleStompMessage(String message) {
        try {
            String[] lines = message.split("\n");
            if (lines.length == 0) return;

            String command = lines[0];
            Map<String, String> headers = new HashMap<>();
            StringBuilder body = new StringBuilder();

            boolean inBody = false;
            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];

                if (line.isEmpty() && !inBody) {
                    inBody = true;
                    continue;
                }

                if (inBody) {
                    body.append(line);
                } else {
                    String[] headerParts = line.split(":", 2);
                    if (headerParts.length == 2) {
                        headers.put(headerParts[0], headerParts[1]);
                    }
                }
            }

            // Handle different STOMP commands
            switch (command) {
                case STOMP_CONNECTED:
                    handleStompConnected();
                    break;
                case STOMP_MESSAGE:
                    handleStompMessageFrame(headers, body.toString());
                    break;
                case STOMP_ERROR:
                    handleStompError(body.toString());
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing STOMP message", e);
        }
    }

    private void handleStompConnected() {
        Log.d(TAG, "✅ STOMP connected");
        isStompConnected = true;
        subscribeToUserTopics();
    }

    private void handleStompMessageFrame(Map<String, String> headers, String body) {
        try {
            JsonObject json = null;
            if (!body.isEmpty()) {
                try {
                    json = gson.fromJson(body, JsonObject.class);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Failed to parse JSON body: " + body, e);
                    return;
                }
            }

            String destination = headers.get("destination");
            if (destination != null && json != null) {
                if (destination.contains("/chat/")) {
                    processIncomingMessage(json);
                } else if (destination.contains("/location/")) {
                    processLocationUpdate(json);
                } else if (destination.contains("/typing/")) {
                    processTypingIndicator(json);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling STOMP message", e);
        }
    }

    private void handleStompError(String body) {
        Log.e(TAG, "❌ STOMP error: " + body);
        notifyError("STOMP error: " + body);
    }

    private void subscribeToUserTopics() {
        if (currentUserId == null) return;

        subscribeToTopic("/user/" + currentUserId + "/chat");
        subscribeToTopic("/user/" + currentUserId + "/location");
        subscribeToTopic("/user/" + currentUserId + "/typing");
    }

    private void subscribeToTopic(String destination) {
        StringBuilder frame = new StringBuilder();
        frame.append(STOMP_SUBSCRIBE).append("\n");
        frame.append("id:sub-").append(destination.hashCode()).append("\n");
        frame.append("destination:").append(destination).append("\n");
        frame.append("\n\0");

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(frame.toString());
            Log.d(TAG, "📤 Subscribed to: " + destination);
        }
    }

    // Message processing methods
    private void processIncomingMessage(JsonObject json) {
        try {
            Message message = parseMessageFromJson(json);
            notifyMessageReceived(message);
        } catch (Exception e) {
            Log.e(TAG, "Error processing incoming message", e);
        }
    }

    private void processLocationUpdate(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            double latitude = json.get("latitude").getAsDouble();
            double longitude = json.get("longitude").getAsDouble();

            for (LocationListener listener : locationListeners) {
                listener.onLocationUpdate(userId, latitude, longitude);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing location update", e);
        }
    }

    private void processTypingIndicator(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            boolean isTyping = json.get("isTyping").getAsBoolean();

            for (ChatListener listener : chatListeners) {
                listener.onTypingIndicator(userId, isTyping);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing typing indicator", e);
        }
    }

    // Helper methods
    private Message parseMessageFromJson(JsonObject json) {
        Message message = new Message();

        if (json.has("id")) message.setId(json.get("id").getAsLong());
        if (json.has("conversationId")) message.setConversationId(json.get("conversationId").getAsLong());
        if (json.has("senderId")) message.setSenderId(json.get("senderId").getAsLong());
        if (json.has("content")) message.setContent(json.get("content").getAsString());
        if (json.has("messageType")) message.setMessageType(json.get("messageType").getAsString());
        if (json.has("timestamp")) {
            long timestampLong = json.get("timestamp").getAsLong();
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault());
            String timeString = sdf.format(new java.util.Date(timestampLong));
            message.setTimestamp(timeString);
        }
        return message;
    }

    private void scheduleReconnect() {
        reconnectAttempts++;
        Log.d(TAG, "🔄 Scheduling reconnect attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);

        reconnectHandler.postDelayed(() -> {
            if (!isConnected && reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
                connect(currentUserId, null);
            }
        }, RECONNECT_DELAY * reconnectAttempts);
    }

    // Listener management
    public void addListener(WebSocketListener listener) {
        if (listener != null && !listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
    }

    public void addChatListener(ChatListener listener) {
        if (listener != null && !chatListeners.contains(listener)) {
            chatListeners.add(listener);
        }
    }

    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    public void addLocationListener(LocationListener listener) {
        if (listener != null && !locationListeners.contains(listener)) {
            locationListeners.add(listener);
        }
    }

    public void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    // Notification methods
    private void notifyConnectionChanged(boolean connected) {
        for (WebSocketListener listener : listeners) {
            listener.onConnectionChanged(connected);
        }
    }

    private void notifyError(String error) {
        for (WebSocketListener listener : listeners) {
            listener.onError(error);
        }
    }

    private void notifyMessageReceived(Message message) {
        for (ChatListener listener : chatListeners) {
            listener.onMessageReceived(message);
        }
    }

    // Public utility methods
    public boolean isConnected() {
        return isConnected && isStompConnected;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    // Send methods
    public void sendMessage(Long conversationId, String content) {
        if (!isConnected() || conversationId == null || content == null) {
            Log.w(TAG, "Cannot send message: not connected or invalid parameters");
            return;
        }

        try {
            JsonObject messageJson = new JsonObject();
            messageJson.addProperty("conversationId", conversationId);
            messageJson.addProperty("content", content);
            messageJson.addProperty("senderId", currentUserId);
            messageJson.addProperty("messageType", "TEXT");

            StringBuilder frame = new StringBuilder();
            frame.append("SEND").append("\n");
            frame.append("destination:/app/chat.send").append("\n");
            frame.append("content-type:application/json").append("\n");
            frame.append("\n");
            frame.append(messageJson.toString()).append("\0");

            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(frame.toString());
                Log.d(TAG, "📤 Message sent to conversation: " + conversationId);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }
}