package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class UserAiActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_ai);

        TextView navHome = findViewById(R.id.navHome);
        TextView navHistory = findViewById(R.id.navHistory);
        TextView navProfile = findViewById(R.id.navProfile);
        ImageView btnNavUpload = findViewById(R.id.btnNavUpload);
        LinearLayout issuesContainer = findViewById(R.id.issuesContainer);

        navHome.setOnClickListener(v -> {
            startActivity(new Intent(this, UserDashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navHistory.setOnClickListener(v -> {
            startActivity(new Intent(this, UserHistoryActivity.class));
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
                        JSONArray pendingArray = new JSONArray();
                        
                        if (array != null) {
                            for(int i = 0; i < array.length(); i++) {
                                String status = array.getJSONObject(i).optString("status");
                                if("Pending".equals(status) || "Assigned".equals(status)) {
                                    pendingArray.put(array.getJSONObject(i));
                                }
                            }
                        }

                        if (pendingArray.length() == 0) {
                            TextView empty = new TextView(UserAiActivity.this);
                            empty.setText("You have no active pending issues.");
                            empty.setGravity(Gravity.CENTER);
                            empty.setPadding(0, 50, 0, 0);
                            container.addView(empty);
                            return;
                        }

                        for (int i = 0; i < pendingArray.length(); i++) {
                            JSONObject report = pendingArray.getJSONObject(i);
                            
                            // Card Container
                            LinearLayout card = new LinearLayout(UserAiActivity.this);
                            card.setOrientation(LinearLayout.HORIZONTAL);
                            card.setBackgroundResource(R.drawable.bg_rounded_white);
                            card.setPadding(32, 32, 32, 32);
                            card.setElevation(4f);
                            LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                            cardParams.setMargins(0, 0, 0, 32);
                            card.setLayoutParams(cardParams);

                            // Image
                            ImageView imageView = new ImageView(UserAiActivity.this);
                            imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                            imageView.setBackgroundResource(R.drawable.bg_rounded_white);
                            imageView.setClipToOutline(true);
                            String uriString = report.optString("photo_uri", "");
                            ApiClient.loadImage(imageView, uriString, getContentResolver());
                            card.addView(imageView);

                            // Details Container
                            LinearLayout details = new LinearLayout(UserAiActivity.this);
                            details.setOrientation(LinearLayout.VERTICAL);
                            details.setPadding(32, 0, 0, 0);
                            details.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                            TextView title = new TextView(UserAiActivity.this);
                            title.setText(report.optString("category", "General") + " Issue");
                            title.setTextColor(Color.parseColor("#0f172a"));
                            title.setTextSize(16f);
                            title.setTypeface(null, android.graphics.Typeface.BOLD);
                            details.addView(title);

                            TextView address = new TextView(UserAiActivity.this);
                            address.setText(report.optString("address", ""));
                            address.setTextColor(Color.parseColor("#64748b"));
                            address.setTextSize(12f);
                            details.addView(address);

                            card.addView(details);

                            // Action Buttons Container (Replaced with Arrow Icon)
                            LinearLayout actionContainer = new LinearLayout(UserAiActivity.this);
                            actionContainer.setOrientation(LinearLayout.VERTICAL);
                            actionContainer.setGravity(Gravity.CENTER_VERTICAL);
                            actionContainer.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));

                            ImageView arrowIcon = new ImageView(UserAiActivity.this);
                            arrowIcon.setImageResource(android.R.drawable.ic_media_play);
                            arrowIcon.setColorFilter(Color.parseColor("#cbd5e1"));
                            actionContainer.addView(arrowIcon);

                            card.addView(actionContainer);
                            
                            final int reportIdInt = (int) report.optLong("id", 0);
                            card.setOnClickListener(v -> {
                                Intent intent = new Intent(UserAiActivity.this, UserIssueDetailsActivity.class);
                                intent.putExtra("report_id", reportIdInt);
                                startActivity(intent);
                            });

                            container.addView(card);
                        }
                    } else {
                        android.widget.Toast.makeText(UserAiActivity.this, response.optString("message"), android.widget.Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                android.widget.Toast.makeText(UserAiActivity.this, error, android.widget.Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showEditDialog(long reportId, String city, String address, String contact, LinearLayout container) {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("Edit Report");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        final android.widget.EditText inputCity = new android.widget.EditText(this);
        inputCity.setHint("City Name");
        inputCity.setText(city);
        layout.addView(inputCity);

        final android.widget.EditText inputAddress = new android.widget.EditText(this);
        inputAddress.setHint("Address");
        inputAddress.setText(address);
        layout.addView(inputAddress);

        final android.widget.EditText inputContact = new android.widget.EditText(this);
        inputContact.setHint("Contact Number");
        inputContact.setText(contact);
        layout.addView(inputContact);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            try {
                JSONObject payload = new JSONObject();
                payload.put("report_id", reportId);
                payload.put("city_name", inputCity.getText().toString());
                payload.put("address", inputAddress.getText().toString());
                payload.put("contact_number", inputContact.getText().toString());
                
                ApiClient.post("update_report.php", payload, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(JSONObject response) {
                        if ("success".equals(response.optString("status"))) {
                            android.widget.Toast.makeText(UserAiActivity.this, "Updated successfully", android.widget.Toast.LENGTH_SHORT).show();
                            container.removeAllViews();
                            loadIssues(container); // Refresh
                        } else {
                            android.widget.Toast.makeText(UserAiActivity.this, response.optString("message"), android.widget.Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        android.widget.Toast.makeText(UserAiActivity.this, error, android.widget.Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void deleteReport(long id, LinearLayout container) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("report_id", id);
            ApiClient.post("delete_report.php", payload, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if ("success".equals(response.optString("status"))) {
                        android.widget.Toast.makeText(UserAiActivity.this, "Issue Deleted", android.widget.Toast.LENGTH_SHORT).show();
                        container.removeAllViews();
                        loadIssues(container); // Refresh
                    } else {
                        android.widget.Toast.makeText(UserAiActivity.this, response.optString("message"), android.widget.Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    android.widget.Toast.makeText(UserAiActivity.this, error, android.widget.Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
