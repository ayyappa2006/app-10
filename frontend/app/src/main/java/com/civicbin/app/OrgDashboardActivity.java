package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class OrgDashboardActivity extends AppCompatActivity {

    private TextView tvOpenIssues;
    private TextView tvResolvedIssues;
    private LinearLayout recentActivityContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_dashboard);

        tvOpenIssues = findViewById(R.id.tvOpenIssues);
        tvResolvedIssues = findViewById(R.id.tvResolvedIssues);
        recentActivityContainer = findViewById(R.id.recentActivityContainer);

        TextView navIssues = findViewById(R.id.navIssues);
        TextView navSettings = findViewById(R.id.navSettings);
        TextView navCompleted = findViewById(R.id.navCompleted);

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
        
        navCompleted.setOnClickListener(v -> {
            startActivity(new Intent(this, OrgCompletedActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadDashboardData();
        fetchCleanlinessScore();
    }

    private void fetchCleanlinessScore() {
        ApiClient.get("get_cleanliness_score.php", new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
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

    private void loadDashboardData() {
        android.content.SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", android.content.Context.MODE_PRIVATE);
        int orgId = prefs.getInt("org_id", 0);
        ApiClient.get("get_reports.php?org_id=" + orgId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                if ("success".equals(response.optString("status"))) {
                    JSONArray array = response.optJSONArray("data");
                    if (array != null) {
                        int openCount = 0;
                        int resolvedCount = 0;
                        
                        recentActivityContainer.removeAllViews();

                        // Limit to top 5 recent activities for dashboard
                        int displayCount = Math.min(array.length(), 5);
                        int myOrgId = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getInt("org_id", -1);

                        for (int i = 0; i < array.length(); i++) {
                            try {
                                JSONObject report = array.getJSONObject(i);
                                String status = report.optString("status", "Pending");
                                int resolvedBy = report.optInt("resolved_by_org_id", -1);

                                if ("Pending".equals(status)) {
                                    openCount++;
                                    if (i < displayCount) {
                                        addRecentItem(report);
                                    }
                                } else if ("Completed".equals(status) && resolvedBy == myOrgId) {
                                    resolvedCount++;
                                    if (i < displayCount) {
                                        addRecentItem(report);
                                    }
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }

                        tvOpenIssues.setText(String.valueOf(openCount));
                        tvResolvedIssues.setText(String.valueOf(resolvedCount));
                    }
                }
            }

            @Override
            public void onError(String error) {
                Toast.makeText(OrgDashboardActivity.this, "Failed to load dashboard: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void addRecentItem(JSONObject report) {
        String city = report.optString("city_name", "Unknown");
        String status = report.optString("status", "Pending");
        long id = report.optLong("id", 0);

        LinearLayout card = new LinearLayout(this);
        card.setOrientation(LinearLayout.HORIZONTAL);
        card.setBackgroundResource(R.drawable.bg_rounded_white);
        card.setPadding(32, 32, 32, 32);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(0, 0, 0, 16);
        card.setLayoutParams(params);
        card.setGravity(Gravity.CENTER_VERTICAL);

        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        textLayout.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1f));

        TextView tvTitle = new TextView(this);
        tvTitle.setText(status.equals("Pending") ? "New Issue: " + city : "Resolved: " + city);
        tvTitle.setTextColor(Color.parseColor("#0f172a"));
        tvTitle.setTextSize(14f);

        textLayout.addView(tvTitle);
        card.addView(textLayout);

        // Add Delete Button directly on dashboard recent item
        Button btnDelete = new Button(this);
        btnDelete.setText("Delete");
        btnDelete.setBackgroundColor(Color.parseColor("#ef4444"));
        btnDelete.setTextColor(Color.WHITE);
        btnDelete.setPadding(16, 0, 16, 0);
        btnDelete.setOnClickListener(v -> confirmAndDelete(id));

        card.addView(btnDelete);
        recentActivityContainer.addView(card);
        
        card.setOnClickListener(v -> {
            Intent intent = new Intent(this, OrgIssueDetailsActivity.class);
            intent.putExtra("report_id", id);
            intent.putExtra("city_name", city);
            intent.putExtra("address", report.optString("address", ""));
            intent.putExtra("contact_number", report.optString("contact_number", ""));
            intent.putExtra("photo_uri", report.optString("photo_uri", ""));
            intent.putExtra("status", status);
            startActivity(intent);
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
                        Toast.makeText(OrgDashboardActivity.this, "Issue Deleted", Toast.LENGTH_SHORT).show();
                        loadDashboardData(); // Refresh dashboard
                    } else {
                        Toast.makeText(OrgDashboardActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(OrgDashboardActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
