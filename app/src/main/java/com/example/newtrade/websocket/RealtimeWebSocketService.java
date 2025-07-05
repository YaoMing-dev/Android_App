// app/src/main/java/com/example/newtrade/websocket/RealtimeWebSocketService.java
package com.example.newtrade.websocket;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class RealtimeWebSocketService extends Service {

    private static final String TAG = "WebSocketService";
    private static final int RECONNECT_DELAY = 5000; // 5 seconds

    // Binder for local service binding
    private final IBinder binder = new LocalBinder();

    // WebSocket client
    private WebSocketClient webSocketClient;
    private boolean isConnected = false;
    private boolean shouldReconnect = true;

    // Utils
    private SharedPrefsManager prefsManager;
    private Gson gson;

    // Listeners
    private final Map<String, WebSocketListener> listeners = new ConcurrentHashMap<>();

    public class LocalBinder extends Binder {
        public RealtimeWebSocketService getService() {
            return RealtimeWebSocketService.this;
        }
    }

    public interface WebSocketListener {
        void onMessageReceived(String type, JsonObject data);
        void onConnectionStatusChanged(boolean connected);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        prefsManager = SharedPrefsManager.getInstance(this);
        gson = new Gson();

        Log.d(TAG, "✅ WebSocket service created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (prefsManager.isLoggedIn()) {
            connect();
        }
        return START_STICKY; // Restart service if killed
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        shouldReconnect = false;
        disconnect();
        Log.d(TAG, "WebSocket service destroyed");
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    // =============================================
    // PUBLIC METHODS
    // =============================================

    public void connect() {
        if (isConnected || webSocketClient != null) {
            Log.d(TAG, "Already connected or connecting");
            return;
        }

        Long userId = prefsManager.getUserId();
        if (userId == null) {
            Log.w(TAG, "❌ Cannot connect: User not logged in");
            return;
        }

        try {
            URI serverUri = new URI(Constants.WS_BASE_URL + "?userId=" + userId);

            webSocketClient = new WebSocketClient(serverUri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    isConnected = true;
                    Log.d(TAG, "✅ WebSocket connected");

                    // Notify listeners
                    for (WebSocketListener listener : listeners.values()) {
                        listener.onConnectionStatusChanged(true);
                    }

                    // Send initial connection message
                    sendConnectionMessage();
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "📨 WebSocket message received: " + message);
                    handleIncomingMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    isConnected = false;
                    Log.d(TAG, "❌ WebSocket disconnected: " + reason);

                    // Notify listeners
                    for (WebSocketListener listener : listeners.values()) {
                        listener.onConnectionStatusChanged(false);
                    }

                    // Attempt to reconnect
                    if (shouldReconnect) {
                        scheduleReconnect();
                    }
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "❌ WebSocket error", ex);
                    isConnected = false;

                    // Notify listeners
                    for (WebSocketListener listener : listeners.values()) {
                        listener.onConnectionStatusChanged(false);
                    }
                }
            };

            webSocketClient.connect();
            Log.d(TAG, "🔄 Connecting to WebSocket...");

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to create WebSocket connection", e);
        }
    }

    public void disconnect() {
        if (webSocketClient != null) {
            webSocketClient.close();
            webSocketClient = null;
        }
        isConnected = false;
        Log.d(TAG, "WebSocket disconnected");
    }

    public void sendMessage(String type, Map<String, Object> data) {
        if (!isConnected || webSocketClient == null) {
            Log.w(TAG, "❌ Cannot send message: WebSocket not connected");
            return;
        }

        try {
            Map<String, Object> message = new HashMap<>();
            message.put("type", type);
            message.put("data", data);
            message.put("timestamp", System.currentTimeMillis());

            String jsonMessage = gson.toJson(message);
            webSocketClient.send(jsonMessage);

            Log.d(TAG, "📤 Message sent: " + type);
        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to send message", e);
        }
    }

    public void joinConversation(Long conversationId) {
        if (conversationId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", conversationId);
        sendMessage("JOIN_CONVERSATION", data);
    }

    public void leaveConversation(Long conversationId) {
        if (conversationId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", conversationId);
        sendMessage("LEAVE_CONVERSATION", data);
    }

    public void sendChatMessage(Long conversationId, String messageText) {
        if (conversationId == null || messageText == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("conversationId", conversationId);
        data.put("messageText", messageText);
        data.put("messageType", "TEXT");
        sendMessage("SEND_MESSAGE", data);
    }

    public void addListener(String key, WebSocketListener listener) {
        listeners.put(key, listener);
    }

    public void removeListener(String key) {
        listeners.remove(key);
    }

    public boolean isConnected() {
        return isConnected;
    }

    // =============================================
    // PRIVATE METHODS
    // =============================================

    private void sendConnectionMessage() {
        Long userId = prefsManager.getUserId();
        if (userId == null) return;

        Map<String, Object> data = new HashMap<>();
        data.put("userId", userId);
        data.put("action", "CONNECT");
        sendMessage("USER_CONNECT", data);
    }

    private void handleIncomingMessage(String message) {
        try {
            JsonObject jsonMessage = gson.fromJson(message, JsonObject.class);
            String type = jsonMessage.get("type").getAsString();
            JsonObject data = jsonMessage.getAsJsonObject("data");

            // Notify all listeners
            for (WebSocketListener listener : listeners.values()) {
                listener.onMessageReceived(type, data);
            }

            // Handle specific message types
            switch (type) {
                case Constants.WS_MESSAGE_TYPE_NEW_MESSAGE:
                    handleNewMessage(data);
                    break;
                case Constants.WS_MESSAGE_TYPE_NOTIFICATION:
                    handleNotification(data);
                    break;
                case Constants.WS_MESSAGE_TYPE_USER_JOINED:
                    handleUserJoined(data);
                    break;
                case Constants.WS_MESSAGE_TYPE_USER_LEFT:
                    handleUserLeft(data);
                    break;
                default:
                    Log.d(TAG, "Unknown message type: " + type);
            }

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to parse incoming message", e);
        }
    }

    private void handleNewMessage(JsonObject data) {
        // Handle new chat message
        Log.d(TAG, "📨 New message received");

        // Send local broadcast for chat activities
        Intent intent = new Intent("NEW_MESSAGE_RECEIVED");
        intent.putExtra("message_data", data.toString());
        sendBroadcast(intent);
    }

    private void handleNotification(JsonObject data) {
        // Handle notification
        Log.d(TAG, "🔔 Notification received");

        // Send local broadcast for notification handling
        Intent intent = new Intent("NOTIFICATION_RECEIVED");
        intent.putExtra("notification_data", data.toString());
        sendBroadcast(intent);
    }

    private void handleUserJoined(JsonObject data) {
        // Handle user joined conversation
        Log.d(TAG, "👤 User joined conversation");
    }

    private void handleUserLeft(JsonObject data) {
        // Handle user left conversation
        Log.d(TAG, "👤 User left conversation");
    }

    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(RECONNECT_DELAY);
                if (shouldReconnect && !isConnected) {
                    Log.d(TAG, "🔄 Attempting to reconnect...");
                    connect();
                }
            } catch (InterruptedException e) {
                Log.e(TAG, "Reconnect interrupted", e);
            }
        }).start();
    }
}