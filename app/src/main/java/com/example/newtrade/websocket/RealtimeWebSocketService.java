// app/src/main/java/com/example/newtrade/websocket/RealtimeWebSocketService.java
package com.example.newtrade.websocket;

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
import java.util.concurrent.ConcurrentHashMap;

public class RealtimeWebSocketService {
    private static final String TAG = "RealtimeWebSocketService";

    // ✅ Singleton instance
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

    // Listeners
    private final Map<String, List<WebSocketListener>> listeners = new ConcurrentHashMap<>();
    private final Map<Long, List<ChatListener>> chatListeners = new ConcurrentHashMap<>();
    private final List<LocationListener> locationListeners = new ArrayList<>();

    // Gson for JSON parsing
    private final Gson gson = new Gson();

    // ✅ Private constructor for Singleton
    private RealtimeWebSocketService() {
        // Private constructor
    }

    // ✅ getInstance method
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

    // ===== INTERFACES =====

    public interface WebSocketListener {
        void onConnectionChanged(boolean connected);
        void onError(String error);
    }

    public interface ChatListener {
        void onMessageReceived(Message message);
        void onTypingIndicator(Long userId, boolean isTyping);
        void onMessageDelivered(Long messageId);
        void onMessageRead(Long messageId);
    }

    public interface LocationListener {
        void onUserLocationUpdate(Long userId, double latitude, double longitude);
        void onNearbyUsersUpdate(Map<Long, Map<String, Object>> nearbyUsers);
    }

    // ===== CONNECTION METHODS =====

    // ✅ FIX: Connect method with only userId parameter (matching MainActivity call)
    public void connect(Long userId) {
        this.currentUserId = userId;

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
                    Log.d(TAG, "❌ WebSocket closed: " + reason);
                    isConnected = false;
                    isStompConnected = false;
                    isConnecting = false;

                    notifyConnectionChanged(false);

                    // Auto-reconnect if not manually disconnected
                    if (remote && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "❌ WebSocket error: " + ex.getMessage());
                    isConnecting = false;
                    notifyError("Connection error: " + ex.getMessage());

                    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to create WebSocket connection", e);
            isConnecting = false;
            notifyError("Failed to connect: " + e.getMessage());
        }
    }

    public void disconnect() {
        isConnecting = false;
        reconnectAttempts = MAX_RECONNECT_ATTEMPTS; // Prevent reconnection

        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }

        isConnected = false;
        isStompConnected = false;
        Log.d(TAG, "🔌 WebSocket disconnected");
    }

    private void scheduleReconnect() {
        reconnectAttempts++;
        Log.d(TAG, "🔄 Scheduling reconnect attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);

        new Thread(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY * reconnectAttempts);
                if (!isConnected && reconnectAttempts <= MAX_RECONNECT_ATTEMPTS) {
                    connect(currentUserId);
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Reconnect interrupted", e);
            }
        }).start();
    }

    // ===== STOMP PROTOCOL HANDLING =====

    private void sendStompConnect() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            StringBuilder frame = new StringBuilder();
            frame.append(STOMP_CONNECT).append("\n");
            frame.append("accept-version:1.2\n");
            frame.append("host:").append(Constants.WS_HOST != null ? Constants.WS_HOST : "localhost").append("\n");
            frame.append("user-id:").append(currentUserId).append("\n");
            frame.append("\n");
            frame.append("\0");

            webSocketClient.send(frame.toString());
            Log.d(TAG, "📤 STOMP CONNECT sent");
        }
    }

    private void handleStompMessage(String message) {
        try {
            String[] lines = message.split("\n");
            if (lines.length == 0) return;

            String command = lines[0];

            switch (command) {
                case STOMP_CONNECTED:
                    handleStompConnected();
                    break;
                case STOMP_MESSAGE:
                    handleStompMessageReceived(lines);
                    break;
                case STOMP_ERROR:
                    handleStompError(lines);
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling STOMP message", e);
        }
    }

    private void handleStompConnected() {
        Log.d(TAG, "✅ STOMP connected");
        isStompConnected = true;

        // Subscribe to user-specific channels
        subscribeToUserChannels();
    }

    private void subscribeToUserChannels() {
        if (currentUserId != null) {
            // Subscribe to user messages
            sendStompSubscribe("/user/" + currentUserId + "/queue/messages");
            // Subscribe to notifications
            sendStompSubscribe("/user/" + currentUserId + "/queue/notifications");
            // Subscribe to location updates
            sendStompSubscribe("/topic/location");

            Log.d(TAG, "✅ Subscribed to user channels");
        }
    }

    private void sendStompSubscribe(String destination) {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            StringBuilder frame = new StringBuilder();
            frame.append(STOMP_SUBSCRIBE).append("\n");
            frame.append("id:").append(destination.hashCode()).append("\n");
            frame.append("destination:").append(destination).append("\n");
            frame.append("\n");
            frame.append("\0");

            webSocketClient.send(frame.toString());
            Log.d(TAG, "📤 STOMP SUBSCRIBE: " + destination);
        }
    }

    private void handleStompMessageReceived(String[] lines) {
        try {
            // Parse STOMP message headers and body
            Map<String, String> headers = new HashMap<>();
            int bodyStartIndex = -1;

            for (int i = 1; i < lines.length; i++) {
                String line = lines[i];
                if (line.isEmpty()) {
                    bodyStartIndex = i + 1;
                    break;
                }
                String[] headerParts = line.split(":", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }

            if (bodyStartIndex > 0 && bodyStartIndex < lines.length) {
                StringBuilder bodyBuilder = new StringBuilder();
                for (int i = bodyStartIndex; i < lines.length; i++) {
                    bodyBuilder.append(lines[i]);
                    if (i < lines.length - 1) bodyBuilder.append("\n");
                }

                String body = bodyBuilder.toString().replace("\0", "");
                String destination = headers.get("destination");

                handleMessageByDestination(destination, body);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error parsing STOMP message", e);
        }
    }

    private void handleMessageByDestination(String destination, String body) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (destination != null) {
                if (destination.contains("/queue/messages")) {
                    handleChatMessage(json);
                } else if (destination.contains("/queue/notifications")) {
                    handleNotification(json);
                } else if (destination.contains("/topic/location")) {
                    handleLocationMessage(json);
                }
            }

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Invalid JSON in message: " + body, e);
        }
    }

    private void handleChatMessage(JsonObject json) {
        try {
            Message message = parseMessageFromJson(json);
            notifyMessageReceived(message);

        } catch (Exception e) {
            Log.e(TAG, "Error handling chat message", e);
        }
    }

    private void handleNotification(JsonObject json) {
        try {
            String type = json.get("type").getAsString();

            switch (type) {
                case "typing":
                    handleTypingIndicator(json);
                    break;
                case "delivered":
                    handleMessageDelivered(json);
                    break;
                case "read":
                    handleMessageRead(json);
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling notification", e);
        }
    }

    private void handleLocationMessage(JsonObject json) {
        try {
            String type = json.get("type").getAsString();

            switch (type) {
                case "user_location":
                    handleUserLocationUpdate(json);
                    break;
                case "nearby_users":
                    handleNearbyUsersUpdate(json);
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling location message", e);
        }
    }

    private void handleStompError(String[] lines) {
        StringBuilder errorBuilder = new StringBuilder();
        for (int i = 1; i < lines.length; i++) {
            errorBuilder.append(lines[i]).append("\n");
        }
        String error = errorBuilder.toString();
        Log.e(TAG, "❌ STOMP Error: " + error);
        notifyError("STOMP Error: " + error);
    }

    // ===== MESSAGE PARSING =====

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

    private void handleTypingIndicator(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            boolean isTyping = json.get("isTyping").getAsBoolean();
            Long conversationId = json.get("conversationId").getAsLong();

            List<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    listener.onTypingIndicator(userId, isTyping);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling typing indicator", e);
        }
    }

    private void handleMessageDelivered(JsonObject json) {
        try {
            Long messageId = json.get("messageId").getAsLong();
            Long conversationId = json.get("conversationId").getAsLong();

            List<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    listener.onMessageDelivered(messageId);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling message delivered", e);
        }
    }

    private void handleMessageRead(JsonObject json) {
        try {
            Long messageId = json.get("messageId").getAsLong();
            Long conversationId = json.get("conversationId").getAsLong();

            List<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    listener.onMessageRead(messageId);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling message read", e);
        }
    }

    private void handleUserLocationUpdate(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            double latitude = json.get("latitude").getAsDouble();
            double longitude = json.get("longitude").getAsDouble();

            synchronized (locationListeners) {
                for (LocationListener listener : locationListeners) {
                    listener.onUserLocationUpdate(userId, latitude, longitude);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling user location update", e);
        }
    }

    private void handleNearbyUsersUpdate(JsonObject json) {
        try {
            Map<Long, Map<String, Object>> nearbyUsers = new HashMap<>();

            if (json.has("users")) {
                JsonObject usersObj = json.getAsJsonObject("users");
                for (String userIdStr : usersObj.keySet()) {
                    Long userId = Long.parseLong(userIdStr);
                    JsonObject userInfo = usersObj.getAsJsonObject(userIdStr);

                    Map<String, Object> userInfoMap = new HashMap<>();
                    if (userInfo.has("name")) {
                        userInfoMap.put("name", userInfo.get("name").getAsString());
                    }
                    if (userInfo.has("distance")) {
                        userInfoMap.put("distance", userInfo.get("distance").getAsDouble());
                    }
                    if (userInfo.has("latitude")) {
                        userInfoMap.put("latitude", userInfo.get("latitude").getAsDouble());
                    }
                    if (userInfo.has("longitude")) {
                        userInfoMap.put("longitude", userInfo.get("longitude").getAsDouble());
                    }

                    nearbyUsers.put(userId, userInfoMap);
                }
            }

            synchronized (locationListeners) {
                for (LocationListener listener : locationListeners) {
                    listener.onNearbyUsersUpdate(nearbyUsers);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error handling nearby users update", e);
        }
    }

    // ===== LISTENER MANAGEMENT =====

    public void addWebSocketListener(String tag, WebSocketListener listener) {
        if (listener != null) {
            listeners.computeIfAbsent(tag, k -> new ArrayList<>()).add(listener);
        }
    }

    public void removeWebSocketListener(String tag) {
        listeners.remove(tag);
    }

    public void addChatListener(Long conversationId, ChatListener listener) {
        if (listener != null && conversationId != null) {
            chatListeners.computeIfAbsent(conversationId, k -> new ArrayList<>()).add(listener);
        }
    }

    public void removeChatListener(Long conversationId, ChatListener listener) {
        if (conversationId != null) {
            List<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                conversationListeners.remove(listener);
                if (conversationListeners.isEmpty()) {
                    chatListeners.remove(conversationId);
                }
            }
        }
    }

    public void addLocationListener(LocationListener listener) {
        if (listener != null) {
            synchronized (locationListeners) {
                if (!locationListeners.contains(listener)) {
                    locationListeners.add(listener);
                }
            }
        }
    }

    public void removeLocationListener(LocationListener listener) {
        synchronized (locationListeners) {
            locationListeners.remove(listener);
        }
    }

    // ===== NOTIFICATION METHODS =====

    private void notifyConnectionChanged(boolean connected) {
        for (List<WebSocketListener> listenerList : listeners.values()) {
            for (WebSocketListener listener : listenerList) {
                try {
                    listener.onConnectionChanged(connected);
                } catch (Exception e) {
                    Log.e(TAG, "Error notifying listener", e);
                }
            }
        }
    }

    private void notifyError(String error) {
        for (List<WebSocketListener> listenerList : listeners.values()) {
            for (WebSocketListener listener : listenerList) {
                try {
                    listener.onError(error);
                } catch (Exception e) {
                    Log.e(TAG, "Error notifying listener", e);
                }
            }
        }
    }

    private void notifyMessageReceived(Message message) {
        if (message.getConversationId() != null) {
            List<ChatListener> conversationListeners = chatListeners.get(message.getConversationId());
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    try {
                        listener.onMessageReceived(message);
                    } catch (Exception e) {
                        Log.e(TAG, "Error notifying listener", e);
                    }
                }
            }
        }
    }

    // ===== PUBLIC UTILITY METHODS =====

    public boolean isConnected() {
        return isConnected && isStompConnected;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }

    // ===== SEND METHODS =====

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
            frame.append("destination:/app/chat/").append(conversationId).append("/send").append("\n");
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

    public void sendTypingIndicator(Long conversationId, boolean isTyping) {
        if (!isConnected() || conversationId == null) {
            return;
        }

        try {
            JsonObject typingJson = new JsonObject();
            typingJson.addProperty("conversationId", conversationId);
            typingJson.addProperty("senderId", currentUserId);
            typingJson.addProperty("isTyping", isTyping);

            StringBuilder frame = new StringBuilder();
            frame.append("SEND").append("\n");
            frame.append("destination:/app/chat/").append(conversationId).append("/typing").append("\n");
            frame.append("content-type:application/json").append("\n");
            frame.append("\n");
            frame.append(typingJson.toString()).append("\0");

            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(frame.toString());
                Log.d(TAG, "📤 Typing indicator sent: " + isTyping);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending typing indicator", e);
        }
    }

    public void joinConversation(Long conversationId) {
        if (conversationId != null) {
            // Subscribe to specific conversation
            sendStompSubscribe("/topic/conversation/" + conversationId);
            Log.d(TAG, "✅ Joined conversation: " + conversationId);
        }
    }

    public void leaveConversation(Long conversationId) {
        // Implementation depends on backend support for unsubscribe
        Log.d(TAG, "Left conversation: " + conversationId);
    }

    public void sendLocationUpdate(double latitude, double longitude) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot send location: not connected");
            return;
        }

        try {
            JsonObject locationJson = new JsonObject();
            locationJson.addProperty("userId", currentUserId);
            locationJson.addProperty("latitude", latitude);
            locationJson.addProperty("longitude", longitude);
            locationJson.addProperty("timestamp", System.currentTimeMillis());

            StringBuilder frame = new StringBuilder();
            frame.append("SEND").append("\n");
            frame.append("destination:/app/location/update").append("\n");
            frame.append("content-type:application/json").append("\n");
            frame.append("\n");
            frame.append(locationJson.toString()).append("\0");

            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(frame.toString());
                Log.d(TAG, "📤 Location update sent: " + latitude + ", " + longitude);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error sending location update", e);
        }
    }

    public void requestNearbyUsers(double latitude, double longitude, double radiusKm) {
        if (!isConnected()) {
            Log.w(TAG, "Cannot request nearby users: not connected");
            return;
        }

        try {
            JsonObject requestJson = new JsonObject();
            requestJson.addProperty("userId", currentUserId);
            requestJson.addProperty("latitude", latitude);
            requestJson.addProperty("longitude", longitude);
            requestJson.addProperty("radiusKm", radiusKm);

            StringBuilder frame = new StringBuilder();
            frame.append("SEND").append("\n");
            frame.append("destination:/app/location/nearby").append("\n");
            frame.append("content-type:application/json").append("\n");
            frame.append("\n");
            frame.append(requestJson.toString()).append("\0");

            if (webSocketClient != null && webSocketClient.isOpen()) {
                webSocketClient.send(frame.toString());
                Log.d(TAG, "📤 Nearby users request sent");
            }

        } catch (Exception e) {
            Log.e(TAG, "Error requesting nearby users", e);
        }
    }
}