// app/src/main/java/com/example/newtrade/utils/LocationUtils.java
package com.example.newtrade.utils;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

import androidx.core.content.ContextCompat;

import com.example.newtrade.models.Product;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class LocationUtils {

    private static final String TAG = "LocationUtils";

    // ===== PERMISSION & GPS CHECKS =====
    public static boolean hasLocationPermission(Context context) {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean isLocationEnabled(Context context) {
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager != null && (
                locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        );
    }

    // ===== DISTANCE CALCULATION =====
    /**
     * Calculate distance between two points using Haversine formula
     * @param lat1 Latitude of first point
     * @param lon1 Longitude of first point
     * @param lat2 Latitude of second point
     * @param lon2 Longitude of second point
     * @return Distance in kilometers
     */
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

    // ===== BATCH PRODUCT DISTANCE OPERATIONS =====
    /**
     * Calculate distances from user location to all products
     * @param userLat User's latitude
     * @param userLng User's longitude
     * @param products List of products to calculate distances for
     */
    public static void calculateDistancesForProducts(double userLat, double userLng,
                                                     List<Product> products) {
        if (products == null || products.isEmpty()) {
            Log.w(TAG, "No products to calculate distances for");
            return;
        }

        int calculatedCount = 0;
        for (Product product : products) {
            if (product.hasLocation()) {
                double distance = calculateDistance(
                        userLat, userLng,
                        product.getLatitude(), product.getLongitude()
                );
                product.setDistanceFromUser(distance);
                calculatedCount++;
            }
        }

        Log.d(TAG, "✅ Calculated distances for " + calculatedCount + "/" + products.size() + " products");
    }

    /**
     * Sort products by distance from user (nearest first)
     * Products without location data are moved to the end
     */
    public static void sortProductsByDistance(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return;
        }

        Collections.sort(products, (p1, p2) -> {
            Double d1 = p1.getDistanceFromUser();
            Double d2 = p2.getDistanceFromUser();

            // Products without distance go to end
            if (d1 == null && d2 == null) return 0;
            if (d1 == null) return 1;
            if (d2 == null) return -1;

            return d1.compareTo(d2);
        });

        Log.d(TAG, "✅ Products sorted by distance");
    }

    /**
     * Filter products to only include those within specified radius
     * @param products List of products to filter
     * @param radiusKm Maximum distance in kilometers
     * @return New list containing only nearby products
     */
    public static List<Product> filterProductsByRadius(List<Product> products, double radiusKm) {
        if (products == null || products.isEmpty()) {
            return new ArrayList<>();
        }

        List<Product> filtered = new ArrayList<>();
        for (Product product : products) {
            if (product.isNearby(radiusKm)) {
                filtered.add(product);
            }
        }

        Log.d(TAG, "✅ Filtered " + filtered.size() + "/" + products.size() +
                " products within " + radiusKm + "km");
        return filtered;
    }

    /**
     * Get products grouped by distance ranges
     * @param products List of products
     * @return Lists of products in different distance ranges
     */
    public static LocationGroups groupProductsByDistance(List<Product> products) {
        LocationGroups groups = new LocationGroups();

        if (products == null || products.isEmpty()) {
            return groups;
        }

        for (Product product : products) {
            Double distance = product.getDistanceFromUser();
            if (distance == null) {
                groups.noLocation.add(product);
            } else if (distance <= 1.0) {
                groups.within1km.add(product);
            } else if (distance <= 5.0) {
                groups.within5km.add(product);
            } else if (distance <= 10.0) {
                groups.within10km.add(product);
            } else if (distance <= 25.0) {
                groups.within25km.add(product);
            } else {
                groups.beyond25km.add(product);
            }
        }

        Log.d(TAG, "✅ Grouped products: " +
                "1km=" + groups.within1km.size() + ", " +
                "5km=" + groups.within5km.size() + ", " +
                "10km=" + groups.within10km.size() + ", " +
                "25km=" + groups.within25km.size() + ", " +
                "far=" + groups.beyond25km.size() + ", " +
                "no_loc=" + groups.noLocation.size());

        return groups;
    }

    public static class LocationGroups {
        public List<Product> within1km = new ArrayList<>();
        public List<Product> within5km = new ArrayList<>();
        public List<Product> within10km = new ArrayList<>();
        public List<Product> within25km = new ArrayList<>();
        public List<Product> beyond25km = new ArrayList<>();
        public List<Product> noLocation = new ArrayList<>();

        public List<Product> getAllNearby() {
            List<Product> nearby = new ArrayList<>();
            nearby.addAll(within1km);
            nearby.addAll(within5km);
            nearby.addAll(within10km);
            return nearby;
        }

        public int getTotalNearbyCount() {
            return within1km.size() + within5km.size() + within10km.size();
        }
    }

    // ===== CURRENT LOCATION RETRIEVAL =====
    /**
     * Get user's current location asynchronously
     */
    public static void getCurrentLocation(Context context, LocationCallback callback) {
        if (!hasLocationPermission(context)) {
            callback.onLocationError("Location permission denied");
            return;
        }

        if (!isLocationEnabled(context)) {
            callback.onLocationError("GPS is disabled");
            return;
        }

        FusedLocationProviderClient fusedLocationClient =
                LocationServices.getFusedLocationProviderClient(context);

        try {
            fusedLocationClient.getCurrentLocation(
                    LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnSuccessListener(location -> {
                if (location != null) {
                    Log.d(TAG, "✅ Got current location: " + location.getLatitude() +
                            ", " + location.getLongitude());
                    callback.onLocationSuccess(location.getLatitude(), location.getLongitude());
                } else {
                    Log.w(TAG, "Current location is null, trying last known location");
                    tryLastKnownLocation(fusedLocationClient, callback);
                }
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Failed to get current location", e);
                tryLastKnownLocation(fusedLocationClient, callback);
            });
        } catch (SecurityException e) {
            Log.e(TAG, "Location permission error", e);
            callback.onLocationError("Location permission error");
        }
    }

    private static void tryLastKnownLocation(FusedLocationProviderClient fusedLocationClient,
                                             LocationCallback callback) {
        try {
            fusedLocationClient.getLastLocation()
                    .addOnSuccessListener(location -> {
                        if (location != null) {
                            Log.d(TAG, "✅ Got last known location: " + location.getLatitude() +
                                    ", " + location.getLongitude());
                            callback.onLocationSuccess(location.getLatitude(), location.getLongitude());
                        } else {
                            callback.onLocationError("Unable to get location");
                        }
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Failed to get last known location", e);
                        callback.onLocationError("Location error: " + e.getMessage());
                    });
        } catch (SecurityException e) {
            callback.onLocationError("Location permission error");
        }
    }

    public interface LocationCallback {
        void onLocationSuccess(double latitude, double longitude);
        void onLocationError(String error);
    }

    // ===== UTILITY METHODS =====
    /**
     * Format distance for display
     */
    public static String formatDistance(double distanceKm) {
        if (distanceKm < 1.0) {
            return String.format("%.0f m", distanceKm * 1000);
        } else {
            return String.format("%.1f km", distanceKm);
        }
    }

    /**
     * Get distance description for user display
     */
    public static String getDistanceDescription(double distanceKm) {
        if (distanceKm < 0.5) {
            return "Very close";
        } else if (distanceKm < 2.0) {
            return "Nearby";
        } else if (distanceKm < 10.0) {
            return "Within city";
        } else if (distanceKm < 50.0) {
            return "Same region";
        } else {
            return "Far away";
        }
    }

    /**
     * Validate coordinates
     */
    public static boolean isValidCoordinates(double latitude, double longitude) {
        return latitude >= -90 && latitude <= 90 && longitude >= -180 && longitude <= 180;
    }

    /**
     * Check if coordinates are in Vietnam (approximate bounds)
     */
    public static boolean isInVietnam(double latitude, double longitude) {
        return latitude >= 8.0 && latitude <= 24.0 &&
                longitude >= 102.0 && longitude <= 110.0;
    }

    // ===== DEBUGGING =====
    public static void logLocationStats(List<Product> products) {
        if (products == null || products.isEmpty()) {
            Log.d(TAG, "No products to analyze");
            return;
        }

        int withLocation = 0;
        int withDistance = 0;
        double minDistance = Double.MAX_VALUE;
        double maxDistance = 0;
        double totalDistance = 0;

        for (Product product : products) {
            if (product.hasLocation()) {
                withLocation++;
            }
            if (product.getDistanceFromUser() != null) {
                withDistance++;
                double distance = product.getDistanceFromUser();
                totalDistance += distance;
                minDistance = Math.min(minDistance, distance);
                maxDistance = Math.max(maxDistance, distance);
            }
        }

        Log.d(TAG, "=== LOCATION STATS ===");
        Log.d(TAG, "Total products: " + products.size());
        Log.d(TAG, "With coordinates: " + withLocation);
        Log.d(TAG, "With calculated distance: " + withDistance);
        if (withDistance > 0) {
            Log.d(TAG, "Distance range: " + formatDistance(minDistance) +
                    " to " + formatDistance(maxDistance));
            Log.d(TAG, "Average distance: " + formatDistance(totalDistance / withDistance));
        }
        Log.d(TAG, "=====================");
    }
}