// app/src/main/java/com/example/newtrade/utils/LocationManager.java
package com.example.newtrade.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationManager {
    private static final String TAG = "LocationManager";

    private Context context;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private LocationCallback callback;
    private boolean isRequestingLocation = false;

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    public LocationManager(Context context, LocationCallback callback) {
        this.context = context;
        this.callback = callback;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        locationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);

                if (locationResult != null && !locationResult.getLocations().isEmpty()) {
                    Location location = locationResult.getLastLocation();
                    Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());

                    stopLocationUpdates();
                    if (LocationManager.this.callback != null) {
                        LocationManager.this.callback.onLocationReceived(location);
                    }
                } else {
                    if (LocationManager.this.callback != null) {
                        LocationManager.this.callback.onLocationError("No location data received");
                    }
                }
            }
        };
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        if (context instanceof Activity) {
            ActivityCompat.requestPermissions((Activity) context,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    Constants.REQUEST_LOCATION_PERMISSION);
        }
    }

    public void getCurrentLocation() {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        if (isRequestingLocation) {
            Log.d(TAG, "Location request already in progress");
            return;
        }

        try {
            // First try to get last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Use cached location if it's recent (within 5 minutes)
                            long locationAge = System.currentTimeMillis() - location.getTime();
                            if (locationAge < 5 * 60 * 1000) { // 5 minutes
                                Log.d(TAG, "Using cached location");
                                callback.onLocationReceived(location);
                                return;
                            }
                        }

                        // Request fresh location
                        requestLocationUpdates();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last location", e);
                        requestLocationUpdates();
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location", e);
            callback.onLocationError("Location permission denied");
        }
    }

    private void requestLocationUpdates() {
        if (!hasLocationPermission()) {
            callback.onLocationError("Location permission not granted");
            return;
        }

        LocationRequest locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 10000)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(5000)
                .setMaxUpdateDelayMillis(15000)
                .build();

        try {
            isRequestingLocation = true;
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper());

            // Set timeout for location request
            new android.os.Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (isRequestingLocation) {
                    stopLocationUpdates();
                    callback.onLocationError("Location request timeout");
                }
            }, 30000); // 30 seconds timeout

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location updates", e);
            isRequestingLocation = false;
            callback.onLocationError("Location permission denied");
        }
    }

    private void stopLocationUpdates() {
        if (isRequestingLocation) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            isRequestingLocation = false;
        }
    }

    public void cleanup() {
        stopLocationUpdates();
    }

    // Helper method to calculate distance between two points
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // convert to kilometers

        return distance;
    }
}