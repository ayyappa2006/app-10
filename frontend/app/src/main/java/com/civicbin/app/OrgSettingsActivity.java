package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OrgSettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_settings);

        TextView tvHeroName = findViewById(R.id.tvHeroName);
        TextView tvHeroReportCount = findViewById(R.id.tvHeroReportCount);

        TextView tvProfile = findViewById(R.id.tvProfile);
        TextView tvResetPassword = findViewById(R.id.tvResetPassword);
        TextView tvMessages = findViewById(R.id.tvMessages);
        TextView tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);
        TextView tvHelpSupport = findViewById(R.id.tvHelpSupport);
        Button btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE);
        String orgName = prefs.getString("org_name", "Organization");
        int orgId = prefs.getInt("org_id", -1);
        
        tvHeroName.setText(orgName);

        if (orgId != -1) {
            ApiClient.get("get_reports.php?org_id=" + orgId, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(org.json.JSONObject response) {
                    try {
                        if ("success".equals(response.optString("status"))) {
                            org.json.JSONArray array = response.optJSONArray("data");
                            int completedCount = 0;
                            if (array != null) {
                                for(int i=0; i<array.length(); i++) {
                                    if("Completed".equals(array.getJSONObject(i).optString("status"))) {
                                        completedCount++;
                                    }
                                }
                            }
                            tvHeroReportCount.setText(String.valueOf(completedCount));
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error) {
                    System.err.println("Error: " + error);
                }
            });
        }

        tvProfile.setOnClickListener(v -> startActivity(new Intent(this, OrgProfileActivity.class)));
        tvResetPassword.setOnClickListener(v -> startActivity(new Intent(this, OrgResetPasswordActivity.class)));
        tvMessages.setOnClickListener(v -> startActivity(new Intent(this, OrgChatListActivity.class)));
        tvPrivacyPolicy.setOnClickListener(v -> startActivity(new Intent(this, OrgPrivacyActivity.class)));
        tvHelpSupport.setOnClickListener(v -> startActivity(new Intent(this, OrgHelpActivity.class)));

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        TextView navDashboard = findViewById(R.id.navDashboard);
        TextView navIssues = findViewById(R.id.navIssues);
        TextView navCompleted = findViewById(R.id.navCompleted);

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
        
        navCompleted.setOnClickListener(v -> {
            startActivity(new Intent(this, OrgCompletedActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
    }
}
