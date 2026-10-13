package com.civicbin.app;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import org.osmdroid.config.Configuration;
import org.osmdroid.util.GeoPoint;
import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Marker;

public class UserDashboardActivity extends AppCompatActivity {

    private static final int LOCATION_PERMISSION_REQUEST_CODE = 1;
    private MapView map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Load OSMDroid configuration before inflating layout
        Context ctx = getApplicationContext();
        Configuration.getInstance().load(ctx, PreferenceManager.getDefaultSharedPreferences(ctx));
        Configuration.getInstance().setUserAgentValue(ctx.getPackageName());

        setContentView(R.layout.activity_user_dashboard);

        map = findViewById(R.id.mapView);
        map.setMultiTouchControls(true);

        Button btnUploadPhoto = findViewById(R.id.btnUploadPhoto);
        btnUploadPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(this, UploadPhotoActivity.class);
            startActivity(intent);
        });

        Button btnNavUpload = findViewById(R.id.btnNavUpload);
        if(btnNavUpload != null) {
            btnNavUpload.setOnClickListener(v -> {
                startActivity(new Intent(this, UploadPhotoActivity.class));
            });
        }

        TextView navAi = findViewById(R.id.navAi);
        navAi.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserAiActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        TextView navHistory = findViewById(R.id.navHistory);
        navHistory.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserHistoryActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        TextView navProfile = findViewById(R.id.navProfile);
        navProfile.setOnClickListener(v -> {
            Intent intent = new Intent(this, UserProfileActivity.class);
            startActivity(intent);
            overridePendingTransition(0, 0);
            finish();
        });

        checkLocationPermission();
        fetchReportCount();
        fetchCleanlinessScore();
    }

    private void fetchCleanlinessScore() {
        ApiClient.get("get_cleanliness_score.php", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                try {
                    if ("success".equals(response.optString("status"))) {
                        int score = response.optInt("score", 100);
                        TextView tvCleanScore = findViewById(R.id.tvCleanScore);
                        android.widget.ProgressBar progressCleanliness = findViewById(R.id.progressCleanliness);
                        
                        if(tvCleanScore != null) tvCleanScore.setText(String.valueOf(score));
                        if(progressCleanliness != null) progressCleanliness.setProgress(score);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {
                // Ignore
            }
        });
    }

    private void fetchReportCount() {
        int userId = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        TextView tvReportCount = findViewById(R.id.tvReportCount);
        ApiClient.get("get_user_reports.php?user_id=" + userId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(org.json.JSONObject response) {
                try {
                    if ("success".equals(response.optString("status"))) {
                        org.json.JSONArray array = response.optJSONArray("data");
                        int count = (array != null) ? array.length() : 0;
                        if(tvReportCount != null) {
                            tvReportCount.setText(String.valueOf(count));
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(String error) {
                // Ignore
            }
        });
    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
        } else {
            setupMap();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                setupMap();
            } else {
                Toast.makeText(this, "Location permission denied", Toast.LENGTH_SHORT).show();
                // Fallback location
                map.getController().setZoom(15.0);
                map.getController().setCenter(new GeoPoint(37.7749, -122.4194));
            }
        }
    }

    private void setupMap() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnownLocation == null) {
                lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            }

            if (lastKnownLocation != null) {
                GeoPoint startPoint = new GeoPoint(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());
                map.getController().setZoom(15.0);
                map.getController().setCenter(startPoint);

                Marker startMarker = new Marker(map);
                startMarker.setPosition(startPoint);
                startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM);
                startMarker.setTitle("You are here");
                map.getOverlays().add(startMarker);
            }

            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000, 10, new LocationListener() {
                @Override
                public void onLocationChanged(@NonNull Location location) {
                    GeoPoint point = new GeoPoint(location.getLatitude(), location.getLongitude());
                    map.getController().animateTo(point);
                }
            });
        }
    }
}
