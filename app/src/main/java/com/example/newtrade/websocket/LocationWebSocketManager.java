// app/src/main/java/com/example/newtrade/websocket/LocationWebSocketManager.java
package com.example.newtrade.websocket;

import android.util.Log;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class LocationWebSocketManager {
    private static final String TAG = "LocationWebSocketManager";
    private static LocationWebSocketManager instance;

    // Listeners
    private final Set<LocationListener> locationListeners = ConcurrentHashMap.newKeySet();

    public interface LocationListener {
        void onUserLocationUpdate(Long userId, double latitude, double longitude);
        void onNearbyUsersUpdate(Map<Long, Map<String, Object>> nearbyUsers);
    }

    public static synchronized LocationWebSocketManager getInstance() {
        if (instance == null) {
            instance = new LocationWebSocketManager();
        }
        return instance;
    }

    private LocationWebSocketManager() {}

    public void addLocationListener(LocationListener listener) {
        locationListeners.add(listener);
        Log.d(TAG, "✅ Added location listener");
    }

    public void removeLocationListener(LocationListener listener) {
        locationListeners.remove(listener);
        Log.d(TAG, "➖ Removed location listener");
    }

    public void sendLocationUpdate(double latitude, double longitude) {
        // TODO: Implement location update via ChatWebSocketManager
        Log.d(TAG, "📍 Sending location update: " + latitude + ", " + longitude);
    }

    public void requestNearbyUsers(double latitude, double longitude, double radiusKm) {
        // TODO: Implement nearby users request
        Log.d(TAG, "👥 Requesting nearby users within " + radiusKm + "km");
    }

    public boolean isConnected() {
        // Delegate to ChatWebSocketManager
        return ChatWebSocketManager.getInstance().isConnected();
    }

    // Notify listeners (for future use)
    private void notifyLocationUpdate(Long userId, double latitude, double longitude) {
        for (LocationListener listener : locationListeners) {
            try {
                listener.onUserLocationUpdate(userId, latitude, longitude);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying location listener", e);
            }
        }
    }

    private void notifyNearbyUsers(Map<Long, Map<String, Object>> nearbyUsers) {
        for (LocationListener listener : locationListeners) {
            try {
                listener.onNearbyUsersUpdate(nearbyUsers);
            } catch (Exception e) {
                Log.e(TAG, "Error notifying nearby users listener", e);
            }
        }
    }
}