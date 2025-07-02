// app/src/main/java/com/example/newtrade/ui/location/RealtimeLocationActivity.java
package com.example.newtrade.ui.location;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.example.newtrade.R;
import com.example.newtrade.utils.NavigationUtils;
import com.example.newtrade.websocket.LocationWebSocketManager; // ✅ NEW: Use LocationWebSocketManager
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.Map;

public class RealtimeLocationActivity extends AppCompatActivity implements
        LocationWebSocketManager.LocationListener { // ✅ FIX: Use new interface

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

    // WebSocket Manager
    private LocationWebSocketManager locationWebSocketManager; // ✅ FIX: Use new manager

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_realtime_location);

        initViews();
        setupToolbar();
        initLocationServices();
        initWebSocketManager(); // ✅ FIX: Use new method

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

    // ✅ FIX: New method to init WebSocket manager
    private void initWebSocketManager() {
        locationWebSocketManager = LocationWebSocketManager.getInstance();
        locationWebSocketManager.addLocationListener(this);
        Log.d(TAG, "✅ LocationWebSocketManager initialized");
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
        if (locationWebSocketManager != null && locationWebSocketManager.isConnected()) {
            locationWebSocketManager.sendLocationUpdate(location.getLatitude(), location.getLongitude());

            // Request nearby users
            locationWebSocketManager.requestNearbyUsers(location.getLatitude(), location.getLongitude(), 5.0); // 5km radius
        }

        Log.d(TAG, "📍 Location updated: " + location.getLatitude() + ", " + location.getLongitude());
    }

    // ===== LocationWebSocketManager.LocationListener Implementation =====

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
                            .append(distance != null ? String.format("%.1fkm", distance) : "Unknown distance")
                            .append(")\n");
                }
            }

            tvNearbyUsers.setText(nearbyText.toString());
        });
    }

    // ===== PERMISSION HANDLING =====

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == LOCATION_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startLocationUpdates();
            } else {
                Toast.makeText(this, "Location permission required for this feature", Toast.LENGTH_LONG).show();
                tvLocationStatus.setText("❌ Location permission denied");
            }
        }
    }

    // ===== LIFECYCLE =====

    @Override
    protected void onResume() {
        super.onResume();
        if (hasLocationPermission()) {
            startLocationUpdates();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopLocationUpdates();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        stopLocationUpdates();

        if (locationWebSocketManager != null) {
            locationWebSocketManager.removeLocationListener(this);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}