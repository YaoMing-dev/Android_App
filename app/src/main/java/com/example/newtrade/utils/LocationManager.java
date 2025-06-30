// app/src/main/java/com/example/newtrade/utils/LocationManager.java
package com.example.newtrade.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager as AndroidLocationManager;
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
    private AndroidLocationManager locationManager;
    private LocationListener locationListener;

    public LocationManager(Context context, LocationCallback callback) {
        this.context = context;
        this.callback = callback;
        this.locationManager = (AndroidLocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    // FR-2.1.3: Location autofill using GPS with permission check
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
                        callback.onLocationError(provider + " provider disabled");
                    }
                }
            };

            // Try GPS first, then network
            if (locationManager.isProviderEnabled(AndroidLocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(AndroidLocationManager.GPS_PROVIDER, locationListener, null);
            } else if (locationManager.isProviderEnabled(AndroidLocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(AndroidLocationManager.NETWORK_PROVIDER, locationListener, null);
            } else {
                if (callback != null) {
                    callback.onLocationError("No location providers available");
                }
            }

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting location", e);
            if (callback != null) {
                callback.onLocationError("Security exception: " + e.getMessage());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting location", e);
            if (callback != null) {
                callback.onLocationError("Error getting location: " + e.getMessage());
            }
        }
    }

    private Location getLastKnownLocation() {
        if (!hasLocationPermission()) return null;

        try {
            Location gpsLocation = locationManager.getLastKnownLocation(AndroidLocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(AndroidLocationManager.NETWORK_PROVIDER);

            // Return the more recent location
            if (gpsLocation != null && networkLocation != null) {
                return gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            } else if (gpsLocation != null) {
                return gpsLocation;
            } else {
                return networkLocation;
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception getting last known location", e);
            return null;
        }
    }

    private void removeLocationUpdates() {
        if (locationManager != null && locationListener != null) {
            try {
                locationManager.removeUpdates(locationListener);
            } catch (SecurityException e) {
                Log.e(TAG, "Error removing location updates", e);
            }
        }
    }

    public void cleanup() {
        removeLocationUpdates();
        locationListener = null;
    }
}