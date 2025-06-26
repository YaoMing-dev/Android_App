// app/src/main/java/com/example/newtrade/ui/location/RealtimeLocationActivity.java
// ✅ GPS Realtime với WebSocket
package com.example.newtrade.ui.location;

import android.Manifest;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.newtrade.R;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.websocket.RealtimeWebSocketService;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Map;

public class RealtimeLocationActivity extends AppCompatActivity implements
        RealtimeWebSocketService.LocationListener {

    private static final String TAG = "RealtimeLocationActivity";
    private static final int LOCATION_PERMISSION_REQUEST = 1001;
    private static final long LOCATION_UPDATE_INTERVAL = 10000; // 10 seconds
    private static final long LOCATION_FASTEST_INTERVAL = 5000; // 5 seconds

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvCurrentLocation;
    private TextView tvNearbyUsers;
    private TextView tvLocationStatus;

    // Location
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private LocationCallback locationCallback;
    private Location lastKnownLocation;

    // WebSocket Service
    private RealtimeWebSocketService webSocketService;
    private boolean isServiceBound = false;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            RealtimeWebSocketService.LocalBinder binder = (RealtimeWebSocketService.LocalBinder) service;
            webSocketService = binder.getService();
            isServiceBound = true;

            webSocketService.addLocationListener(RealtimeLocationActivity.this);

            Log.d(TAG, "✅ WebSocket service connected for location");
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            webSocketService = null;
            isServiceBound = false;
            Log.d(TAG, "❌ WebSocket service disconnected");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_location);

        initViews();
        setupToolbar();
        initLocationServices();
        startAndBindWebSocketService();

        Log.d(TAG, "✅ RealtimeLocationActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvCurrentLocation = findViewById(R.id.tv_current_location);
        tvNearbyUsers = findViewById(R.id.tv_nearby_users);
        tvLocationStatus = findViewById(R.id.tv_location_status);
    }

    private void setupToolbar() {
        NavigationUtils.setupToolbarWithBackButton(this, toolbar, "Live Location");
    }

    private void initLocationServices() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Create location request
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, LOCATION_UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(LOCATION_FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(LOCATION_UPDATE_INTERVAL * 2)
                .build();

        // Location callback
        locationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(@NonNull LocationResult locationResult) {
                super.onLocationResult(locationResult);

                Location location = locationResult.getLastLocation();
                if (location != null) {
                    handleLocationUpdate(location);
                }
            }
        };

        // Request location permission
        if (!hasLocationPermission()) {
            requestLocationPermission();
        } else {
            startLocationUpdates();
        }
    }

    private void startAndBindWebSocketService() {
        Intent serviceIntent = new Intent(this, RealtimeWebSocketService.class);
        startService(serviceIntent);
        bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                LOCATION_PERMISSION_REQUEST);
    }

    private void startLocationUpdates() {
        if (!hasLocationPermission()) {
            return;
        }

        try {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, getMainLooper());
            tvLocationStatus.setText("🟢 Location tracking active");
            Log.d(TAG, "✅ Location updates started");

        } catch (SecurityException e) {
            Log.e(TAG, "❌ Location permission denied", e);
            tvLocationStatus.setText("❌ Location permission denied");
        }
    }

    private void stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback);
        tvLocationStatus.setText("⏸️ Location tracking paused");
        Log.d(TAG, "⏸️ Location updates stopped");
    }

    private void handleLocationUpdate(Location location) {
        lastKnownLocation = location;

        // Update UI
        String locationText = String.format("📍 %.6f, %.6f\nAccuracy: %.1fm",
                location.getLatitude(), location.getLongitude(), location.getAccuracy());
        tvCurrentLocation.setText(locationText);

        // Send to WebSocket
        if (webSocketService != null && webSocketService.isConnected()) {
            webSocketService.sendLocationUpdate(location.getLatitude(), location.getLongitude());

            // Request nearby users
            webSocketService.requestNearbyUsers(location.getLatitude(), location.getLongitude(), 5.0); // 5km radius
        }

        Log.d(TAG, "📍 Location updated: " + location.getLatitude() + ", " + location.getLongitude());
    }

    // ===== Location Listener =====

    @Override
    public void onUserLocationUpdate(Long userId, double latitude, double longitude) {
        runOnUiThread(() -> {
            Log.d(TAG, "👤 User " + userId + " location: " + latitude + ", " + longitude);
            // TODO: Update map or list with user location
        });
    }

    @Override
    public void onNearbyUsersUpdate(Map<Long, Map<String, Object>> nearbyUsers) {
        runOnUiThread(() -> {
            StringBuilder nearbyText = new StringBuilder("👥 Nearby Users:\n\n");

            if (nearbyUsers.isEmpty()) {
                nearbyText.append("No users nearby");
            } else {
                for (Map.Entry<Long, Map<String, Object>> entry : nearbyUsers.entrySet()) {
                    Long userId = entry.getKey();
                    Map<String, Object> userInfo = entry.getValue();

                    String name = (String) userInfo.get("name");
                    Double distance = (Double) userInfo.get("distance");

                    nearbyText.append("• ")
                            .append(name != null ? name : "User " + userId)
                            .append(" (")
                            .append(String.format("%.1f km", distance))
                            .append(")\n");
                }
            }

            tvNearbyUsers.setText(nearbyText.toString());

            Log.d(TAG, "👥 Updated nearby users: " + nearbyUsers.size());
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
                Toast.makeText(this, "✅ Location permission granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "❌ Location permission denied", Toast.LENGTH_LONG).show();
                tvLocationStatus.setText("❌ Location permission required");
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (NavigationUtils.handleBackButton(this, item)) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // Stop location updates
        stopLocationUpdates();

        // Remove location listener
        if (webSocketService != null) {
            webSocketService.removeLocationListener(this);
        }

        // Unbind service
        if (isServiceBound) {
            unbindService(serviceConnection);
            isServiceBound = false;
        }

        Log.d(TAG, "🧹 RealtimeLocationActivity destroyed");
    }
}