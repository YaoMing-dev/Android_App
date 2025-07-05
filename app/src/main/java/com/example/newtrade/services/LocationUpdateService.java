// app/src/main/java/com/example/newtrade/services/LocationUpdateService.java
package com.example.newtrade.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class LocationUpdateService extends Service {

    private static final String TAG = "LocationUpdateService";

    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "LocationUpdateService created");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "LocationUpdateService started");
        return START_STICKY;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d(TAG, "LocationUpdateService destroyed");
    }
}