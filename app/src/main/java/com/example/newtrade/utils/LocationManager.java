// app/src/main/java/com/example/newtrade/utils/LocationManager.java
package com.example.newtrade.utils;

import android.content.Context;
import android.location.Location;

public class LocationManager {
    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    private Context context;
    private LocationCallback callback;

    public LocationManager(Context context, LocationCallback callback) {
        this.context = context;
        this.callback = callback;
    }

    public void getCurrentLocation() {
        // TODO: Implement location getting
        // For now, just call error
        if (callback != null) {
            callback.onLocationError("Location not implemented yet");
        }
    }

    public void cleanup() {
        // TODO: Cleanup location resources
    }
}