// app/src/main/java/com/example/newtrade/utils/LocationUtils.java
package com.example.newtrade.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationUtils {

    private static final String TAG = "LocationUtils";
    public static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    // Distance options for search radius
    public static final int[] SEARCH_RADIUS_OPTIONS = {5, 10, 25, 50, 100}; // in km

    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public static void requestLocationPermission(Activity activity) {
        ActivityCompat.requestPermissions(activity,
                new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                },
                LOCATION_PERMISSION_REQUEST_CODE);
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    // FR-6.1: Calculate distance between two points
    public static double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);

        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return R * c; // Distance in km
    }

    // FR-6.1: Check if location is within search radius
    public static boolean isWithinRadius(double userLat, double userLon,
                                       double itemLat, double itemLon, int radiusKm) {
        double distance = calculateDistance(userLat, userLon, itemLat, itemLon);
        return distance <= radiusKm;
    }

    // Get current location using FusedLocationProviderClient
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        if (!hasLocationPermission(context)) {
            callback.onError("Location permission not granted");
            return;
        }

        FusedLocationProviderClient fusedLocationClient = LocationServices.getFusedLocationProviderClient(context);

        try {
            fusedLocationClient.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(location.getLatitude(), location.getLongitude());
                    } else {
                        callback.onError("Unable to get current location");
                    }
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Failed to get location", e);
                    callback.onError("Failed to get location: " + e.getMessage());
                });
        } catch (SecurityException e) {
            callback.onError("Location permission denied");
        }
    }

    // FR-6.2: Convert address string to coordinates (Geocoding)
    public static void getCoordinatesFromAddress(Context context, String address, GeocodeCallback callback) {
        if (address == null || address.trim().isEmpty()) {
            callback.onError("Address is empty");
            return;
        }

        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocationName(address, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address addr = addresses.get(0);
                    double latitude = addr.getLatitude();
                    double longitude = addr.getLongitude();

                    // Run callback on main thread
                    ((Activity) context).runOnUiThread(() ->
                        callback.onLocationReceived(latitude, longitude, addr.getAddressLine(0)));
                } else {
                    ((Activity) context).runOnUiThread(() ->
                        callback.onError("Address not found"));
                }
            } catch (IOException e) {
                Log.e(TAG, "Geocoding failed", e);
                ((Activity) context).runOnUiThread(() ->
                    callback.onError("Geocoding failed: " + e.getMessage()));
            }
        }).start();
    }

    // FR-6.2: Convert coordinates to address string (Reverse Geocoding)
    public static void getAddressFromCoordinates(Context context, double latitude, double longitude,
                                                GeocodeCallback callback) {
        new Thread(() -> {
            try {
                Geocoder geocoder = new Geocoder(context, Locale.getDefault());
                List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);

                if (addresses != null && !addresses.isEmpty()) {
                    Address address = addresses.get(0);
                    String addressText = address.getAddressLine(0);

                    // Run callback on main thread
                    ((Activity) context).runOnUiThread(() ->
                        callback.onLocationReceived(latitude, longitude, addressText));
                } else {
                    ((Activity) context).runOnUiThread(() ->
                        callback.onError("Address not found for coordinates"));
                }
            } catch (IOException e) {
                Log.e(TAG, "Reverse geocoding failed", e);
                ((Activity) context).runOnUiThread(() ->
                    callback.onError("Reverse geocoding failed: " + e.getMessage()));
            }
        }).start();
    }

    // Format distance for display
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1) {
            return String.format(Locale.getDefault(), "%.0f m", distanceKm * 1000);
        } else {
            return String.format(Locale.getDefault(), "%.1f km", distanceKm);
        }
    }

    // Callback interfaces
    public interface LocationCallback {
        void onLocationReceived(double latitude, double longitude);
        void onError(String error);
    }

    public interface GeocodeCallback {
        void onLocationReceived(double latitude, double longitude, String address);
        void onError(String error);
    }
}