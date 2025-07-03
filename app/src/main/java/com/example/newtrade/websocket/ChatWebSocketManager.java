// app/src/main/java/com/example/newtrade/websocket/ChatWebSocketManager.java
package com.example.newtrade.websocket;

import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompClient;
import ua.naiksoftware.stomp.dto.LifecycleEvent;
import ua.naiksoftware.stomp.dto.StompHeader;
import ua.naiksoftware.stomp.dto.StompMessage;

public class ChatWebSocketManager {
    private static final String TAG = "ChatWebSocketManager";
    private static ChatWebSocketManager instance;

    // ✅ REPLACED: WebSocketClient with StompClient
    private StompClient stompClient;
    private CompositeDisposable compositeDisposable;
    private boolean isConnected = false;
    private boolean isConnecting = false;
    private Long currentUserId;
    private Long currentConversationId;
    private Gson gson = new Gson();

    // Listeners - keep same interface
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

    private ChatWebSocketManager() {
        compositeDisposable = new CompositeDisposable();
    }

    public void connect(Long userId) {
        if (isConnected || isConnecting || userId == null || userId <= 0) {
            Log.d(TAG, "Already connected/connecting or invalid userId: " + userId);
            return;
        }

        this.currentUserId = userId;
        isConnecting = true;

        try {
            // ✅ UPDATED: Use STOMP instead of raw WebSocket
            String wsUrl = "ws://10.0.2.2:8080/ws";
            Log.d(TAG, "🔄 Connecting STOMP to: " + wsUrl + " for user: " + userId);

            // Disconnect existing connection if any
            disconnect();

            // ✅ Create STOMP client
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, wsUrl);

            // ✅ Setup headers
            List<StompHeader> headers = new ArrayList<>();
            headers.add(new StompHeader("User-ID", String.valueOf(userId)));

            // ✅ Subscribe to lifecycle events
            compositeDisposable.add(
                    stompClient.lifecycle()
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleLifecycleEvent, this::handleError)
            );

            // ✅ Connect with headers
            stompClient.connect(headers);

        } catch (Exception e) {
            Log.e(TAG, "❌ Failed to create STOMP connection", e);
            isConnecting = false;
            notifyError("Failed to connect: " + e.getMessage());
        }
    }

    // ✅ NEW: Handle STOMP lifecycle events
    private void handleLifecycleEvent(LifecycleEvent lifecycleEvent) {
        switch (lifecycleEvent.getType()) {
            case OPENED:
                Log.d(TAG, "✅ STOMP connected");
                isConnected = true;
                isConnecting = false;
                notifyConnected();
                subscribeToTopics();

                // Join conversation if available
                if (currentConversationId != null) {
                    joinConversation(currentConversationId);
                }
                break;

            case CLOSED:
                Log.d(TAG, "❌ STOMP disconnected");
                isConnected = false;
                isConnecting = false;
                notifyDisconnected();

                // Auto reconnect
                if (currentUserId != null) {
                    scheduleReconnect();
                }
                break;

            case ERROR:
                Log.e(TAG, "❌ STOMP error", lifecycleEvent.getException());
                isConnected = false;
                isConnecting = false;
                String errorMsg = lifecycleEvent.getException() != null ?
                        lifecycleEvent.getException().getMessage() : "Unknown error";
                notifyError("Connection error: " + errorMsg);
                break;
        }
    }

    // ✅ NEW: Handle subscription errors
    private void handleError(Throwable throwable) {
        Log.e(TAG, "❌ STOMP subscription error", throwable);
        isConnected = false;
        isConnecting = false;
        notifyError("Subscription error: " + throwable.getMessage());
    }

    // ✅ NEW: Subscribe to STOMP topics
    private void subscribeToTopics() {
        if (!isConnected || stompClient == null) {
            Log.w(TAG, "Cannot subscribe - not connected");
            return;
        }

        try {
            // Subscribe to personal notifications
            compositeDisposable.add(
                    stompClient.topic("/user/queue/notifications")
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleNotificationMessage, this::handleError)
            );

            Log.d(TAG, "✅ Subscribed to STOMP topics");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error subscribing to topics", e);
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

        if (!isConnected || stompClient == null) {
            Log.w(TAG, "Not connected, will join when connected");
            return;
        }

        try {
            // ✅ Subscribe to conversation messages
            compositeDisposable.add(
                    stompClient.topic("/topic/conversation/" + conversationId)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(this::handleChatMessage, this::handleError)
            );

            // ✅ Send join message
            JsonObject joinMessage = new JsonObject();
            joinMessage.addProperty("conversationId", conversationId);
            joinMessage.addProperty("userId", currentUserId);

            sendToDestination("/app/join/conversation/" + conversationId, joinMessage.toString());

            Log.d(TAG, "📡 Joined conversation: " + conversationId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error joining conversation", e);
        }
    }

    public void leaveConversation() {
        if (!isConnected || currentConversationId == null || stompClient == null) return;

        try {
            JsonObject leaveMessage = new JsonObject();
            leaveMessage.addProperty("conversationId", currentConversationId);
            leaveMessage.addProperty("userId", currentUserId);

            sendToDestination("/app/leave/conversation/" + currentConversationId, leaveMessage.toString());
            Log.d(TAG, "📡 Left conversation: " + currentConversationId);

            currentConversationId = null;

        } catch (Exception e) {
            Log.e(TAG, "❌ Error leaving conversation", e);
        }
    }

    public void sendChatMessage(String messageText) {
        if (!isConnected || currentConversationId == null || stompClient == null) {
            Log.w(TAG, "Cannot send message - not connected or no conversation");
            notifyError("Cannot send message - not connected");
            return;
        }

        try {
            JsonObject message = new JsonObject();
            message.addProperty("conversationId", currentConversationId);
            message.addProperty("senderId", currentUserId);
            message.addProperty("messageText", messageText);
            message.addProperty("messageType", "TEXT");

            sendToDestination("/app/send/message", message.toString());
            Log.d(TAG, "📤 Sent message to conversation: " + currentConversationId);

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending message", e);
            notifyError("Failed to send message");
        }
    }

    public void sendTypingIndicator(boolean isTyping) {
        if (!isConnected || currentConversationId == null || stompClient == null) return;

        try {
            JsonObject typing = new JsonObject();
            typing.addProperty("conversationId", currentConversationId);
            typing.addProperty("userId", currentUserId);
            typing.addProperty("isTyping", isTyping);

            sendToDestination("/app/send/typing", typing.toString());

        } catch (Exception e) {
            Log.e(TAG, "❌ Error sending typing indicator", e);
        }
    }

    // ✅ NEW: Send message to STOMP destination
    private void sendToDestination(String destination, String message) {
        if (stompClient != null && isConnected) {
            compositeDisposable.add(
                    stompClient.send(destination, message)
                            .subscribeOn(Schedulers.io())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe(
                                    () -> Log.d(TAG, "✅ Message sent to: " + destination),
                                    throwable -> {
                                        Log.e(TAG, "❌ Failed to send message to: " + destination, throwable);
                                        notifyError("Failed to send message");
                                    }
                            )
            );
        }
    }

    // ✅ NEW: Handle incoming chat messages
    private void handleChatMessage(StompMessage stompMessage) {
        try {
            String payload = stompMessage.getPayload();
            Log.d(TAG, "📨 Received chat message: " + payload);

            JsonObject messageJson = gson.fromJson(payload, JsonObject.class);
            notifyMessageReceived(messageJson);

        } catch (JsonSyntaxException e) {
            Log.e(TAG, "❌ Error parsing chat message JSON", e);
        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling chat message", e);
        }
    }

    // ✅ NEW: Handle notifications
    private void handleNotificationMessage(StompMessage stompMessage) {
        try {
            String payload = stompMessage.getPayload();
            Log.d(TAG, "🔔 Received notification: " + payload);

            // Handle notifications here if needed

        } catch (Exception e) {
            Log.e(TAG, "❌ Error handling notification", e);
        }
    }

    public void disconnect() {
        try {
            Log.d(TAG, "🔌 Disconnecting...");

            // ✅ Clear all subscriptions
            if (compositeDisposable != null && !compositeDisposable.isDisposed()) {
                compositeDisposable.clear();
            }

            // ✅ Disconnect STOMP client
            if (stompClient != null) {
                if (stompClient.isConnected()) {
                    stompClient.disconnect();
                }
                stompClient = null;
            }

            // Reset state
            isConnected = false;
            isConnecting = false;
            currentConversationId = null;

            Log.d(TAG, "✅ Disconnected");

        } catch (Exception e) {
            Log.e(TAG, "❌ Error disconnecting", e);
        }
    }

    // ===== LISTENER MANAGEMENT (unchanged) =====

    public void addChatListener(ChatListener listener) {
        if (listener != null) {
            chatListeners.add(listener);
        }
    }

    public void removeChatListener(ChatListener listener) {
        chatListeners.remove(listener);
    }

    private void notifyConnected() {
        for (ChatListener listener : chatListeners) {
            try {
                listener.onConnected();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener onConnected", e);
            }
        }
    }

    private void notifyDisconnected() {
        for (ChatListener listener : chatListeners) {
            try {
                listener.onDisconnected();
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener onDisconnected", e);
            }
        }
    }

    private void notifyMessageReceived(JsonObject message) {
        for (ChatListener listener : chatListeners) {
            try {
                listener.onMessageReceived(message);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener onMessageReceived", e);
            }
        }
    }

    private void notifyError(String error) {
        for (ChatListener listener : chatListeners) {
            try {
                listener.onError(error);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying listener onError", e);
            }
        }
    }

    // ===== GETTERS (unchanged) =====

    public boolean isConnected() {
        return isConnected;
    }

    public boolean isConnecting() {
        return isConnecting;
    }

    public Long getCurrentConversationId() {
        return currentConversationId;
    }

    public Long getCurrentUserId() {
        return currentUserId;
    }
}