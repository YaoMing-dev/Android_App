// app/src/main/java/com/example/newtrade/ui/location/LocationPickerActivity.java
package com.example.newtrade.ui.location;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.newtrade.R;
import com.example.newtrade.utils.Constants;
import com.example.newtrade.utils.LocationManager;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class LocationPickerActivity extends AppCompatActivity implements
        OnMapReadyCallback, LocationManager.LocationCallback {

    private static final String TAG = "LocationPickerActivity";

    // UI Components
    private Toolbar toolbar;
    private EditText etSearchLocation;
    private MaterialButton btnConfirm;
    private FloatingActionButton fabMyLocation;

    // Map
    private GoogleMap googleMap;
    private Marker selectedMarker;

    // Location
    private LocationManager locationManager;
    private LatLng selectedLocation;
    private String selectedAddress;

    // Initial data
    private double initialLatitude = 0.0;
    private double initialLongitude = 0.0;
    private String initialAddress = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        locationManager = new LocationManager(this, this);

        // Get initial location from intent
        Intent intent = getIntent();
        initialLatitude = intent.getDoubleExtra(Constants.BUNDLE_LOCATION_LAT, 0.0);
        initialLongitude = intent.getDoubleExtra(Constants.BUNDLE_LOCATION_LNG, 0.0);
        initialAddress = intent.getStringExtra(Constants.BUNDLE_LOCATION_NAME);

        initViews();
        setupToolbar();
        setupMap();
        setupListeners();
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etSearchLocation = findViewById(R.id.et_search_location);
        btnConfirm = findViewById(R.id.btn_confirm);
        fabMyLocation = findViewById(R.id.fab_my_location);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Choose Location");
        }
    }

    private void setupMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map_fragment);
        if (mapFragment != null) {
            mapFragment.getMapAsync(this);
        }
    }

    private void setupListeners() {
        // Search location
        etSearchLocation.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String query = s.toString().trim();
                if (query.length() > 3) {
                    searchLocation(query);
                }
            }
        });

        // Confirm location
        btnConfirm.setOnClickListener(v -> confirmLocation());

        // My location
        fabMyLocation.setOnClickListener(v -> getCurrentLocation());
    }

    private void requestLocation() {
        if (!locationManager.hasLocationPermission()) {
            locationManager.requestLocationPermission();
            return;
        }

        // Show loading indicator if needed
        locationManager.getCurrentLocation();
    }

    @Override
    public void onMapReady(@NonNull GoogleMap map) {
        googleMap = map;

        // Enable map controls
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        googleMap.getUiSettings().setMyLocationButtonEnabled(false); // We have our own button

        // Check permission and enable my location
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            googleMap.setMyLocationEnabled(true);
        }

        // Set map click listener
        googleMap.setOnMapClickListener(this::onMapClick);

        // Set initial location
        if (initialLatitude != 0.0 && initialLongitude != 0.0) {
            LatLng initialLocation = new LatLng(initialLatitude, initialLongitude);
            setSelectedLocation(initialLocation, initialAddress);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, Constants.DEFAULT_MAP_ZOOM));
        } else {
            // Default to Vietnam center
            LatLng vietnamCenter = new LatLng(16.0583, 108.2772);
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(vietnamCenter, 6));
            getCurrentLocation();
        }
    }

    private void onMapClick(LatLng latLng) {
        setSelectedLocation(latLng, null);
        reverseGeocode(latLng);
    }

    private void setSelectedLocation(LatLng location, String address) {
        selectedLocation = location;

        // Clear previous marker
        if (selectedMarker != null) {
            selectedMarker.remove();
        }

        // Add new marker
        selectedMarker = googleMap.addMarker(new MarkerOptions()
                .position(location)
                .title("Selected Location")
                .snippet(address));

        // Update address text
        if (address != null && !address.isEmpty()) {
            selectedAddress = address;
            etSearchLocation.setText(address);
        }

        // Enable confirm button
        btnConfirm.setEnabled(true);
    }

    private void searchLocation(String query) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocationName(query, 5);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                LatLng location = new LatLng(address.getLatitude(), address.getLongitude());

                // Move camera to found location
                googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(location, Constants.DEFAULT_MAP_ZOOM));

                // Set as selected location
                String addressText = getAddressText(address);
                setSelectedLocation(location, addressText);
            }
        } catch (IOException e) {
            Log.e(TAG, "Geocoder error", e);
        }
    }

    private void reverseGeocode(LatLng location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(
                    location.latitude, location.longitude, 1);

            if (addresses != null && !addresses.isEmpty()) {
                Address address = addresses.get(0);
                String addressText = getAddressText(address);

                selectedAddress = addressText;
                etSearchLocation.setText(addressText);

                // Update marker
                if (selectedMarker != null) {
                    selectedMarker.setSnippet(addressText);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, "Reverse geocoding error", e);
        }
    }

    private String getAddressText(Address address) {
        StringBuilder addressText = new StringBuilder();

        if (address.getFeatureName() != null) {
            addressText.append(address.getFeatureName()).append(", ");
        }
        if (address.getLocality() != null) {
            addressText.append(address.getLocality()).append(", ");
        }
        if (address.getSubAdminArea() != null) {
            addressText.append(address.getSubAdminArea()).append(", ");
        }
        if (address.getAdminArea() != null) {
            addressText.append(address.getAdminArea()).append(", ");
        }
        if (address.getCountryName() != null) {
            addressText.append(address.getCountryName());
        }

        // Remove trailing comma and space
        String result = addressText.toString();
        if (result.endsWith(", ")) {
            result = result.substring(0, result.length() - 2);
        }

        return result;
    }

    private void getCurrentLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    Constants.PERMISSION_REQUEST_LOCATION);
            return;
        }

        fabMyLocation.setEnabled(false);
        locationManager.requestLocation();
    }

    private void confirmLocation() {
        if (selectedLocation == null) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return selected location
        Intent resultIntent = new Intent();
        resultIntent.putExtra(Constants.BUNDLE_LOCATION_LAT, selectedLocation.latitude);
        resultIntent.putExtra(Constants.BUNDLE_LOCATION_LNG, selectedLocation.longitude);
        resultIntent.putExtra(Constants.BUNDLE_LOCATION_NAME, selectedAddress);

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    // LocationManager.LocationCallback implementation
    @Override
    public void onLocationReceived(Location location) {
        fabMyLocation.setEnabled(true);

        LatLng userLocation = new LatLng(location.getLatitude(), location.getLongitude());

        // Move camera to user location
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(userLocation, Constants.DEFAULT_MAP_ZOOM));

        // Set as selected location
        setSelectedLocation(userLocation, null);
        reverseGeocode(userLocation);
    }

    @Override
    public void onLocationError(String error) {
        fabMyLocation.setEnabled(true);
        Toast.makeText(this, "Failed to get location: " + error, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == Constants.PERMISSION_REQUEST_LOCATION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation();

                // Enable my location on map
                if (googleMap != null) {
                    try {
                        googleMap.setMyLocationEnabled(true);
                    } catch (SecurityException e) {
                        Log.e(TAG, "Security exception", e);
                    }
                }
            } else {
                Toast.makeText(this, "Location permission required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (locationManager != null) {
            locationManager.cleanup();
        }
    }
}