package com.example.newtrade.ui.location;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.newtrade.R;
import com.google.android.material.appbar.MaterialToolbar;

public class LocationPickerActivity extends AppCompatActivity {

    private static final String TAG = "LocationPickerActivity";

    // UI Components
    private MaterialToolbar toolbar;
    private TextView tvSelectedLocation;
    private Button btnConfirmLocation, btnUseCurrentLocation;

    // Data
    private String selectedLocation = "";
    private double selectedLatitude = 0.0;
    private double selectedLongitude = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location_picker);

        initViews();
        setupToolbar();
        setupListeners();

        Log.d(TAG, "✅ LocationPickerActivity created");
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        tvSelectedLocation = findViewById(R.id.tv_selected_location);
        btnConfirmLocation = findViewById(R.id.btn_confirm_location);
        btnUseCurrentLocation = findViewById(R.id.btn_use_current_location);
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Choose Location");
        }
    }

    private void setupListeners() {
        btnUseCurrentLocation.setOnClickListener(v -> useCurrentLocation());
        btnConfirmLocation.setOnClickListener(v -> confirmLocation());
    }

    private void useCurrentLocation() {
        // TODO: Implement GPS location
        selectedLocation = "Current Location";
        selectedLatitude = 10.7769; // Default Ho Chi Minh City
        selectedLongitude = 106.7009;

        tvSelectedLocation.setText(selectedLocation);
        btnConfirmLocation.setEnabled(true);

        Toast.makeText(this, "Using current location", Toast.LENGTH_SHORT).show();
    }

    private void confirmLocation() {
        if (selectedLocation.isEmpty()) {
            Toast.makeText(this, "Please select a location", Toast.LENGTH_SHORT).show();
            return;
        }

        // Return result
        Intent resultIntent = new Intent();
        resultIntent.putExtra("location", selectedLocation);
        resultIntent.putExtra("latitude", selectedLatitude);
        resultIntent.putExtra("longitude", selectedLongitude);

        setResult(Activity.RESULT_OK, resultIntent);
        finish();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}