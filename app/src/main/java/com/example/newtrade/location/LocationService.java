package com.example.newtrade.location;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationService implements LocationListener {
    private static final String TAG = "LocationService";
    private static final long MIN_TIME_BETWEEN_UPDATES = 10000; // 10 seconds
    private static final float MIN_DISTANCE_CHANGE_FOR_UPDATES = 10; // 10 meters

    private Context context;
    private LocationManager locationManager;
    private Geocoder geocoder;
    private LocationCallback callback;
    private Location lastKnownLocation;

    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude, String address);
        void onLocationError(String error);
    }

    public LocationService(Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        this.geocoder = new Geocoder(context, Locale.getDefault());
    }

    public void setLocationCallback(LocationCallback callback) {
        this.callback = callback;
    }

    public void startLocationUpdates() {
        if (!hasLocationPermission()) {
            if (callback != null) {
                callback.onLocationError("Location permission not granted");
            }
            return;
        }

        try {
            // Try GPS first
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this
                );
                Log.d(TAG, "GPS location updates started");
            }

            // Fallback to network provider
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestLocationUpdates(
                    LocationManager.NETWORK_PROVIDER,
                    MIN_TIME_BETWEEN_UPDATES,
                    MIN_DISTANCE_CHANGE_FOR_UPDATES,
                    this
                );
                Log.d(TAG, "Network location updates started");
            }

            // Get last known location immediately
            getLastKnownLocation();

        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when requesting location updates", e);
            if (callback != null) {
                callback.onLocationError("Security exception: " + e.getMessage());
            }
        }
    }

    public void stopLocationUpdates() {
        if (locationManager != null) {
            locationManager.removeUpdates(this);
            Log.d(TAG, "Location updates stopped");
        }
    }

    public void getCurrentLocation() {
        if (!hasLocationPermission()) {
            if (callback != null) {
                callback.onLocationError("Location permission not granted");
            }
            return;
        }

        try {
            Location gpsLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location networkLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            Location bestLocation = null;
            if (gpsLocation != null && networkLocation != null) {
                bestLocation = gpsLocation.getTime() > networkLocation.getTime() ? gpsLocation : networkLocation;
            } else if (gpsLocation != null) {
                bestLocation = gpsLocation;
            } else if (networkLocation != null) {
                bestLocation = networkLocation;
            }

            if (bestLocation != null) {
                onLocationChanged(bestLocation);
            } else {
                if (callback != null) {
                    callback.onLocationError("Unable to get current location");
                }
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting current location", e);
            if (callback != null) {
                callback.onLocationError("Permission denied");
            }
        }
    }

    private void getLastKnownLocation() {
        try {
            Location lastGps = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            Location lastNetwork = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);

            if (lastGps != null && (lastNetwork == null || lastGps.getTime() > lastNetwork.getTime())) {
                onLocationChanged(lastGps);
            } else if (lastNetwork != null) {
                onLocationChanged(lastNetwork);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception when getting last known location", e);
        }
    }

    @Override
    public void onLocationChanged(Location location) {
        if (location != null) {
            lastKnownLocation = location;
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            Log.d(TAG, "Location updated: " + latitude + ", " + longitude);

            // Get address from coordinates
            getAddressFromLocation(latitude, longitude);
        }
    }

    private void getAddressFromLocation(double latitude, double longitude) {
        try {
            List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
            String address = "Unknown location";

            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                StringBuilder sb = new StringBuilder();

                if (addr.getFeatureName() != null) {
                    sb.append(addr.getFeatureName()).append(", ");
                }
                if (addr.getLocality() != null) {
                    sb.append(addr.getLocality()).append(", ");
                }
                if (addr.getAdminArea() != null) {
                    sb.append(addr.getAdminArea());
                }

                address = sb.toString();
                if (address.endsWith(", ")) {
                    address = address.substring(0, address.length() - 2);
                }
            }

            if (callback != null) {
                callback.onLocationReceived(latitude, longitude, address);
            }

        } catch (IOException e) {
            Log.e(TAG, "Geocoder exception", e);
            if (callback != null) {
                callback.onLocationReceived(latitude, longitude, "Address not available");
            }
        }
    }

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

    public Location getLastKnownLocationData() {
        return lastKnownLocation;
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        Log.d(TAG, "Provider " + provider + " status changed: " + status);
    }

    @Override
    public void onProviderEnabled(String provider) {
        Log.d(TAG, "Provider " + provider + " enabled");
    }

    @Override
    public void onProviderDisabled(String provider) {
        Log.d(TAG, "Provider " + provider + " disabled");
        if (callback != null) {
            callback.onLocationError("Location provider " + provider + " disabled");
        }
    }
}
