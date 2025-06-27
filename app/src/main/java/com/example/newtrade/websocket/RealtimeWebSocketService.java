// app/src/main/java/com/example/newtrade/websocket/RealtimeWebSocketService.java
// ✅ COMPLETE WebSocket Service cho Chat + GPS Realtime
package com.example.newtrade.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
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

    // ===== WEBSOCKET CONNECTION =====

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
            // ✅ FIX: Complete WebSocket URL
            String wsUrl = Constants.WS_BASE_URL + "/chat?userId=" + currentUserId;
            Log.d(TAG, "🔗 Connecting to WebSocket: " + wsUrl);

            URI uri = URI.create(wsUrl);

            webSocketClient = new WebSocketClient(uri, new Draft_6455()) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    isConnecting = false;
                    reconnectAttempts = 0;

                    Log.d(TAG, "✅ WebSocket connected");
                    notifyConnected();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📩 Received: " + message);
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    isConnecting = false;

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

                    Log.e(TAG, "❌ WebSocket error", ex);
                    notifyError(ex.getMessage());
                }
            };

            webSocketClient.connect();

        } catch (Exception e) {
            isConnecting = false;
            Log.e(TAG, "❌ Failed to create WebSocket connection", e);
            notifyError("Connection failed: " + e.getMessage());
        }
    }

    private void scheduleReconnect() {
        reconnectAttempts++;
        Log.d(TAG, "🔄 Scheduling reconnect attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS);

        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
            if (!isConnected && !isConnecting) {
                connect();
            }
        }, RECONNECT_DELAY * reconnectAttempts); // Exponential backoff
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        isConnected = false;
        isConnecting = false;
        reconnectAttempts = 0;

        Log.d(TAG, "🔌 WebSocket disconnected");
    }

    // ===== AUTHENTICATION =====

    private void authenticateUser() {
        JsonObject authMessage = new JsonObject();
        authMessage.addProperty("type", "AUTHENTICATE");
        authMessage.addProperty("userId", currentUserId);
        authMessage.addProperty("action", "user_online");

        sendMessage(authMessage.toString());
        Log.d(TAG, "🔐 Authentication sent for user: " + currentUserId);
    }

    // ===== MESSAGE HANDLING =====

    private void handleIncomingMessage(String rawMessage) {
        try {
            JsonObject json = gson.fromJson(rawMessage, JsonObject.class);
            String type = json.has("type") ? json.get("type").getAsString() : "";

            switch (type) {
                case "CHAT_MESSAGE":
                    handleChatMessage(json);
                    break;
                case "TYPING_INDICATOR":
                    handleTypingIndicator(json);
                    break;
                case "MESSAGE_STATUS":
                    handleMessageStatus(json);
                    break;
                case "LOCATION_UPDATE":
                    handleLocationUpdate(json);
                    break;
                case "NEARBY_USERS":
                    handleNearbyUsers(json);
                    break;
                case "USER_STATUS":
                    handleUserStatus(json);
                    break;
                case "SYSTEM_MESSAGE":
                    handleSystemMessage(json);
                    break;
                default:
                    Log.d(TAG, "⚠️ Unknown message type: " + type);
            }

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "❌ Error parsing message JSON", e);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling message", e);
        }
    }

    private void handleChatMessage(JsonObject json) {
        try {
            Message message = new Message();

            if (json.has("id")) message.setId(json.get("id").getAsLong());
            if (json.has("conversationId")) message.setConversationId(json.get("conversationId").getAsLong());
            if (json.has("senderId")) message.setSenderId(json.get("senderId").getAsLong());
            if (json.has("senderName")) message.setSenderName(json.get("senderName").getAsString());
            if (json.has("content")) message.setContent(json.get("content").getAsString());
            if (json.has("messageType")) message.setMessageType(json.get("messageType").getAsString());
            if (json.has("timestamp")) message.setTimestamp(json.get("timestamp").getAsString());

            // Notify chat listeners for this conversation
            Long conversationId = message.getConversationId();
            if (conversationId != null && chatListeners.containsKey(conversationId)) {
                for (ChatListener listener : chatListeners.get(conversationId)) {
                    listener.onMessageReceived(message);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing chat message", e);
        }
    }

    private void handleTypingIndicator(JsonObject json) {
        try {
            Long conversationId = json.get("conversationId").getAsLong();
            Long userId = json.get("userId").getAsLong();
            boolean isTyping = json.get("isTyping").getAsBoolean();

            if (chatListeners.containsKey(conversationId)) {
                for (ChatListener listener : chatListeners.get(conversationId)) {
                    listener.onTypingIndicator(userId, isTyping);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing typing indicator", e);
        }
    }

    private void handleMessageStatus(JsonObject json) {
        try {
            Long messageId = json.get("messageId").getAsLong();
            String status = json.get("status").getAsString();
            Long conversationId = json.get("conversationId").getAsLong();

            if (chatListeners.containsKey(conversationId)) {
                for (ChatListener listener : chatListeners.get(conversationId)) {
                    if ("DELIVERED".equals(status)) {
                        listener.onMessageDelivered(messageId);
                    } else if ("READ".equals(status)) {
                        listener.onMessageRead(messageId);
                    }
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing message status", e);
        }
    }

    private void handleLocationUpdate(JsonObject json) {
        try {
            Long userId = json.get("userId").getAsLong();
            double latitude = json.get("latitude").getAsDouble();
            double longitude = json.get("longitude").getAsDouble();

            for (LocationListener listener : locationListeners) {
                listener.onUserLocationUpdate(userId, latitude, longitude);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing location update", e);
        }
    }

    private void handleNearbyUsers(JsonObject json) {
        try {
            Map<Long, Map<String, Object>> nearbyUsers = new HashMap<>();

            if (json.has("users")) {
                JsonObject users = json.getAsJsonObject("users");
                for (String userIdStr : users.keySet()) {
                    Long userId = Long.parseLong(userIdStr);
                    JsonObject userInfo = users.getAsJsonObject(userIdStr);

                    Map<String, Object> userMap = new HashMap<>();
                    userMap.put("latitude", userInfo.get("latitude").getAsDouble());
                    userMap.put("longitude", userInfo.get("longitude").getAsDouble());
                    userMap.put("distance", userInfo.get("distance").getAsDouble());
                    if (userInfo.has("name")) {
                        userMap.put("name", userInfo.get("name").getAsString());
                    }

                    nearbyUsers.put(userId, userMap);
                }
            }

            for (LocationListener listener : locationListeners) {
                listener.onNearbyUsersUpdate(nearbyUsers);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing nearby users", e);
        }
    }

    private void handleUserStatus(JsonObject json) {
        // Handle user online/offline status
        Log.d(TAG, "👤 User status update: " + json.toString());
    }

    private void handleSystemMessage(JsonObject json) {
        // Handle system messages (maintenance, announcements, etc.)
        Log.d(TAG, "🔔 System message: " + json.toString());
    }

    // ===== SENDING MESSAGES =====

    private void sendMessage(String message) {
        if (webSocketClient != null && isConnected) {
            webSocketClient.send(message);
        } else {
            Log.w(TAG, "⚠️ Cannot send message - not connected");
        }
    }

    // ===== CHAT METHODS =====

    public void joinConversation(Long conversationId) {
        JsonObject joinMessage = new JsonObject();
        joinMessage.addProperty("type", "JOIN_CONVERSATION");
        joinMessage.addProperty("conversationId", conversationId);
        joinMessage.addProperty("userId", currentUserId);

        sendMessage(joinMessage.toString());
        Log.d(TAG, "📡 Joined conversation: " + conversationId);
    }

    public void leaveConversation(Long conversationId) {
        JsonObject leaveMessage = new JsonObject();
        leaveMessage.addProperty("type", "LEAVE_CONVERSATION");
        leaveMessage.addProperty("conversationId", conversationId);
        leaveMessage.addProperty("userId", currentUserId);

        sendMessage(leaveMessage.toString());
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

        sendMessage(chatMessage.toString());
        Log.d(TAG, "📤 Sent chat message to conversation: " + conversationId);
    }

    public void sendTypingIndicator(Long conversationId, boolean isTyping) {
        JsonObject typingMessage = new JsonObject();
        typingMessage.addProperty("type", "TYPING");
        typingMessage.addProperty("conversationId", conversationId);
        typingMessage.addProperty("userId", currentUserId);
        typingMessage.addProperty("isTyping", isTyping);

        sendMessage(typingMessage.toString());
    }

    // ===== LOCATION METHODS =====

    public void sendLocationUpdate(double latitude, double longitude) {
        JsonObject locationMessage = new JsonObject();
        locationMessage.addProperty("type", "LOCATION_UPDATE");
        locationMessage.addProperty("userId", currentUserId);
        locationMessage.addProperty("latitude", latitude);
        locationMessage.addProperty("longitude", longitude);
        locationMessage.addProperty("timestamp", System.currentTimeMillis());

        sendMessage(locationMessage.toString());
        Log.d(TAG, "📍 Sent location update: " + latitude + ", " + longitude);
    }

    public void requestNearbyUsers(double latitude, double longitude, double radiusKm) {
        JsonObject nearbyRequest = new JsonObject();
        nearbyRequest.addProperty("type", "REQUEST_NEARBY_USERS");
        nearbyRequest.addProperty("userId", currentUserId);
        nearbyRequest.addProperty("latitude", latitude);
        nearbyRequest.addProperty("longitude", longitude);
        nearbyRequest.addProperty("radius", radiusKm);

        sendMessage(nearbyRequest.toString());
        Log.d(TAG, "🔍 Requested nearby users within " + radiusKm + "km");
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
        Set<ChatListener> listeners = chatListeners.get(conversationId);
        if (listeners != null) {
            listeners.remove(listener);
            if (listeners.isEmpty()) {
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
            listener.onConnected();
        }
    }

    private void notifyDisconnected() {
        for (WebSocketListener listener : listeners) {
            listener.onDisconnected();
        }
    }

    private void notifyError(String error) {
        for (WebSocketListener listener : listeners) {
            listener.onError(error);
        }
    }

    // ===== PUBLIC GETTERS =====

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public int getReconnectAttempts() {
        return reconnectAttempts;
    }
}