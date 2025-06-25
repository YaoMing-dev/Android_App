package com.example.newtrade.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public class ChatWebSocketClient extends WebSocketClient {
    private static final String TAG = "ChatWebSocketClient";
    private final WebSocketListener listener;
    private boolean isManualClose = false;
    private boolean isConnecting = false; // 🔥 FIX: Add our own connecting state

    public interface WebSocketListener {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public ChatWebSocketClient(URI serverUri, WebSocketListener listener) {
        super(serverUri);
        this.listener = listener;

        // Set connection timeout
        setConnectionLostTimeout(30);
    }

    @Override
    public void connect() {
        isConnecting = true;
        super.connect();
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.d(TAG, "✅ WebSocket connected - Status: " + handshake.getHttpStatus());
        isManualClose = false;
        isConnecting = false; // 🔥 FIX: Update connecting state

        if (listener != null) {
            listener.onConnected();
        }
    }

    @Override
    public void onMessage(String message) {
        Log.d(TAG, "📨 Message received: " + message);

        if (listener != null) {
            listener.onMessageReceived(message);
        }
    }

    @Override
    public void onClose(int code, String reason, boolean remote) {
        Log.d(TAG, "❌ WebSocket closed - Code: " + code + ", Reason: " + reason + ", Remote: " + remote);
        isConnecting = false; // 🔥 FIX: Update connecting state

        if (listener != null) {
            listener.onDisconnected();
        }

        // Auto-reconnect if not manually closed
        if (!isManualClose && !remote) {
            scheduleReconnect();
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "❌ WebSocket error: " + ex.getMessage(), ex);
        isConnecting = false; // 🔥 FIX: Update connecting state

        if (listener != null) {
            listener.onError(ex.getMessage());
        }
    }

    // 🔥 ENHANCED METHODS
    public void sendChatMessage(String message, Long conversationId, Long senderId) {
        if (isOpen()) {
            try {
                // 🔥 FIX: Use Locale.US for String.format
                String jsonMessage = String.format(Locale.US,
                        "{\"conversationId\":%d,\"senderId\":%d,\"messageText\":\"%s\",\"messageType\":\"TEXT\",\"timestamp\":%d}",
                        conversationId, senderId, escapeJson(message), System.currentTimeMillis()
                );
                send(jsonMessage);
                Log.d(TAG, "📤 Message sent: " + jsonMessage);
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send message", e);
                if (listener != null) {
                    listener.onError("Failed to send message: " + e.getMessage());
                }
            }
        } else {
            Log.e(TAG, "❌ WebSocket not connected - cannot send message");
            if (listener != null) {
                listener.onError("WebSocket not connected");
            }
        }
    }

    // 🔥 ADD @SuppressWarnings to avoid unused method warning
    @SuppressWarnings("unused")
    public void sendTypingIndicator(Long conversationId, Long senderId, boolean isTyping) {
        if (isOpen()) {
            try {
                // 🔥 FIX: Use Locale.US for String.format
                String jsonMessage = String.format(Locale.US,
                        "{\"conversationId\":%d,\"senderId\":%d,\"type\":\"%s\",\"timestamp\":%d}",
                        conversationId, senderId, isTyping ? "TYPING_START" : "TYPING_STOP", System.currentTimeMillis()
                );
                send(jsonMessage);
                Log.d(TAG, "📤 Typing indicator sent: " + (isTyping ? "START" : "STOP"));
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send typing indicator", e);
            }
        }
    }

    @SuppressWarnings("unused")
    public void sendReadReceipt(Long conversationId, Long senderId, Long messageId) {
        if (isOpen()) {
            try {
                // 🔥 FIX: Use Locale.US for String.format
                String jsonMessage = String.format(Locale.US,
                        "{\"conversationId\":%d,\"senderId\":%d,\"messageId\":%d,\"type\":\"READ_RECEIPT\",\"timestamp\":%d}",
                        conversationId, senderId, messageId, System.currentTimeMillis()
                );
                send(jsonMessage);
                Log.d(TAG, "📤 Read receipt sent for message: " + messageId);
            } catch (Exception e) {
                Log.e(TAG, "❌ Failed to send read receipt", e);
            }
        }
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\"", "\\\"").replace("\n", "\\n").replace("\r", "\\r");
    }

    private void scheduleReconnect() {
        new Thread(() -> {
            try {
                Thread.sleep(TimeUnit.SECONDS.toMillis(3)); // Wait 3 seconds
                if (!isOpen() && !isManualClose) {
                    Log.d(TAG, "🔄 Attempting to reconnect WebSocket...");
                    reconnect();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }).start();
    }

    @Override
    public void close() {
        isManualClose = true;
        isConnecting = false;
        super.close();
    }

    // Status methods
    public boolean isConnected() {
        return isOpen();
    }

    // 🔥 FIX: Use our own isConnecting state instead of non-existent method
    public String getConnectionStatus() {
        if (isOpen()) {
            return "Connected";
        } else if (isConnecting) {
            return "Connecting...";
        } else if (isClosed()) {
            return "Disconnected";
        } else {
            return "Unknown";
        }
    }

    // 🔥 ADD: Helper method to check if we're connecting
    public boolean isConnecting() {
        return isConnecting;
    }
}