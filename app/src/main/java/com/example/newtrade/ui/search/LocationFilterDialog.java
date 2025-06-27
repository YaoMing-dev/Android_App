// app/src/main/java/com/example/newtrade/ui/search/LocationFilterDialog.java
package com.example.newtrade.ui.search;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.example.newtrade.R;
import com.example.newtrade.utils.LocationUtils;
import com.example.newtrade.utils.SharedPrefsManager;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.android.material.textfield.TextInputEditText;

public class LocationFilterDialog extends BottomSheetDialogFragment {

    private static final String TAG = "LocationFilterDialog";
    private static final int REQUEST_LOCATION_PERMISSION = 1001;

    // UI Components
    private TextInputEditText etCustomLocation;
    private Button btnUseCurrentLocation, btnUseCustomLocation, btnApply, btnClear;
    private SeekBar seekBarRadius;
    private TextView tvRadiusValue, tvCurrentLocationStatus;

    // Data
    private SharedPrefsManager prefsManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationFilterListener listener;

    // Current filter values
    private Double currentLatitude = null;
    private Double currentLongitude = null;
    private String currentLocationName = "";
    private int radiusKm = 10; // Default 10km

    public interface LocationFilterListener {
        void onLocationFilterApplied(Double latitude, Double longitude, String locationName, int radiusKm);
        void onLocationFilterCleared();
    }

    public static LocationFilterDialog newInstance() {
        return new LocationFilterDialog();
    }

    public void setLocationFilterListener(LocationFilterListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefsManager = SharedPrefsManager.getInstance(requireContext());
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        // Load saved filter values
        loadSavedLocationFilter();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.dialog_location_filter, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        initViews(view);
        setupListeners();
        updateUI();
    }

    private void initViews(View view) {
        etCustomLocation = view.findViewById(R.id.et_custom_location);
        btnUseCurrentLocation = view.findViewById(R.id.btn_use_current_location);
        btnUseCustomLocation = view.findViewById(R.id.btn_use_custom_location);
        btnApply = view.findViewById(R.id.btn_apply);
        btnClear = view.findViewById(R.id.btn_clear);
        seekBarRadius = view.findViewById(R.id.seek_bar_radius);
        tvRadiusValue = view.findViewById(R.id.tv_radius_value);
        tvCurrentLocationStatus = view.findViewById(R.id.tv_current_location_status);
    }

    private void setupListeners() {
        btnUseCurrentLocation.setOnClickListener(v -> getCurrentLocation());
        btnUseCustomLocation.setOnClickListener(v -> useCustomLocation());
        btnApply.setOnClickListener(v -> applyLocationFilter());
        btnClear.setOnClickListener(v -> clearLocationFilter());

        // Radius SeekBar
        seekBarRadius.setMax(95); // 5-100km range
        seekBarRadius.setProgress(radiusKm - 5); // Adjust for 5km minimum
        seekBarRadius.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                radiusKm = progress + 5; // 5-100km range
                tvRadiusValue.setText(radiusKm + " km");
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });
    }

    private void getCurrentLocation() {
        if (!LocationUtils.hasLocationPermission(requireContext())) {
            requestPermissions(new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
            }, REQUEST_LOCATION_PERMISSION);
            return;
        }

        if (!LocationUtils.isLocationEnabled(requireContext())) {
            Toast.makeText(requireContext(), "Please enable GPS", Toast.LENGTH_SHORT).show();
            return;
        }

        btnUseCurrentLocation.setEnabled(false);
        btnUseCurrentLocation.setText("⏳ Getting Location...");

        try {
            fusedLocationClient.getCurrentLocation(
                    com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY,
                    null
            ).addOnCompleteListener(task -> {
                btnUseCurrentLocation.setEnabled(true);
                btnUseCurrentLocation.setText("📍 Use Current Location");

                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    handleLocationResult(location);
                } else {
                    // Fallback to last known location
                    getLastKnownLocation();
                }
            });
        } catch (SecurityException e) {
            btnUseCurrentLocation.setEnabled(true);
            btnUseCurrentLocation.setText("📍 Use Current Location");
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void getLastKnownLocation() {
        try {
            fusedLocationClient.getLastLocation().addOnCompleteListener(task -> {
                if (task.isSuccessful() && task.getResult() != null) {
                    Location location = task.getResult();
                    handleLocationResult(location);
                } else {
                    Toast.makeText(requireContext(), "Unable to get current location", Toast.LENGTH_SHORT).show();
                }
            });
        } catch (SecurityException e) {
            Toast.makeText(requireContext(), "Location permission denied", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleLocationResult(Location location) {
        currentLatitude = location.getLatitude();
        currentLongitude = location.getLongitude();
        currentLocationName = "Current Location";

        // TODO: Reverse geocoding to get address name
        // For now, just show coordinates
        currentLocationName = String.format("%.4f, %.4f", currentLatitude, currentLongitude);

        updateUI();
        Log.d(TAG, "✅ Got current location: " + currentLocationName);
    }

    private void useCustomLocation() {
        String customLocation = etCustomLocation.getText().toString().trim();
        if (customLocation.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // TODO: Geocoding to convert address to coordinates
        // For now, mock some coordinates for Ho Chi Minh City
        if (customLocation.toLowerCase().contains("ho chi minh") ||
                customLocation.toLowerCase().contains("hcm") ||
                customLocation.toLowerCase().contains("saigon")) {

            currentLatitude = 10.8231;
            currentLongitude = 106.6297;
            currentLocationName = "Ho Chi Minh City, Vietnam";

            updateUI();
            Toast.makeText(requireContext(), "Location set to: " + currentLocationName, Toast.LENGTH_SHORT).show();
        } else {
            // Mock other locations
            currentLatitude = 10.8000 + (Math.random() - 0.5) * 0.2;
            currentLongitude = 106.6000 + (Math.random() - 0.5) * 0.2;
            currentLocationName = customLocation;

            updateUI();
            Toast.makeText(requireContext(), "Location set to: " + currentLocationName, Toast.LENGTH_SHORT).show();
        }
    }

    private void updateUI() {
        tvRadiusValue.setText(radiusKm + " km");

        if (currentLatitude != null && currentLongitude != null) {
            tvCurrentLocationStatus.setText("📍 " + currentLocationName);
            tvCurrentLocationStatus.setVisibility(View.VISIBLE);
            btnApply.setEnabled(true);
        } else {
            tvCurrentLocationStatus.setVisibility(View.GONE);
            btnApply.setEnabled(false);
        }
    }

    private void applyLocationFilter() {
        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(requireContext(), "Please select a location first", Toast.LENGTH_SHORT).show();
            return;
        }

        // Save filter preferences
        saveLocationFilter();

        if (listener != null) {
            listener.onLocationFilterApplied(currentLatitude, currentLongitude, currentLocationName, radiusKm);
        }

        dismiss();
        Log.d(TAG, "✅ Location filter applied: " + currentLocationName + " (" + radiusKm + "km)");
    }

    private void clearLocationFilter() {
        currentLatitude = null;
        currentLongitude = null;
        currentLocationName = "";
        radiusKm = 10;

        // Clear saved preferences
        clearSavedLocationFilter();

        if (listener != null) {
            listener.onLocationFilterCleared();
        }

        dismiss();
        Log.d(TAG, "✅ Location filter cleared");
    }

    private void loadSavedLocationFilter() {
        // TODO: Load from SharedPreferences
        // For now, use defaults
    }

    private void saveLocationFilter() {
        // TODO: Save to SharedPreferences
        // prefsManager.saveLocationFilter(currentLatitude, currentLongitude, currentLocationName, radiusKm);
    }

    private void clearSavedLocationFilter() {
        // TODO: Clear from SharedPreferences
        // prefsManager.clearLocationFilter();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();
            } else {
                Toast.makeText(requireContext(), "Location permission is required", Toast.LENGTH_SHORT).show();
            }
        }
    }
}