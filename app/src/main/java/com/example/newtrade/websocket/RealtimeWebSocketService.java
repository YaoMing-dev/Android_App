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
import java.util.concurrent.TimeUnit;

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

    // ===== ✅ IMPROVED WEBSOCKET CONNECTION =====

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
        Log.d(TAG, "🔗 Starting WebSocket connection...");

        try {
            // ✅ IMPROVED: Better URL construction and headers
            String wsUrl = Constants.WS_BASE_URL + "/ws";
            Log.d(TAG, "🔗 Connecting to STOMP WebSocket: " + wsUrl);

            URI uri = URI.create(wsUrl);

            // ✅ ADD: Connection headers
            Map<String, String> headers = new HashMap<>();
            headers.put("User-ID", String.valueOf(currentUserId));
            headers.put("Origin", "http://localhost");

            webSocketClient = new WebSocketClient(uri, new Draft_6455(), headers, 5000) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    isConnecting = false;
                    reconnectAttempts = 0;

                    Log.d(TAG, "✅ WebSocket connected! Server: " + handshake.getHttpStatusMessage());

                    // Send STOMP connect frame
                    sendStompConnect();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 Received: " + message);
                    handleStompFrame(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    isStompConnected = false;

                    Log.d(TAG, "❌ WebSocket closed: " + reason + " Status line: " + code);

                    notifyDisconnected();

                    // Auto reconnect if not manually closed
                    if (remote && reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    isConnecting = false;
                    Log.e(TAG, "❌ WebSocket error: " + ex.getMessage(), ex);
                    notifyError("Connection error: " + ex.getMessage());

                    // Schedule reconnect on error
                    if (reconnectAttempts < MAX_RECONNECT_ATTEMPTS) {
                        scheduleReconnect();
                    }
                }
            };

            // ✅ IMPROVED: Connect with timeout
            new Thread(() -> {
                try {
                    Log.d(TAG, "🔄 Attempting to connect...");
                    boolean connected = webSocketClient.connectBlocking(10, TimeUnit.SECONDS);

                    if (!connected) {
                        Log.e(TAG, "❌ Connection timeout");
                        isConnecting = false;
                        notifyError("Connection timeout");
                        scheduleReconnect();
                    }
                } catch (InterruptedException e) {
                    Log.e(TAG, "❌ Connection interrupted", e);
                    isConnecting = false;
                    notifyError("Connection interrupted");
                    scheduleReconnect();
                }
            }).start();

        } catch (Exception e) {
            isConnecting = false;
            Log.e(TAG, "❌ Failed to create WebSocket connection", e);
            notifyError("Failed to create connection: " + e.getMessage());
            scheduleReconnect();
        }
    }

    // ✅ IMPROVED: STOMP Protocol Handling
    private void sendStompConnect() {
        StringBuilder connectFrame = new StringBuilder();
        connectFrame.append(STOMP_CONNECT).append("\n");
        connectFrame.append("accept-version:1.0,1.1,2.0").append("\n");
        connectFrame.append("host:").append("10.0.2.2:8080").append("\n");
        connectFrame.append("User-ID:").append(currentUserId).append("\n");
        connectFrame.append("\n");
        connectFrame.append("\u0000");

        sendRawMessage(connectFrame.toString());
        Log.d(TAG, "📡 Sent STOMP CONNECT frame");
    }

    private void handleStompFrame(String frame) {
        try {
            if (frame == null || frame.trim().isEmpty()) return;

            // Remove null terminator
            frame = frame.replace("\u0000", "");

            String[] lines = frame.split("\n");
            if (lines.length == 0) return;

            String command = lines[0].trim();
            Map<String, String> headers = new HashMap<>();
            int bodyStartIndex = -1;

            // Parse headers
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

            // Extract body
            String body = "";
            if (bodyStartIndex > 0 && bodyStartIndex < lines.length) {
                StringBuilder bodyBuilder = new StringBuilder();
                for (int i = bodyStartIndex; i < lines.length; i++) {
                    bodyBuilder.append(lines[i]);
                    if (i < lines.length - 1) bodyBuilder.append("\n");
                }
                body = bodyBuilder.toString().trim();
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

        // Subscribe to user-specific messages
        String userTopic = "/user/" + currentUserId + "/queue/messages";
        subscribeToTopic(userTopic, "user-messages");

        // Subscribe to conversation notifications
        String notificationTopic = "/user/" + currentUserId + "/queue/notifications";
        subscribeToTopic(notificationTopic, "notifications");

        Log.d(TAG, "📡 Subscribed to STOMP topics for user: " + currentUserId);
    }

    private void subscribeToTopic(String destination, String subscriptionId) {
        StringBuilder subscribeFrame = new StringBuilder();
        subscribeFrame.append(STOMP_SUBSCRIBE).append("\n");
        subscribeFrame.append("id:").append(subscriptionId).append("\n");
        subscribeFrame.append("destination:").append(destination).append("\n");
        subscribeFrame.append("\n");
        subscribeFrame.append("\u0000");

        sendRawMessage(subscribeFrame.toString());
        Log.d(TAG, "📡 Subscribed to: " + destination);
    }

    // ===== CONNECTION MANAGEMENT =====

    private void scheduleReconnect() {
        if (reconnectAttempts >= MAX_RECONNECT_ATTEMPTS) {
            Log.e(TAG, "❌ Max reconnect attempts reached");
            notifyError("Connection failed after " + MAX_RECONNECT_ATTEMPTS + " attempts");
            return;
        }

        reconnectAttempts++;
        long delay = RECONNECT_DELAY * reconnectAttempts; // Exponential backoff

        Log.d(TAG, "🔄 Scheduling reconnect attempt " + reconnectAttempts + "/" + MAX_RECONNECT_ATTEMPTS + " in " + delay + "ms");

        reconnectHandler.postDelayed(() -> {
            if (!isConnected && !isConnecting) {
                connect();
            }
        }, delay);
    }

    public void disconnect() {
        reconnectHandler.removeCallbacksAndMessages(null);

        if (webSocketClient != null) {
            try {
                webSocketClient.close();
            } catch (Exception e) {
                Log.e(TAG, "Error closing WebSocket", e);
            }
            webSocketClient = null;
        }

        isConnected = false;
        isConnecting = false;
        isStompConnected = false;
        reconnectAttempts = 0;

        Log.d(TAG, "🔌 WebSocket disconnected");
    }

    // ===== MESSAGING =====

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
        chatMessage.addProperty("conversationId", conversationId);
        chatMessage.addProperty("senderId", currentUserId);
        chatMessage.addProperty("messageText", content);
        chatMessage.addProperty("messageType", messageType != null ? messageType : "TEXT");

        sendStompMessage("/app/chat/" + conversationId + "/send", chatMessage.toString());
        Log.d(TAG, "📡 Sent chat message to conversation: " + conversationId);
    }

    // ===== MESSAGE HANDLING =====

    private void handleStompMessage(Map<String, String> headers, String body) {
        try {
            String destination = headers.get("destination");
            if (destination == null) return;

            if (destination.contains("/queue/messages")) {
                handleChatMessage(body);
            } else if (destination.contains("/queue/notifications")) {
                handleNotification(body);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling STOMP message", e);
        }
    }

    private void handleChatMessage(String messageJson) {
        try {
            // Parse and notify chat listeners
            JsonObject messageObj = gson.fromJson(messageJson, JsonObject.class);

            // Convert to Message object and notify listeners
            // This would depend on your Message model structure

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing chat message", e);
        }
    }

    private void handleNotification(String notificationJson) {
        try {
            // Handle notifications
            Log.d(TAG, "📨 Received notification: " + notificationJson);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error parsing notification", e);
        }
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

    // ===== NOTIFICATIONS =====

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

    // ===== PUBLIC GETTERS =====

    public boolean isConnected() {
        return isConnected && isStompConnected;
    }

    public boolean isConnecting() {
        return isConnecting;
    }
}