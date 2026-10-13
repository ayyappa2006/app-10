package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserHistoryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_history);

        TextView navHome = findViewById(R.id.navHome);
        TextView navAi = findViewById(R.id.navAi);
        TextView navProfile = findViewById(R.id.navProfile);
        Button btnNavUpload = findViewById(R.id.btnNavUpload);
        LinearLayout issuesContainer = findViewById(R.id.issuesContainer);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navAi.setOnClickListener(v -> {
            startActivity(new Intent(this, UserAiActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        
        navProfile.setOnClickListener(v -> {
            startActivity(new Intent(this, UserProfileActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        btnNavUpload.setOnClickListener(v -> {
            startActivity(new Intent(this, UploadPhotoActivity.class));
        });

        loadIssues(issuesContainer);
    }

    private void loadIssues(LinearLayout container) {
        int userId = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getInt("user_id", -1);
        
        ApiClient.get("get_user_reports.php?user_id=" + userId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if ("success".equals(response.optString("status"))) {
                        JSONArray array = response.optJSONArray("data");
                        JSONArray completedArray = new JSONArray();
                        
                        if (array != null) {
                            for(int i = 0; i < array.length(); i++) {
                                if("Completed".equals(array.getJSONObject(i).optString("status"))) {
                                    completedArray.put(array.getJSONObject(i));
                                }
                            }
                        }

                        if (completedArray.length() == 0) {
                            TextView empty = new TextView(UserHistoryActivity.this);
                            empty.setText("You have no completed issues yet.");
                            empty.setGravity(Gravity.CENTER);
                            empty.setPadding(0, 50, 0, 0);
                            container.addView(empty);
                            return;
                        }

                        for (int i = 0; i < completedArray.length(); i++) {
                            JSONObject report = completedArray.getJSONObject(i);
                            
                            // Card Container
                            LinearLayout card = new LinearLayout(UserHistoryActivity.this);
                            card.setOrientation(LinearLayout.HORIZONTAL);
                            card.setBackgroundResource(R.drawable.bg_rounded_white);
                            card.setPadding(32, 32, 32, 32);
                            card.setElevation(4f);
                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cardParams.setMargins(0, 0, 0, 32);
                            card.setLayoutParams(cardParams);

                            // Image
                            ImageView imageView = new ImageView(UserHistoryActivity.this);
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setBackgroundResource(R.drawable.bg_rounded_white);
                            imageView.setClipToOutline(true);
                            String uriString = report.optString("photo_uri", "");
                            ApiClient.loadImage(imageView, uriString, getContentResolver());
                            card.addView(imageView);

                            // Details Container
                            LinearLayout details = new LinearLayout(UserHistoryActivity.this);
                            details.setOrientation(LinearLayout.VERTICAL);
                            details.setPadding(32, 0, 0, 0);
                            details.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                            TextView title = new TextView(UserHistoryActivity.this);
                            title.setText(report.optString("city_name", "Unknown") + " Issue");
                            title.setTextColor(Color.parseColor("#0f172a"));
                            title.setTextSize(16f);
                            title.setTypeface(null, android.graphics.Typeface.BOLD);
                            details.addView(title);
                            
                            TextView statusBadge = new TextView(UserHistoryActivity.this);
                            statusBadge.setText("Completed");
                            statusBadge.setTextSize(10f);
                            statusBadge.setTypeface(null, android.graphics.Typeface.BOLD);
                            statusBadge.setPadding(16, 4, 16, 4);
                            statusBadge.setTextColor(Color.parseColor("#0ba360"));
                            statusBadge.setBackgroundColor(Color.parseColor("#dcfce7"));
                            LinearLayout.LayoutParams badgeParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            badgeParams.setMargins(0, 8, 0, 8);
                            statusBadge.setLayoutParams(badgeParams);
                            details.addView(statusBadge);

                            TextView address = new TextView(UserHistoryActivity.this);
                            address.setText(report.optString("address", ""));
                            address.setTextColor(Color.parseColor("#64748b"));
                            address.setTextSize(12f);
                            details.addView(address);

                            card.addView(details);

                            // Add Delete Button
                            android.widget.Button btnDelete = new android.widget.Button(UserHistoryActivity.this);
                            btnDelete.setText("Delete");
                            btnDelete.setBackgroundColor(Color.parseColor("#ef4444"));
                            btnDelete.setTextColor(Color.WHITE);
                            btnDelete.setPadding(16, 0, 16, 0);
                            final long reportId = report.optLong("id", 0);
                            btnDelete.setOnClickListener(v -> deleteReport(reportId, container));
                            card.addView(btnDelete);

                            container.addView(card);
                        }
                    } else {
                        android.widget.Toast.makeText(UserHistoryActivity.this, response.optString("message"), android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                android.widget.Toast.makeText(UserHistoryActivity.this, error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void deleteReport(long id, LinearLayout container) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("report_id", id);
            ApiClient.post("delete_report.php", payload, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if ("success".equals(response.optString("status"))) {
                        android.widget.Toast.makeText(UserHistoryActivity.this, "Issue Deleted", android.widget.Toast.LENGTH_SHORT).show();
                        container.removeAllViews();
                        loadIssues(container); // Refresh
                    } else {
                        android.widget.Toast.makeText(UserHistoryActivity.this, response.optString("message"), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    android.widget.Toast.makeText(UserHistoryActivity.this, error, android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
