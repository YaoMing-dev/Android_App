// app/src/main/java/com/example/newtrade/utils/LocationManager.java
package com.example.newtrade.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class LocationManager {
    private static final String TAG = "LocationManager";
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    public interface LocationCallback {
        void onLocationReceived(Location location);
        void onLocationError(String error);
    }

    private Context context;
    private LocationCallback callback;
    private android.location.LocationManager locationManager;
    private LocationListener locationListener;

    public LocationManager(Context context, LocationCallback callback) {
        this.context = context;
        this.callback = callback;
        this.locationManager = (android.location.LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    public void requestLocationPermission() {
        if (context instanceof AppCompatActivity) {
            ActivityCompat.requestPermissions((AppCompatActivity) context,
                    new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    },
                    LOCATION_PERMISSION_REQUEST_CODE);
        }
    }

    public void getCurrentLocation() {
        if (!hasLocationPermission()) {
            if (callback != null) {
                callback.onLocationError("Location permission not granted");
            }
            return;
        }

        if (locationManager == null) {
            if (callback != null) {
                callback.onLocationError("Location service not available");
            }
            return;
        }

        try {
            // Try to get last known location first
            Location lastKnownLocation = getLastKnownLocation();
            if (lastKnownLocation != null) {
                if (callback != null) {
                    callback.onLocationReceived(lastKnownLocation);
                }
                return;
            }

            // Request new location
            locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    Log.d(TAG, "Location received: " + location.getLatitude() + ", " + location.getLongitude());
                    if (callback != null) {
                        callback.onLocationReceived(location);
                    }
                    removeLocationUpdates();
                }

                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}

                @Override
                public void onProviderEnabled(String provider) {}

                @Override
                public void onProviderDisabled(String provider) {
                    if (callback != null) {
                        callback.onLocationError("Location provider disabled");
                    }
                }
            };

            // Request location updates
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                locationManager.requestLocationUpdates(
                        android.location.LocationManager.GPS_PROVIDER,
                        5000,
                        10,
                        locationListener
                );
            }

        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
            if (callback != null) {
                callback.onLocationError("Failed to get location: " + e.getMessage());
            }
        }
    }

    private Location getLastKnownLocation() {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location gpsLocation = locationManager.getLastKnownLocation(android.location.LocationManager.GPS_PROVIDER);
                Location networkLocation = locationManager.getLastKnownLocation(android.location.LocationManager.NETWORK_PROVIDER);

                if (gpsLocation != null && networkLocation != null) {
                    return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
                }
                return gpsLocation != null ? gpsLocation : networkLocation;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting last known location", e);
        }
        return null;
    }

    private void removeLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            locationManager.removeUpdates(locationListener);
            locationListener = null;
        }
    }

    public void cleanup() {
        removeLocationUpdates();
    }
}