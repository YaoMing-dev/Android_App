//// app/src/main/java/com/example/newtrade/websocket/ChatWebSocketClient.java
//// ✅ FIXED - No errors version
//package com.example.newtrade.websocket;
//
//import android.util.Log;
//import com.example.newtrade.models.Message;
//import com.example.newtrade.utils.Constants;
//import com.google.gson.Gson;
//import com.google.gson.JsonObject;
//import com.google.gson.JsonSyntaxException;
//import org.java_websocket.client.WebSocketClient;
//import org.java_websocket.handshake.ServerHandshake;
//import java.net.URI;
//
//public class ChatWebSocketClient {
//    private static final String TAG = "ChatWebSocketClient";
//
//    private WebSocketClient webSocketClient;
//    private final WebSocketListener listener;
//    private final Gson gson;
//    private boolean isConnected = false;
//    private final Long currentUserId;
//    private Long currentConversationId;
//
//    public interface WebSocketListener {
//        void onMessageReceived(Message message);
//        void onTypingIndicator(Long userId, boolean isTyping);
//        void onConnectionChanged(boolean connected);
//        void onError(String error);
//    }
//
//    public ChatWebSocketClient(Long userId, WebSocketListener listener) {
//        this.currentUserId = userId;
//        this.listener = listener;
//        this.gson = new Gson();
//    }
//
//    public void connect() {
//        if (isConnected) {
//            Log.d(TAG, "Already connected");
//            return;
//        }
//
//        try {
//            String wsUrl = Constants.WS_BASE_URL + "/ws";
//            Log.d(TAG, "Connecting to: " + wsUrl);
//
//            URI serverUri = URI.create(wsUrl);
//
//            webSocketClient = new WebSocketClient(serverUri) {
//                @Override
//                public void onOpen(ServerHandshake handshake) {
//                    Log.d(TAG, "✅ WebSocket connected");
//                    isConnected = true;
//                    if (listener != null) {
//                        listener.onConnectionChanged(true);
//                    }
//                }
//
//                @Override
//                public void onMessage(String message) {
//                    Log.d(TAG, "📨 Received: " + message);
//                    handleMessage(message);
//                }
//
//                @Override
//                public void onClose(int code, String reason, boolean remote) {
//                    Log.d(TAG, "❌ WebSocket closed: " + reason);
//                    isConnected = false;
//                    if (listener != null) {
//                        listener.onConnectionChanged(false);
//                    }
//                }
//
//                @Override
//                public void onError(Exception ex) {
//                    Log.e(TAG, "❌ WebSocket error", ex);
//                    isConnected = false;
//                    if (listener != null) {
//                        listener.onError(ex.getMessage());
//                    }
//                }
//            };
//
//            webSocketClient.connect();
//
//        } catch (Exception e) {
//            Log.e(TAG, "❌ Connection failed", e);
//            if (listener != null) {
//                listener.onError("Connection failed: " + e.getMessage());
//            }
//        }
//    }
//
//    private void handleMessage(String rawMessage) {
//        try {
//            // ✅ FIX: Use gson.fromJson instead of JsonParser.parseString
//            JsonObject json = gson.fromJson(rawMessage, JsonObject.class);
//
//            if (json.has("type")) {
//                String type = json.get("type").getAsString();
//
//                switch (type) {
//                    case "NEW_MESSAGE":
//                        handleNewMessage(json);
//                        break;
//                    case "TYPING":
//                        handleTyping(json);
//                        break;
//                    default:
//                        Log.d(TAG, "Unknown message type: " + type);
//                }
//            }
//
//        } catch (JsonSyntaxException e) {
//            Log.e(TAG, "❌ Error parsing JSON message", e);
//        } catch (Exception e) {
//            Log.e(TAG, "❌ Error handling message", e);
//        }
//    }
//
//    private void handleNewMessage(JsonObject json) {
//        try {
//            // ✅ FIX: Create Message manually instead of using gson.fromJson
//            Message message = new Message();
//
//            if (json.has("id")) {
//                message.setId(json.get("id").getAsLong());
//            }
//            if (json.has("conversationId")) {
//                message.setConversationId(json.get("conversationId").getAsLong());
//            }
//            if (json.has("senderId")) {
//                message.setSenderId(json.get("senderId").getAsLong());
//            }
//            if (json.has("content")) {
//                message.setContent(json.get("content").getAsString());
//            }
//            if (json.has("timestamp")) {
//                message.setTimestamp(json.get("timestamp").getAsString());
//            }
//
//            // Set default message type
//            message.setMessageType("TEXT");
//
//            if (listener != null) {
//                listener.onMessageReceived(message);
//            }
//
//        } catch (Exception e) {
//            Log.e(TAG, "❌ Error parsing new message", e);
//        }
//    }
//
//    private void handleTyping(JsonObject json) {
//        try {
//            Long userId = json.get("userId").getAsLong();
//            boolean isTyping = json.get("isTyping").getAsBoolean();
//
//            if (listener != null) {
//                listener.onTypingIndicator(userId, isTyping);
//            }
//        } catch (Exception e) {
//            Log.e(TAG, "❌ Error parsing typing indicator", e);
//        }
//    }
//
//    public void joinConversation(Long conversationId) {
//        this.currentConversationId = conversationId;
//
//        if (isConnected && conversationId != null) {
//            JsonObject joinMessage = new JsonObject();
//            joinMessage.addProperty("action", "join_conversation");
//            joinMessage.addProperty("conversationId", conversationId);
//            joinMessage.addProperty("userId", currentUserId);
//
//            sendMessage(joinMessage.toString());
//            Log.d(TAG, "📡 Joined conversation: " + conversationId);
//        }
//    }
//
//    public void sendChatMessage(String messageText) {
//        if (!isConnected || currentConversationId == null) {
//            Log.w(TAG, "Cannot send - not connected or no conversation");
//            return;
//        }
//
//        JsonObject message = new JsonObject();
//        message.addProperty("action", "send_message");
//        message.addProperty("conversationId", currentConversationId);
//        message.addProperty("senderId", currentUserId);
//        message.addProperty("content", messageText);
//        message.addProperty("type", "TEXT");
//
//        sendMessage(message.toString());
//        Log.d(TAG, "📤 Sent message to conversation: " + currentConversationId);
//    }
//
//    public void sendTyping(boolean isTyping) {
//        if (!isConnected || currentConversationId == null) return;
//
//        JsonObject typing = new JsonObject();
//        typing.addProperty("action", "typing");
//        typing.addProperty("conversationId", currentConversationId);
//        typing.addProperty("userId", currentUserId);
//        typing.addProperty("isTyping", isTyping);
//
//        sendMessage(typing.toString());
//    }
//
//    private void sendMessage(String message) {
//        if (webSocketClient != null && isConnected) {
//            webSocketClient.send(message);
//        }
//    }
//
//    public void disconnect() {
//        if (webSocketClient != null) {
//            webSocketClient.close();
//            webSocketClient = null;
//        }
//        isConnected = false;
//        Log.d(TAG, "🔌 Disconnected");
//    }
//
//    public boolean isConnected() {
//        return isConnected;
//    }
//}