// app/src/main/java/com/example/newtrade/websocket/RealtimeWebSocketService.java
package com.example.newtrade.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import com.example.newtrade.models.Message;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.drafts.Draft_6455;
import org.java_websocket.handshake.ServerHandshake;
import java.net.URI;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class RealtimeWebSocketService extends Service {
    private static final String TAG = "RealtimeWebSocketService";

    // WebSocket client
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private boolean isStompConnected = false;

    // Data
    private final Gson gson = new Gson();
    private SharedPrefsManager prefsManager;
    private Long currentUserId;

    // Listeners
    private final Set<WebSocketListener> listeners = ConcurrentHashMap.newKeySet();
    private final Map<Long, Set<ChatListener>> chatListeners = new ConcurrentHashMap<>();
    private final Set<LocationListener> locationListeners = ConcurrentHashMap.newKeySet();

    // Connection management
    private int reconnectAttempts = 0;
    private static final int MAX_RECONNECT_ATTEMPTS = 5;
    private static final long RECONNECT_DELAY = 3000; // 3 seconds
    private Handler reconnectHandler = new Handler(Looper.getMainLooper());

    // STOMP frame types
    private static final String STOMP_CONNECT = "CONNECT";
    private static final String STOMP_CONNECTED = "CONNECTED";
    private static final String STOMP_SEND = "SEND";
    private static final String STOMP_SUBSCRIBE = "SUBSCRIBE";
    private static final String STOMP_MESSAGE = "MESSAGE";
    private static final String STOMP_ERROR = "ERROR";

    // Binder for activities to bind to this service
    private final IBinder binder = new LocalBinder();

    public class LocalBinder extends Binder {
        public RealtimeWebSocketService getService() {
            return RealtimeWebSocketService.this;
        }
    }

    // ===== INTERFACES =====

    public interface WebSocketListener {
        void onConnected();
        void onDisconnected();
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

    // ===== SERVICE LIFECYCLE =====

    @Override
    public void onCreate() {
        super.onCreate();
        prefsManager = SharedPrefsManager.getInstance(this);
        currentUserId = prefsManager.getUserId();

        Log.d(TAG, "✅ RealtimeWebSocketService created for user: " + currentUserId);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (currentUserId != null && !isConnected && !isConnecting) {
            connect();
        }
        return START_STICKY; // Service will be restarted if killed
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        disconnect();
        Log.d(TAG, "🧹 RealtimeWebSocketService destroyed");
    }

    // ===== WEBSOCKET CONNECTION WITH STOMP SUPPORT =====

    private void connect() {
        if (isConnected || isConnecting) {
            Log.d(TAG, "Already connected or connecting");
            return;
        }

        if (currentUserId == null || currentUserId <= 0) {
            Log.e(TAG, "❌ Cannot connect: Invalid user ID");
            return;
        }

        try {
            isConnecting = true;
            String wsUrl = Constants.WS_BASE_URL + "/ws";
            URI serverUri = URI.create(wsUrl);

            Log.d(TAG, "🔄 Connecting to WebSocket: " + wsUrl);

            webSocketClient = new WebSocketClient(serverUri, new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "✅ WebSocket connected");
                    isConnected = true;
                    isConnecting = false;
                    reconnectAttempts = 0;

                    // Send STOMP CONNECT frame
                    sendStompConnect();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 Received: " + message);
                    processStompMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "❌ WebSocket closed: " + code + " - " + reason);
                    isConnected = false;
                    isConnecting = false;
                    isStompConnected = false;

                    notifyDisconnected();

                    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "❌ WebSocket error", ex);
                    isConnected = false;
                    isConnecting = false;
                    isStompConnected = false;

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

    private void sendStompConnect() {
        StringBuilder frame = new StringBuilder();
        frame.append(STOMP_CONNECT).append("\n");
        frame.append("accept-version:1.2\n");
        frame.append("host:").append(Constants.WS_BASE_URL).append("\n");
        frame.append("user-id:").append(currentUserId).append("\n");
        frame.append("\n\0");

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(frame.toString());
            Log.d(TAG, "📤 Sent STOMP CONNECT frame");
        }
    }

    // ✅ FIX: Improved STOMP message processing with proper JSON handling
    private void processStompMessage(String message) {
        try {
            // Parse STOMP frame
            String[] lines = message.split("\n");
            if (lines.length < 2) return;

            String command = lines[0];
            Map<String, String> headers = new HashMap<>();
            String body = "";

            // Parse headers and body
            int bodyStartIndex = -1;
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].trim().isEmpty()) {
                    bodyStartIndex = i + 1;
                    break;
                }
                String[] headerParts = lines[i].split(":", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0].trim(), headerParts[1].trim());
                }
            }

            if (bodyStartIndex >= 0 && bodyStartIndex < lines.length) {
                StringBuilder bodyBuilder = new StringBuilder();
                for (int i = bodyStartIndex; i < lines.length; i++) {
                    bodyBuilder.append(lines[i]);
                    if (i < lines.length - 1) bodyBuilder.append("\n");
                }
                body = bodyBuilder.toString().trim();
            }

            // Handle STOMP commands
            switch (command) {
                case STOMP_CONNECTED:
                    handleStompConnected();
                    break;
                case STOMP_MESSAGE:
                    handleStompMessage(headers, body);
                    break;
                case STOMP_ERROR:
                    handleStompError(body);
                    break;
            }

        } catch (Exception e) {
            Log.e(TAG, "Error processing STOMP message: " + message, e);
        }
    }

    private void handleStompConnected() {
        Log.d(TAG, "✅ STOMP connected");
        isStompConnected = true;
        notifyConnected();

        // Subscribe to user-specific topics
        subscribeToUserTopics();
    }

    // ✅ FIX: Handle STOMP message with proper JSON parsing (fixes String to Map conversion)
    private void handleStompMessage(Map<String, String> headers, String body) {
        try {
            // Parse JSON body properly - no more String to Map<String,Object> conversion issues
            JsonObject json = null;
            if (!body.isEmpty()) {
                try {
                    json = gson.fromJson(body, JsonObject.class);
                } catch (JsonSyntaxException e) {
                    Log.e(TAG, "Failed to parse JSON body: " + body, e);
                    return;
                }
            }

            // Process based on destination
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

        // Subscribe to chat messages
        subscribeToTopic("/user/" + currentUserId + "/chat");

        // Subscribe to location updates
        subscribeToTopic("/user/" + currentUserId + "/location");

        // Subscribe to typing indicators
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

    // ===== MESSAGE PROCESSING =====

    private void processIncomingMessage(JsonObject json) {
        try {
            String messageType = json.has("type") ? json.get("type").getAsString() : "message";

            switch (messageType) {
                case "new_message":
                    notifyNewMessage(json);
                    break;
                case "message_delivered":
                    notifyMessageDelivered(json);
                    break;
                case "message_read":
                    notifyMessageRead(json);
                    break;
                case "typing_indicator":
                    notifyTypingIndicator(json);
                    break;
                default:
                    Log.w(TAG, "Unknown message type: " + messageType);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing incoming message", e);
        }
    }

    private void processLocationUpdate(JsonObject json) {
        try {
            String updateType = json.has("type") ? json.get("type").getAsString() : "location_update";

            switch (updateType) {
                case "user_location":
                    notifyLocationUpdate(json);
                    break;
                case "nearby_users":
                    notifyNearbyUsers(json);
                    break;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error processing location update", e);
        }
    }

    private void processTypingIndicator(JsonObject json) {
        try {
            notifyTypingIndicator(json);
        } catch (Exception e) {
            Log.e(TAG, "Error processing typing indicator", e);
        }
    }

    // ===== PUBLIC API METHODS =====

    public void addWebSocketListener(WebSocketListener listener) {
        listeners.add(listener);
    }

    public void removeWebSocketListener(WebSocketListener listener) {
        listeners.remove(listener);
    }

    public void addChatListener(Long conversationId, ChatListener listener) {
        chatListeners.computeIfAbsent(conversationId, k -> ConcurrentHashMap.newKeySet()).add(listener);
    }

    public void removeChatListener(Long conversationId, ChatListener listener) {
        Set<ChatListener> conversationListeners = chatListeners.get(conversationId);
        if (conversationListeners != null) {
            conversationListeners.remove(listener);
            if (conversationListeners.isEmpty()) {
                chatListeners.remove(conversationId);
            }
        }
    }

    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
    }

    public void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
    }

    public void sendMessage(Long conversationId, String content) {
        if (!isStompConnected) {
            Log.w(TAG, "Cannot send message: not connected");
            return;
        }

        try {
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("conversationId", conversationId);
            messageData.put("content", content);
            messageData.put("senderId", currentUserId);
            messageData.put("type", "message");

            String messageJson = gson.toJson(messageData);
            sendStompMessage("/app/chat/send", messageJson);

        } catch (Exception e) {
            Log.e(TAG, "Error sending message", e);
        }
    }

    public void sendTypingIndicator(Long conversationId, boolean isTyping) {
        if (!isStompConnected) return;

        try {
            Map<String, Object> typingData = new HashMap<>();
            typingData.put("conversationId", conversationId);
            typingData.put("userId", currentUserId);
            typingData.put("isTyping", isTyping);

            String typingJson = gson.toJson(typingData);
            sendStompMessage("/app/typing", typingJson);

        } catch (Exception e) {
            Log.e(TAG, "Error sending typing indicator", e);
        }
    }

    public void updateLocation(double latitude, double longitude) {
        if (!isStompConnected) return;

        try {
            Map<String, Object> locationData = new HashMap<>();
            locationData.put("userId", currentUserId);
            locationData.put("latitude", latitude);
            locationData.put("longitude", longitude);
            locationData.put("timestamp", System.currentTimeMillis());

            String locationJson = gson.toJson(locationData);
            sendStompMessage("/app/location/update", locationJson);

        } catch (Exception e) {
            Log.e(TAG, "Error sending location update", e);
        }
    }

    public void joinConversation(Long conversationId) {
        if (!isStompConnected) return;

        try {
            Map<String, Object> joinData = new HashMap<>();
            joinData.put("conversationId", conversationId);
            joinData.put("userId", currentUserId);

            String joinJson = gson.toJson(joinData);
            sendStompMessage("/app/chat/join", joinJson);

        } catch (Exception e) {
            Log.e(TAG, "Error joining conversation", e);
        }
    }

    private void sendStompMessage(String destination, String body) {
        StringBuilder frame = new StringBuilder();
        frame.append(STOMP_SEND).append("\n");
        frame.append("destination:").append(destination).append("\n");
        frame.append("content-type:application/json").append("\n");
        frame.append("\n");
        frame.append(body);
        frame.append("\0");

        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.send(frame.toString());
        }
    }

    // ===== NOTIFICATION METHODS =====

    private void notifyConnected() {
        for (WebSocketListener listener : listeners) {
            try {
                listener.onConnected();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyDisconnected() {
        for (WebSocketListener listener : listeners) {
            try {
                listener.onDisconnected();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyError(String error) {
        for (WebSocketListener listener : listeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener", e);
            }
        }
    }

    private void notifyNewMessage(JsonObject json) {
        try {
            Long conversationId = json.get("conversationId").getAsLong();
            Set<ChatListener> conversationListeners = chatListeners.get(conversationId);

            if (conversationListeners != null) {
                // Create Message object from JSON
                Message message = parseMessageFromJson(json);

                for (ChatListener listener : conversationListeners) {
                    listener.onMessageReceived(message);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error notifying new message", e);
        }
    }

    private void notifyTypingIndicator(JsonObject json) {
        try {
            Long conversationId = json.get("conversationId").getAsLong();
            Long userId = json.get("userId").getAsLong();
            boolean isTyping = json.get("isTyping").getAsBoolean();

            Set<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    listener.onTypingIndicator(userId, isTyping);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error notifying typing indicator", e);
        }
    }

    private void notifyMessageDelivered(JsonObject json) {
        try {
            Long messageId = json.get("messageId").getAsLong();
            Long conversationId = json.get("conversationId").getAsLong();

            Set<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    listener.onMessageDelivered(messageId);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error notifying message delivered", e);
        }
    }

    private void notifyMessageRead(JsonObject json) {
        try {
            Long messageId = json.get("messageId").getAsLong();
            Long conversationId = json.get("conversationId").getAsLong();

            Set<ChatListener> conversationListeners = chatListeners.get(conversationId);
            if (conversationListeners != null) {
                for (ChatListener listener : conversationListeners) {
                    listener.onMessageRead(messageId);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Error notifying message read", e);
        }
    }

    private void notifyLocationUpdate(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            double latitude = json.get("latitude").getAsDouble();
            double longitude = json.get("longitude").getAsDouble();

            for (LocationListener listener : locationListeners) {
                listener.onUserLocationUpdate(userId, latitude, longitude);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error notifying location update", e);
        }
    }

    private void notifyNearbyUsers(JsonObject json) {
        try {
            // Parse nearby users data - implement based on backend structure
            Map<Long, Map<String, Object>> nearbyUsers = new HashMap<>();
            // TODO: Parse nearby users from JSON

            for (LocationListener listener : locationListeners) {
                listener.onNearbyUsersUpdate(nearbyUsers);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error notifying nearby users", e);
        }
    }

    // ===== HELPER METHODS =====

    private Message parseMessageFromJson(JsonObject json) {
        // Create Message object from JSON data
        Message message = new Message();

        if (json.has("id")) message.setId(json.get("id").getAsLong());
        if (json.has("conversationId")) message.setConversationId(json.get("conversationId").getAsLong());
        if (json.has("senderId")) message.setSenderId(json.get("senderId").getAsLong());
        if (json.has("content")) message.setContent(json.get("content").getAsString());
        if (json.has("messageType")) message.setMessageType(json.get("messageType").getAsString());
        if (json.has("timestamp")) {
            long timestampLong = json.get("timestamp").getAsLong();
            // Convert to readable format like "10:30 AM"
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
                connect();
            }
        }, RECONNECT_DELAY * reconnectAttempts);
    }

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

    public boolean isConnected() {
        return isConnected && isStompConnected;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }
}