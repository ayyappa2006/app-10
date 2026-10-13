package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONArray;
import org.json.JSONObject;

public class OrgCompletedActivity extends AppCompatActivity {

    private LinearLayout issuesContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_completed);

        TextView navDashboard = findViewById(R.id.navDashboard);
        TextView navIssues = findViewById(R.id.navIssues);
        TextView navSettings = findViewById(R.id.navSettings);
        issuesContainer = findViewById(R.id.issuesContainer);

        navDashboard.setOnClickListener(v -> {
            startActivity(new Intent(this, OrgDashboardActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navIssues.setOnClickListener(v -> {
            startActivity(new Intent(this, OrgIssuesActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });

        navSettings.setOnClickListener(v -> {
            startActivity(new Intent(this, OrgSettingsActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadCompletedIssues();
    }

    private void loadCompletedIssues() {
        issuesContainer.removeAllViews();

        android.content.SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", android.content.Context.MODE_PRIVATE);
        int orgId = prefs.getInt("org_id", 0);
        ApiClient.get("get_reports.php?org_id=" + orgId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if ("success".equals(response.optString("status"))) {
                        JSONArray array = response.optJSONArray("data");
                        boolean found = false;

                        int myOrgId = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getInt("org_id", -1);

                        if (array != null) {
                            for (int i = 0; i < array.length(); i++) {
                                JSONObject report = array.getJSONObject(i);
                                String statusStr = report.optString("status", "Pending");
                                int resolvedBy = report.optInt("resolved_by_org_id", -1);

                                if (!"Completed".equals(statusStr) || resolvedBy != myOrgId) {
                                    continue; // Skip if it is not completed or not resolved by this org
                                }

                                found = true;
                                final long reportId = report.optLong("id", 0);
                                String city = report.optString("city_name", "");
                                String addressStr = report.optString("address", "");
                                String photoUri = report.optString("photo_uri", "");

                                // Card Container
                                LinearLayout card = new LinearLayout(OrgCompletedActivity.this);
                                card.setOrientation(LinearLayout.HORIZONTAL);
                                card.setBackgroundResource(R.drawable.bg_rounded_white);
                                card.setPadding(32, 32, 32, 32);
                                card.setElevation(4f);
                                LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                cardParams.setMargins(0, 0, 0, 32);
                                card.setLayoutParams(cardParams);

                                // Image
                                ImageView imageView = new ImageView(OrgCompletedActivity.this);
                                imageView.setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                                imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                imageView.setBackgroundResource(R.drawable.bg_rounded_white);
                                imageView.setClipToOutline(true);
                                ApiClient.loadImage(imageView, photoUri, getContentResolver());
                                card.addView(imageView);

                                // Details Container
                                LinearLayout details = new LinearLayout(OrgCompletedActivity.this);
                                details.setOrientation(LinearLayout.VERTICAL);
                                details.setPadding(32, 0, 0, 0);
                                details.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

                                TextView title = new TextView(OrgCompletedActivity.this);
                                title.setText(city + " Issue");
                                title.setTextColor(Color.parseColor("#0f172a"));
                                title.setTextSize(16f);
                                title.setTypeface(null, android.graphics.Typeface.BOLD);
                                details.addView(title);

                                TextView statusBadge = new TextView(OrgCompletedActivity.this);
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

                                TextView address = new TextView(OrgCompletedActivity.this);
                                address.setText(addressStr);
                                address.setTextColor(Color.parseColor("#64748b"));
                                address.setTextSize(12f);
                                details.addView(address);

                                // Delete Button
                                Button btnDelete = new Button(OrgCompletedActivity.this);
                                btnDelete.setText("Delete");
                                btnDelete.setBackgroundColor(Color.parseColor("#ef4444"));
                                btnDelete.setTextColor(Color.WHITE);
                                btnDelete.setPadding(16, 0, 16, 0);
                                LinearLayout.LayoutParams btnParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
                                btnParams.setMargins(0, 16, 0, 0);
                                btnDelete.setLayoutParams(btnParams);
                                
                                btnDelete.setOnClickListener(v -> confirmAndDelete(reportId));
                                details.addView(btnDelete);

                                card.addView(details);
                                issuesContainer.addView(card);
                            }
                        }

                        if (!found) {
                            TextView empty = new TextView(OrgCompletedActivity.this);
                            empty.setText("No completed issues found.");
                            empty.setGravity(Gravity.CENTER);
                            empty.setPadding(0, 50, 0, 0);
                            issuesContainer.addView(empty);
                        }
                    } else {
                        Toast.makeText(OrgCompletedActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(OrgCompletedActivity.this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void confirmAndDelete(long id) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Issue")
            .setMessage("Are you sure you want to delete this issue?")
            .setPositiveButton("Delete", (dialog, which) -> deleteReport(id))
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteReport(long id) {
        try {
            JSONObject payload = new JSONObject();
            payload.put("report_id", id);
            ApiClient.post("delete_report.php", payload, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if ("success".equals(response.optString("status"))) {
                        Toast.makeText(OrgCompletedActivity.this, "Issue Deleted", Toast.LENGTH_SHORT).show();
                        loadCompletedIssues(); // Refresh
                    } else {
                        Toast.makeText(OrgCompletedActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(OrgCompletedActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
