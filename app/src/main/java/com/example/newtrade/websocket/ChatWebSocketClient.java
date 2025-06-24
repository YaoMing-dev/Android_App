package com.example.newtrade.websocket;

import android.util.Log;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;

import java.net.URI;

public class ChatWebSocketClient extends WebSocketClient {
    private static final String TAG = "ChatWebSocketClient";
    private final WebSocketListener listener;

    public interface WebSocketListener {
        void onMessageReceived(String message);
        void onConnected();
        void onDisconnected();
        void onError(String error);
    }

    public ChatWebSocketClient(URI serverUri, WebSocketListener listener) {
        super(serverUri);
        this.listener = listener;
    }

    @Override
    public void onOpen(ServerHandshake handshake) {
        Log.d(TAG, "✅ WebSocket connected");
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
        Log.d(TAG, "❌ WebSocket disconnected: " + reason);
        if (listener != null) {
            listener.onDisconnected();
        }
    }

    @Override
    public void onError(Exception ex) {
        Log.e(TAG, "❌ WebSocket error", ex);
        if (listener != null) {
            listener.onError(ex.getMessage());
        }
    }

    public void sendChatMessage(String message, Long conversationId, Long senderId) {
        if (isOpen()) {
            String jsonMessage = String.format(
                    "{\"conversationId\":%d,\"senderId\":%d,\"messageText\":\"%s\",\"messageType\":\"TEXT\"}",
                    conversationId, senderId, message
            );
            send(jsonMessage);
            Log.d(TAG, "📤 Message sent: " + jsonMessage);
        } else {
            Log.e(TAG, "❌ WebSocket not connected");
        }
    }
}