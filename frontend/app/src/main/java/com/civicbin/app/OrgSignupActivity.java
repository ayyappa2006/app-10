package com.civicbin.app;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.List;
import java.util.Locale;

public class OrgSignupActivity extends AppCompatActivity {

    private EditText etOrgName, etCity, etEmail, etPassword;
    private Button btnSignup, btnGetLocation;
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1002;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_signup);

        etOrgName = findViewById(R.id.etOrgName);
        etCity = findViewById(R.id.etCity);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        btnSignup = findViewById(R.id.btnSignup);
        btnGetLocation = findViewById(R.id.btnGetLocation);
        TextView tvLogin = findViewById(R.id.tvLogin);

        locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

        // Automatically try to get live location on start
        checkLocationPermission();

        if (btnGetLocation != null) {
            btnGetLocation.setOnClickListener(v -> checkLocationPermission());
        }

        btnSignup.setOnClickListener(v -> {
            String orgName = etOrgName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();
            String city = etCity.getText().toString().trim();

            if (orgName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignup.setText("Creating...");
            btnSignup.setEnabled(false);

            try {
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("org_name", orgName);
                payload.put("email", email);
                payload.put("password", password);
                payload.put("city", city);

                ApiClient.post("org_signup.php", payload, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject response) {
                        btnSignup.setText("Create Account");
                        btnSignup.setEnabled(true);

                        if ("success".equals(response.optString("status"))) {
                            getSharedPreferences("CivicBinPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("org_id", response.optInt("org_id"))
                                .putString("org_name", response.optString("org_name"))
                                .apply();
                            
                            startActivity(new Intent(OrgSignupActivity.this, OrgDashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(OrgSignupActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        btnSignup.setText("Create Account");
                        btnSignup.setEnabled(true);
                        Toast.makeText(OrgSignupActivity.this, error, Toast.LENGTH_LONG).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                btnSignup.setText("Create Account");
                btnSignup.setEnabled(true);
                Toast.makeText(OrgSignupActivity.this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        tvLogin.setOnClickListener(v -> {
            finish(); // Returns to OrgLoginActivity
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            fetchLiveLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && (grantResults[0] == PackageManager.PERMISSION_GRANTED || grantResults[1] == PackageManager.PERMISSION_GRANTED)) {
                fetchLiveLocation();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void fetchLiveLocation() {
        try {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    updateLocationAddress(location);
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(@NonNull String provider) {}
                @Override
                public void onProviderDisabled(@NonNull String provider) {}
            };

            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }

            Location lastKnown = null;
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                if (lastKnown == null) {
                    lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                }
            }

            if (lastKnown != null) {
                updateLocationAddress(lastKnown);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateLocationAddress(Location location) {
        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                String city = addr.getLocality() != null ? addr.getLocality() : addr.getSubAdminArea();
                if (city == null) city = addr.getAddressLine(0);
                if (city != null) {
                    etCity.setText(city);
                }
            } else {
                etCity.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
            }
        } catch (Exception e) {
            e.printStackTrace();
            etCity.setText("Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude());
        }
    }
}

