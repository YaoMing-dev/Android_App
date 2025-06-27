// app/src/main/java/com/example/newtrade/websocket/RealtimeWebSocketService.java
// ✅ FIXED: STOMP-compatible WebSocket Service cho Chat + GPS Realtime
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
            Log.e(TAG, "❌ Invalid user ID, cannot connect");
            notifyError("Invalid user session");
            return;
        }

        isConnecting = true;

        try {
            // ✅ FIX: Use STOMP endpoint from backend
            String wsUrl = Constants.WS_BASE_URL + "/ws";
            Log.d(TAG, "🔗 Connecting to STOMP WebSocket: " + wsUrl);

            URI uri = URI.create(wsUrl);

            webSocketClient = new WebSocketClient(uri, new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    isConnecting = false;
                    reconnectAttempts = 0;

                    Log.d(TAG, "✅ WebSocket connected, sending STOMP CONNECT");
                    sendStompConnect();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📩 Raw STOMP frame: " + message);
                    handleStompFrame(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    isConnecting = false;
                    isStompConnected = false;

                    Log.d(TAG, "❌ WebSocket closed: " + reason);
                    notifyDisconnected();

                    // Auto reconnect
                    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    isConnected = false;
                    isConnecting = false;
                    isStompConnected = false;

                    Log.e(TAG, "❌ WebSocket error", ex);
                    notifyError("Connection error: " + ex.getMessage());

                    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            Log.e(TAG, "❌ WebSocket connection failed", e);
            isConnecting = false;
            notifyError("Connection failed: " + e.getMessage());
        }
    }

    // ===== STOMP PROTOCOL METHODS =====

    private void sendStompConnect() {
        StringBuilder connectFrame = new StringBuilder();
        connectFrame.append(STOMP_CONNECT).append("\n");
        connectFrame.append("accept-version:1.1,1.0").append("\n");
        connectFrame.append("heart-beat:10000,10000").append("\n");
        connectFrame.append("User-ID:").append(currentUserId).append("\n");
        connectFrame.append("\n");
        connectFrame.append("\u0000");

        sendRawMessage(connectFrame.toString());
        Log.d(TAG, "📡 Sent STOMP CONNECT frame");
    }

    private void handleStompFrame(String frame) {
        try {
            String[] lines = frame.split("\n", -1);
            if (lines.length < 1) return;

            String command = lines[0];
            Map<String, String> headers = new HashMap<>();
            int bodyStartIndex = -1;

            // Parse headers
            for (int i = 1; i < lines.length; i++) {
                if (lines[i].isEmpty()) {
                    bodyStartIndex = i + 1;
                    break;
                }
                String[] headerParts = lines[i].split(":", 2);
                if (headerParts.length == 2) {
                    headers.put(headerParts[0], headerParts[1]);
                }
            }

            // Extract body
            String body = "";
            if (bodyStartIndex > 0 && bodyStartIndex < lines.length) {
                StringBuilder bodyBuilder = new StringBuilder();
                for (int i = bodyStartIndex; i < lines.length; i++) {
                    bodyBuilder.append(lines[i]);
                    if (i < lines.length - 1) bodyBuilder.append("\n");
                }
                body = bodyBuilder.toString().replace("\u0000", "");
            }

            handleStompCommand(command, headers, body);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing STOMP frame", e);
        }
    }

    private void handleStompCommand(String command, Map<String, String> headers, String body) {
        switch (command) {
            case STOMP_CONNECTED:
                isStompConnected = true;
                Log.d(TAG, "✅ STOMP connected successfully");
                subscribeToUserTopics();
                notifyConnected();
                break;

            case STOMP_MESSAGE:
                handleStompMessage(headers, body);
                break;

            case STOMP_ERROR:
                Log.e(TAG, "❌ STOMP error: " + body);
                notifyError("STOMP error: " + body);
                break;

            default:
                Log.d(TAG, "🔔 Unknown STOMP command: " + command);
                break;
        }
    }

    private void subscribeToUserTopics() {
        if (!isStompConnected) return;

        // Subscribe to user-specific queue
        String userTopic = "/user/queue/messages";
        subscribeToTopic(userTopic, "user-messages");

        // Subscribe to global topics
        subscribeToTopic("/topic/chat", "global-chat");
        subscribeToTopic("/topic/location", "location-updates");

        Log.d(TAG, "📡 Subscribed to STOMP topics for user: " + currentUserId);
    }

    private void subscribeToTopic(String destination, String id) {
        StringBuilder subscribeFrame = new StringBuilder();
        subscribeFrame.append(STOMP_SUBSCRIBE).append("\n");
        subscribeFrame.append("destination:").append(destination).append("\n");
        subscribeFrame.append("id:").append(id).append("\n");
        subscribeFrame.append("\n");
        subscribeFrame.append("\u0000");

        sendRawMessage(subscribeFrame.toString());
        Log.d(TAG, "📡 Subscribed to: " + destination);
    }

    private void handleStompMessage(Map<String, String> headers, String body) {
        try {
            String destination = headers.get("destination");

            if (destination != null) {
                if (destination.contains("/queue/messages") || destination.contains("/topic/chat")) {
                    handleChatMessage(body);
                } else if (destination.contains("/topic/location")) {
                    handleLocationMessage(body);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling STOMP message", e);
        }
    }

    private void handleChatMessage(String body) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json.has("type")) {
                String type = json.get("type").getAsString();

                switch (type) {
                    case "NEW_MESSAGE":
                        notifyNewMessage(json);
                        break;
                    case "TYPING":
                        notifyTypingIndicator(json);
                        break;
                    case "MESSAGE_DELIVERED":
                        notifyMessageDelivered(json);
                        break;
                    case "MESSAGE_READ":
                        notifyMessageRead(json);
                        break;
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing chat message", e);
        }
    }

    private void handleLocationMessage(String body) {
        try {
            JsonObject json = gson.fromJson(body, JsonObject.class);

            if (json.has("type")) {
                String type = json.get("type").getAsString();

                if ("LOCATION_UPDATE".equals(type)) {
                    notifyLocationUpdate(json);
                } else if ("NEARBY_USERS".equals(type)) {
                    notifyNearbyUsers(json);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing location message", e);
        }
    }

    // ===== SENDING MESSAGES VIA STOMP =====

    private void sendRawMessage(String message) {
        if (webSocketClient != null && isConnected) {
            webSocketClient.send(message);
        } else {
            Log.w(TAG, "⚠️ Cannot send message - not connected");
        }
    }

    private void sendStompMessage(String destination, String body) {
        if (!isStompConnected) {
            Log.w(TAG, "⚠️ STOMP not connected, cannot send message");
            return;
        }

        StringBuilder sendFrame = new StringBuilder();
        sendFrame.append(STOMP_SEND).append("\n");
        sendFrame.append("destination:").append(destination).append("\n");
        sendFrame.append("content-type:application/json").append("\n");
        sendFrame.append("User-ID:").append(currentUserId).append("\n");
        sendFrame.append("\n");
        sendFrame.append(body);
        sendFrame.append("\u0000");

        sendRawMessage(sendFrame.toString());
    }

    // ===== CHAT METHODS =====

    public void joinConversation(Long conversationId) {
        JsonObject joinMessage = new JsonObject();
        joinMessage.addProperty("type", "JOIN_CONVERSATION");
        joinMessage.addProperty("conversationId", conversationId);
        joinMessage.addProperty("userId", currentUserId);

        sendStompMessage("/app/chat/" + conversationId + "/join", joinMessage.toString());
        Log.d(TAG, "📡 Joined conversation: " + conversationId);
    }

    public void leaveConversation(Long conversationId) {
        JsonObject leaveMessage = new JsonObject();
        leaveMessage.addProperty("type", "LEAVE_CONVERSATION");
        leaveMessage.addProperty("conversationId", conversationId);
        leaveMessage.addProperty("userId", currentUserId);

        sendStompMessage("/app/chat/" + conversationId + "/leave", leaveMessage.toString());
        Log.d(TAG, "📡 Left conversation: " + conversationId);
    }

    public void sendChatMessage(Long conversationId, String content, String messageType) {
        JsonObject chatMessage = new JsonObject();
        chatMessage.addProperty("type", "SEND_MESSAGE");
        chatMessage.addProperty("conversationId", conversationId);
        chatMessage.addProperty("senderId", currentUserId);
        chatMessage.addProperty("content", content);
        chatMessage.addProperty("messageType", messageType != null ? messageType : "TEXT");
        chatMessage.addProperty("timestamp", System.currentTimeMillis());

        sendStompMessage("/app/chat/" + conversationId + "/send", chatMessage.toString());
        Log.d(TAG, "📤 Sent chat message to conversation: " + conversationId);
    }

    public void sendTypingIndicator(Long conversationId, boolean isTyping) {
        JsonObject typingMessage = new JsonObject();
        typingMessage.addProperty("type", "TYPING");
        typingMessage.addProperty("conversationId", conversationId);
        typingMessage.addProperty("userId", currentUserId);
        typingMessage.addProperty("isTyping", isTyping);

        sendStompMessage("/app/chat/" + conversationId + "/typing", typingMessage.toString());
    }

    // ===== LOCATION METHODS =====

    public void sendLocationUpdate(double latitude, double longitude) {
        JsonObject locationMessage = new JsonObject();
        locationMessage.addProperty("type", "LOCATION_UPDATE");
        locationMessage.addProperty("userId", currentUserId);
        locationMessage.addProperty("latitude", latitude);
        locationMessage.addProperty("longitude", longitude);
        locationMessage.addProperty("timestamp", System.currentTimeMillis());

        sendStompMessage("/app/location/update", locationMessage.toString());
        Log.d(TAG, "📍 Sent location update: " + latitude + ", " + longitude);
    }

    public void requestNearbyUsers(double latitude, double longitude, double radiusKm) {
        JsonObject requestMessage = new JsonObject();
        requestMessage.addProperty("type", "REQUEST_NEARBY");
        requestMessage.addProperty("userId", currentUserId);
        requestMessage.addProperty("latitude", latitude);
        requestMessage.addProperty("longitude", longitude);
        requestMessage.addProperty("radiusKm", radiusKm);

        sendStompMessage("/app/location/nearby", requestMessage.toString());
        Log.d(TAG, "📍 Requested nearby users");
    }

    // ===== LISTENER MANAGEMENT =====

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
            // Parse nearby users data
            Map<Long, Map<String, Object>> nearbyUsers = new HashMap<>();
            // Implementation depends on JSON structure from backend

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