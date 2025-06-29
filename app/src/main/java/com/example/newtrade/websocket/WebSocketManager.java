package com.example.newtrade.websocket;

import android.util.Log;
import com.example.newtrade.utils.Constants;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONObject;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class WebSocketManager {
    private static final String TAG = "WebSocketManager";
    private static final String WS_URL = Constants.WS_BASE_URL + "/ws/chat";
    private static WebSocketManager instance;
    private WebSocketClient webSocketClient;
    private final List<WebSocketListener> listeners = new ArrayList<>();
    private String authToken;

    public interface WebSocketListener {
        void onMessage(String message);
        void onConnectionStateChange(boolean connected);
    }

    private WebSocketManager() {}

    public static synchronized WebSocketManager getInstance() {
        if (instance == null) {
            instance = new WebSocketManager();
        }
        return instance;
    }

    public void init(String token) {
        this.authToken = token;
        connect();
    }

    public void connect() {
        try {
            URI uri = URI.create(WS_URL);
            webSocketClient = new WebSocketClient(uri) {
                @Override
                public void onOpen(ServerHandshake handshake) {
                    Log.d(TAG, "WebSocket Connected");
                    // Send authentication message
                    try {
                        JSONObject auth = new JSONObject();
                        auth.put("type", "AUTH");
                        auth.put("token", authToken);
                        send(auth.toString());
                    } catch (Exception e) {
                        Log.e(TAG, "Auth error", e);
                    }
                    notifyConnectionChange(true);
                }

                @Override
                public void onMessage(String message) {
                    Log.d(TAG, "Received message: " + message);
                    notifyMessage(message);
                }

                @Override
                public void onClose(int code, String reason, boolean remote) {
                    Log.d(TAG, "WebSocket Closed: " + reason);
                    notifyConnectionChange(false);
                }

                @Override
                public void onError(Exception ex) {
                    Log.e(TAG, "WebSocket Error", ex);
                    notifyConnectionChange(false);
                }
            };
            webSocketClient.connect();
        } catch (Exception e) {
            Log.e(TAG, "WebSocket Init Error", e);
        }
    }

    public void disconnect() {
        if (webSocketClient != null && webSocketClient.isOpen()) {
            webSocketClient.close();
        }
    }

    public void sendMessage(String chatId, String message) {
        try {
            JSONObject messageObj = new JSONObject();
            messageObj.put("type", "MESSAGE");
            messageObj.put("chatId", chatId);
            messageObj.put("content", message);
            webSocketClient.send(messageObj.toString());
        } catch (Exception e) {
            Log.e(TAG, "Send message error", e);
        }
    }

    public void addListener(WebSocketListener listener) {
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(WebSocketListener listener) {
        listeners.remove(listener);
    }

    private void notifyMessage(String message) {
        for (WebSocketListener listener : listeners) {
            listener.onMessage(message);
        }
    }

    private void notifyConnectionChange(boolean connected) {
        for (WebSocketListener listener : listeners) {
            listener.onConnectionStateChange(connected);
        }
    }

    public boolean isConnected() {
        return webSocketClient != null && webSocketClient.isOpen();
    }
}
