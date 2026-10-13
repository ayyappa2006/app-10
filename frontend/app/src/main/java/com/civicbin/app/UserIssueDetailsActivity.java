package com.civicbin.app;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class UserIssueDetailsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_issue_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        int reportId = getIntent().getIntExtra("report_id", -1);
        if (reportId == -1) {
            Toast.makeText(this, "Invalid report ID", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE);
        int userId = prefs.getInt("user_id", -1);

        ApiClient.get("get_user_reports.php?user_id=" + userId, new ApiClient.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if ("success".equals(response.getString("status"))) {
                        JSONArray data = response.getJSONArray("data");
                        for (int i = 0; i < data.length(); i++) {
                            JSONObject report = data.getJSONObject(i);
                            if (report.getInt("id") == reportId) {
                                runOnUiThread(() -> updateUI(report));
                                return;
                            }
                        }
                        runOnUiThread(() -> {
                            Toast.makeText(UserIssueDetailsActivity.this, "Issue not found", Toast.LENGTH_SHORT).show();
                            finish();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserIssueDetailsActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void updateUI(JSONObject report) {
        try {
            TextView tvCategory = findViewById(R.id.tvCategory);
            TextView tvAddress = findViewById(R.id.tvAddress);
            ImageView iconReceived = findViewById(R.id.iconReceived);
            ImageView iconAssigned = findViewById(R.id.iconAssigned);
            ImageView iconCompleted = findViewById(R.id.iconCompleted);

            String category = report.optString("category", "General");
            if(category.isEmpty() || category.equals("null")) category = "General";
            tvCategory.setText(category + " Issue");
            tvAddress.setText(report.optString("address", ""));

            String status = report.optString("status", "Pending");

            // All reports have been received
            iconReceived.setImageResource(android.R.drawable.checkbox_on_background);
            iconReceived.setColorFilter(Color.parseColor("#0ba360"));

            if (status.equals("Assigned") || status.equals("Completed")) {
                iconAssigned.setImageResource(android.R.drawable.checkbox_on_background);
                iconAssigned.setColorFilter(Color.parseColor("#0ba360"));
            }

            if (status.equals("Completed")) {
                iconCompleted.setImageResource(android.R.drawable.checkbox_on_background);
                iconCompleted.setColorFilter(Color.parseColor("#0ba360"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
