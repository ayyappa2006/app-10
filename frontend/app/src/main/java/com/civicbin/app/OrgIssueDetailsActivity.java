package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class OrgIssueDetailsActivity extends AppCompatActivity {

    private long reportId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_issue_details);

        reportId = getIntent().getLongExtra("report_id", -1);
        if (reportId == -1) {
            Toast.makeText(this, "Invalid Issue", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        loadDetailsFromIntent();

        Button btnWorkerAssigned = findViewById(R.id.btnWorkerAssigned);
        Button btnCompleted = findViewById(R.id.btnMarkCompleted);
        Button btnDelete = findViewById(R.id.btnDelete);

        btnWorkerAssigned.setOnClickListener(v -> updateStatus("Assigned"));
        btnCompleted.setOnClickListener(v -> updateStatus("Completed"));
        btnDelete.setOnClickListener(v -> confirmAndDelete());
    }

    private void loadDetailsFromIntent() {
        try {
            ImageView ivDetailPhoto = findViewById(R.id.ivDetailPhoto);
            String uriString = getIntent().getStringExtra("photo_uri");
            ApiClient.loadImage(ivDetailPhoto, uriString, getContentResolver());

            TextView tvTitle = findViewById(R.id.tvDetailTitle);
            String category = getIntent().getStringExtra("category");
            if(category == null || category.isEmpty()) category = "General";
            tvTitle.setText(category + " Issue");

            TextView tvAddress = findViewById(R.id.tvDetailAddress);
            tvAddress.setText(getIntent().getStringExtra("address"));

            TextView tvPhone = findViewById(R.id.tvDetailPhone);
            tvPhone.setText(getIntent().getStringExtra("contact_number"));

            TextView tvStatus = findViewById(R.id.tvDetailStatus);
            String status = getIntent().getStringExtra("status");
            if (status == null) status = "Pending";
            
            tvStatus.setText(status);
            if ("Pending".equals(status) || "Assigned".equals(status)) {
                tvStatus.setTextColor(Color.parseColor("#ea580c"));
                tvStatus.setBackgroundColor(Color.parseColor("#ffedd5"));
            } else {
                tvStatus.setTextColor(Color.parseColor("#0ba360"));
                tvStatus.setBackgroundColor(Color.parseColor("#dcfce7"));
            }

            ImageView iconReceived = findViewById(R.id.iconReceived);
            ImageView iconAssigned = findViewById(R.id.iconAssigned);
            ImageView iconCompleted = findViewById(R.id.iconCompleted);

            iconReceived.setImageResource(android.R.drawable.checkbox_on_background);
            iconReceived.setColorFilter(Color.parseColor("#0ba360"));

            if ("Assigned".equals(status) || "Completed".equals(status)) {
                iconAssigned.setImageResource(android.R.drawable.checkbox_on_background);
                iconAssigned.setColorFilter(Color.parseColor("#0ba360"));
            } else {
                iconAssigned.setImageResource(android.R.drawable.checkbox_off_background);
                iconAssigned.setColorFilter(Color.parseColor("#cbd5e1"));
            }

            if ("Completed".equals(status)) {
                iconCompleted.setImageResource(android.R.drawable.checkbox_on_background);
                iconCompleted.setColorFilter(Color.parseColor("#0ba360"));
            } else {
                iconCompleted.setImageResource(android.R.drawable.checkbox_off_background);
                iconCompleted.setColorFilter(Color.parseColor("#cbd5e1"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateStatus(String newStatus) {
        try {
            int orgId = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getInt("org_id", -1);
            JSONObject payload = new JSONObject();
            payload.put("report_id", reportId);
            payload.put("status", newStatus);
            payload.put("org_id", orgId);

            ApiClient.post("update_report_status.php", payload, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if ("success".equals(response.optString("status"))) {
                        if ("Completed".equals(newStatus)) {
                            Intent intent = new Intent(OrgIssueDetailsActivity.this, OrgCompletedActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                        } else {
                            // Update local intent data and refresh UI
                            getIntent().putExtra("status", newStatus);
                            loadDetailsFromIntent();
                            Toast.makeText(OrgIssueDetailsActivity.this, "Status updated to " + newStatus, Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(OrgIssueDetailsActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(OrgIssueDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void confirmAndDelete() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Delete Issue")
            .setMessage("Are you sure you want to delete this issue?")
            .setPositiveButton("Delete", (dialog, which) -> deleteIssue())
            .setNegativeButton("Cancel", null)
            .show();
    }

    private void deleteIssue() {
        try {
            JSONObject payload = new JSONObject();
            payload.put("report_id", reportId);

            ApiClient.post("delete_report.php", payload, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    if ("success".equals(response.optString("status"))) {
                        Toast.makeText(OrgIssueDetailsActivity.this, "Issue Deleted", Toast.LENGTH_SHORT).show();
                        finish();
                    } else {
                        Toast.makeText(OrgIssueDetailsActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(String error) {
                    Toast.makeText(OrgIssueDetailsActivity.this, error, Toast.LENGTH_SHORT).show();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
