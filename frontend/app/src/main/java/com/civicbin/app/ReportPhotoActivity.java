package com.civicbin.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.json.JSONArray;
import org.json.JSONObject;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

import java.util.List;
import java.util.Locale;

import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;
import com.google.mlkit.vision.label.ImageLabel;

public class ReportPhotoActivity extends AppCompatActivity {

    private MapView mapView;
    private TextView tvAddressDisplay;
    private EditText etCityName;
    private String currentCity = "";
    private String currentAddress = "";
    private LocationManager locationManager;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        android.content.Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, android.preference.PreferenceManager.getDefaultSharedPreferences(ctx));
        
        setContentView(R.layout.activity_report_photo);

        ImageView btnBack = findViewById(R.id.btnBack);
        ImageView ivPreview = findViewById(R.id.ivPreview);
        mapView = findViewById(R.id.mapView);
        tvAddressDisplay = findViewById(R.id.tvAddressDisplay);
        etCityName = findViewById(R.id.etCityName);
        EditText etContactNumber = findViewById(R.id.etContactNumber);
        Spinner spinnerCategory = findViewById(R.id.spinnerCategory);
        EditText etDescription = findViewById(R.id.etDescription);
        Button btnSendReport = findViewById(R.id.btnSendReport);

        String[] categories = new String[]{"Garbage Dump", "Pothole", "Water Leak", "Fallen Tree", "Street Light", "Other"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, categories);
        spinnerCategory.setAdapter(adapter);

        mapView.setMultiTouchControls(true);
        mapView.getController().setZoom(15.0);
        locationManager = (LocationManager) getSystemService(android.content.Context.LOCATION_SERVICE);
        checkLocationPermission();

        btnBack.setOnClickListener(v -> finish());

        String uriString = getIntent().getStringExtra("photoUri");
        if (uriString != null) {
            ivPreview.setImageURI(Uri.parse(uriString));
        }

        btnSendReport.setOnClickListener(v -> {
            String city = etCityName.getText().toString();
            String address = currentAddress;
            String contact = etContactNumber.getText().toString();
            String category = spinnerCategory.getSelectedItem().toString();
            String description = etDescription.getText().toString();

            if (city.isEmpty() || address.isEmpty() || contact.isEmpty() || description.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSendReport.setText("Validating Image...");
            btnSendReport.setEnabled(false);

            try {
                int userId = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getInt("user_id", -1);
                
                String finalPhotoUri = uriString != null ? uriString : "";
                
                // Copy photo to internal storage to bypass Android picker permission expiry!
                if (!finalPhotoUri.isEmpty()) {
                    try {
                        java.io.InputStream is = getContentResolver().openInputStream(Uri.parse(finalPhotoUri));
                        if (is != null) {
                            java.io.File destFile = new java.io.File(getFilesDir(), "report_img_" + System.currentTimeMillis() + ".jpg");
                            java.io.FileOutputStream fos = new java.io.FileOutputStream(destFile);
                            byte[] buffer = new byte[4096];
                            int length;
                            while ((length = is.read(buffer)) > 0) {
                                fos.write(buffer, 0, length);
                            }
                            fos.close();
                            is.close();
                            finalPhotoUri = Uri.fromFile(destFile).toString();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                
                if (finalPhotoUri.isEmpty()) {
                    Toast.makeText(this, "No image selected", Toast.LENGTH_SHORT).show();
                    btnSendReport.setText("Send Report");
                    btnSendReport.setEnabled(true);
                    return;
                }

                InputImage image = InputImage.fromFilePath(this, Uri.parse(finalPhotoUri));
                ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                    .setConfidenceThreshold(0.15f)
                    .build();
                ImageLabeler labeler = ImageLabeling.getClient(options);
                
                final String readyUri = finalPhotoUri;
                labeler.process(image)
                    .addOnSuccessListener(labels -> {
                        boolean isGarbage = false;
                        boolean hasInvalidElements = false;
                        StringBuilder detected = new StringBuilder();
                        for (ImageLabel label : labels) {
                            String text = label.getText().toLowerCase();
                            detected.append(text).append(", ");
                            
                            if (text.contains("person") || text.contains("face") || text.contains("man") || text.contains("woman") 
                                || text.contains("human") || text.contains("qr") || text.contains("barcode") || text.contains("scanner") 
                                || text.contains("code") || text.contains("portrait") || text.contains("selfie")) {
                                hasInvalidElements = true;
                            }
                            
                            if (text.contains("waste") || text.contains("garbage") || text.contains("trash") || text.contains("litter") 
                                || text.contains("rubbish") || text.contains("dump") || text.contains("junk") || text.contains("plastic") 
                                || text.contains("pollution") || text.contains("debris") || text.contains("scrap") || text.contains("landfill")
                                || text.contains("dust") || text.contains("dirt") || text.contains("soil") || text.contains("bag") 
                                || text.contains("bottle") || text.contains("can") || text.contains("cover")) {
                                isGarbage = true;
                            }
                        }
                        
                        if (hasInvalidElements) {
                            Toast.makeText(ReportPhotoActivity.this, "Invalid image: Contains a person, face, or barcode.", Toast.LENGTH_LONG).show();
                            btnSendReport.setText("Send Report");
                            btnSendReport.setEnabled(true);
                        } else if (isGarbage) {
                            btnSendReport.setText("Uploading Image...");
                            ApiClient.uploadPhoto(readyUri, getContentResolver(), new ApiClient.ApiCallback() {
                                @Override
                                public void onSuccess(JSONObject uploadResponse) {
                                    if ("success".equals(uploadResponse.optString("status"))) {
                                        String serverPhotoUrl = uploadResponse.optString("photo_url");
                                        runOnUiThread(() -> {
                                            btnSendReport.setText("Submitting...");
                                            submitReport(userId, city, address, contact, category, description, serverPhotoUrl, btnSendReport);
                                        });
                                    } else {
                                        runOnUiThread(() -> {
                                            Toast.makeText(ReportPhotoActivity.this, "Image upload failed: " + uploadResponse.optString("message"), Toast.LENGTH_LONG).show();
                                            btnSendReport.setText("Send Report");
                                            btnSendReport.setEnabled(true);
                                        });
                                    }
                                }

                                @Override
                                public void onError(String error) {
                                    runOnUiThread(() -> {
                                        Toast.makeText(ReportPhotoActivity.this, "Network error uploading image: " + error, Toast.LENGTH_LONG).show();
                                        btnSendReport.setText("Send Report");
                                        btnSendReport.setEnabled(true);
                                    });
                                }
                            });
                        } else {
                            String det = detected.toString();
                            if (det.length() > 0) det = det.substring(0, det.length() - 2);
                            Toast.makeText(ReportPhotoActivity.this, "Detected: " + det + ". Please add a garbage image.", Toast.LENGTH_LONG).show();
                            btnSendReport.setText("Send Report");
                            btnSendReport.setEnabled(true);
                        }
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(ReportPhotoActivity.this, "Image validation failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        btnSendReport.setText("Send Report");
                        btnSendReport.setEnabled(true);
                    });

            } catch (Exception e) {
                Toast.makeText(this, "Error preparing report", Toast.LENGTH_SHORT).show();
                btnSendReport.setText("Send Report");
                btnSendReport.setEnabled(true);
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            getLocation();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getLocation();
            } else {
                tvAddressDisplay.setText("Location permission denied. Cannot retrieve address.");
            }
        }
    }

    private void getLocation() {
        try {
            LocationListener locationListener = new LocationListener() {
                @Override
                public void onLocationChanged(Location location) {
                    updateLocation(location);
                }
                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {}
                @Override
                public void onProviderEnabled(String provider) {}
                @Override
                public void onProviderDisabled(String provider) {}
            };
            
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.GPS_PROVIDER, locationListener, null);
            }
            if (locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER, locationListener, null);
            }
            
            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(lastKnown != null) {
                updateLocation(lastKnown);
            }
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }

    private void updateLocation(Location location) {
        GeoPoint startPoint = new GeoPoint(location.getLatitude(), location.getLongitude());
        mapView.getController().setCenter(startPoint);
        
        Marker startMarker = new Marker(mapView);
        startMarker.setPosition(startPoint);
        startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
        mapView.getOverlays().clear();
        mapView.getOverlays().add(startMarker);
        mapView.invalidate();

        try {
            Geocoder geocoder = new Geocoder(this, Locale.getDefault());
            List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
            if (addresses != null && !addresses.isEmpty()) {
                Address addr = addresses.get(0);
                currentCity = addr.getLocality() != null ? addr.getLocality() : addr.getSubAdminArea();
                if (currentCity == null) currentCity = "Unknown City";
                
                StringBuilder sb = new StringBuilder();
                for (int i = 0; i <= addr.getMaxAddressLineIndex(); i++) {
                    sb.append(addr.getAddressLine(i)).append(", ");
                }
                currentAddress = sb.toString();
                if(currentAddress.endsWith(", ")) currentAddress = currentAddress.substring(0, currentAddress.length() - 2);

                tvAddressDisplay.setText("📍 " + currentAddress);
            } else {
                currentCity = "Unknown";
                currentAddress = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
                tvAddressDisplay.setText("📍 " + currentAddress);
            }
        } catch (Exception e) {
            e.printStackTrace();
            currentCity = "Unknown";
            currentAddress = "Lat: " + location.getLatitude() + ", Lon: " + location.getLongitude();
            tvAddressDisplay.setText("📍 " + currentAddress);
        }
        
        if (etCityName != null && etCityName.getText().toString().isEmpty()) {
            etCityName.setText(currentCity);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mapView != null) mapView.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mapView != null) mapView.onPause();
    }

    private void submitReport(int userId, String city, String address, String contact, String category, String description, String photoUri, Button btnSendReport) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("user_id", userId);
            payload.put("city_name", city);
            payload.put("address", address);
            payload.put("contact_number", contact);
            payload.put("category", category);
            payload.put("description", description);
            payload.put("photo_uri", photoUri);

            ApiClient.post("submit_report.php", payload, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if ("success".equals(response.optString("status"))) {
                        Toast.makeText(ReportPhotoActivity.this, "Report Sent Successfully!", Toast.LENGTH_LONG).show();
                        finish();
                    } else {
                        Toast.makeText(ReportPhotoActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                        btnSendReport.setText("Send Report");
                        btnSendReport.setEnabled(true);
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(ReportPhotoActivity.this, error, Toast.LENGTH_SHORT).show();
                    btnSendReport.setText("Send Report");
                    btnSendReport.setEnabled(true);
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
