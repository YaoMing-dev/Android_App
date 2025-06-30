// app/src/main/java/com/example/newtrade/utils/LocationManager.java
package com.example.newtrade.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;

public class LocationManager {
    private static final String TAG = "LocationManager";
    private static final long UPDATE_INTERVAL = 10000; // 10 seconds
    private static final long FASTEST_INTERVAL = 5000; // 5 seconds
    private static final long TIMEOUT = 30000; // 30 seconds

    private Context context;
    private LocationCallback locationCallback;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest locationRequest;
    private com.google.android.gms.location.LocationCallback googleLocationCallback;

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    public LocationManager(Context context, LocationCallback callback) {
        this.context = context.getApplicationContext();
        this.locationCallback = callback;

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);
        createLocationRequest();
        createLocationCallback();
    }

    private void createLocationRequest() {
        locationRequest = new LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, UPDATE_INTERVAL)
                .setWaitForAccurateLocation(false)
                .setMinUpdateIntervalMillis(FASTEST_INTERVAL)
                .setMaxUpdateDelayMillis(UPDATE_INTERVAL * 2)
                .build();
    }

    private void createLocationCallback() {
        googleLocationCallback = new com.google.android.gms.location.LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                if (locationResult == null) {
                    if (locationCallback != null) {
                        locationCallback.onLocationError("No location result received");
                    }
                    return;
                }

                Location lastLocation = locationResult.getLastLocation();
                if (lastLocation != null) {
                    Log.d(TAG, "Location received: " + lastLocation.getLatitude() + ", " + lastLocation.getLongitude());

                    // Stop location updates after getting first result
                    stopLocationUpdates();

                    if (locationCallback != null) {
                        locationCallback.onLocationReceived(lastLocation);
                    }
                } else if (locationCallback != null) {
                    locationCallback.onLocationError("Location is null");
                }
            }
        };
    }

    public void requestLocation() {
        if (!hasLocationPermission()) {
            if (locationCallback != null) {
                locationCallback.onLocationError("Location permissions not granted");
            }
            return;
        }

        try {
            // First try to get last known location
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            // Location is recent enough, use it
                            long locationAge = System.currentTimeMillis() - location.getTime();
                            if (locationAge < 5 * 60 * 1000) { // 5 minutes
                                Log.d(TAG, "Using cached location");
                                if (locationCallback != null) {
                                    locationCallback.onLocationReceived(location);
                                }
                                return;
                            }
                        }

                        // Need fresh location
                        requestLocationUpdates();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last location", e);
                        requestLocationUpdates();
                    });

        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied", e);
            if (locationCallback != null) {
                locationCallback.onLocationError("Location permission denied");
            }
        }
    }

    private void requestLocationUpdates() {
        if (!hasLocationPermission()) {
            if (locationCallback != null) {
                locationCallback.onLocationError("Location permissions not granted");
            }
            return;
        }

        try {
            Log.d(TAG, "Requesting location updates");
            fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    googleLocationCallback,
                    Looper.getMainLooper()
            );

            // Set timeout to stop location updates if no result
            android.os.Handler handler = new android.os.Handler(Looper.getMainLooper());
            handler.postDelayed(() -> {
                stopLocationUpdates();
                if (locationCallback != null) {
                    locationCallback.onLocationError("Location request timeout");
                }
            }, TIMEOUT);

        } catch (SecurityException e) {
            Log.e(TAG, "Location permission denied during updates", e);
            if (locationCallback != null) {
                locationCallback.onLocationError("Location permission denied");
            }
        }
    }

    public void stopLocationUpdates() {
        if (fusedLocationClient != null && googleLocationCallback != null) {
            fusedLocationClient.removeLocationUpdates(googleLocationCallback);
            Log.d(TAG, "Location updates stopped");
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    public boolean isLocationEnabled() {
        try {
            android.location.LocationManager locationManager = (android.location.LocationManager)
                    context.getSystemService(Context.LOCATION_SERVICE);

            return locationManager != null &&
                    (locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER) ||
                            locationManager.isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER));
        } catch (Exception e) {
            Log.e(TAG, "Error checking location enabled", e);
            return false;
        }
    }

    public void cleanup() {
        stopLocationUpdates();
        locationCallback = null;
    }

    // Static utility methods
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in km

        return distance;
    }

    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format("%.0f m", distanceKm * 1000);
        } else if (distanceKm < 10) {
            return String.format("%.1f km", distanceKm);
        } else {
            return String.format("%.0f km", distanceKm);
        }
    }
}