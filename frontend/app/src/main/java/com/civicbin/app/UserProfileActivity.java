package com.civicbin.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);

        TextView tvHeroName = findViewById(R.id.tvHeroName);
        TextView tvHeroReportCount = findViewById(R.id.tvHeroReportCount);

        TextView tvAccountDetails = findViewById(R.id.tvAccountDetails);
        TextView tvResetPassword = findViewById(R.id.tvResetPassword);
        TextView tvPrivacyPolicy = findViewById(R.id.tvPrivacyPolicy);
        TextView tvOrganizers = findViewById(R.id.tvOrganizers);
        TextView tvHelpSupport = findViewById(R.id.tvHelpSupport);
        Button btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE);
        String userName = prefs.getString("user_name", "Citizen");
        int userId = prefs.getInt("user_id", -1);
        
        tvHeroName.setText(userName);

        if (userId != -1) {
            ApiClient.get("get_user_reports.php?user_id=" + userId, new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(org.json.JSONObject response) {
                    try {
                        if ("success".equals(response.optString("status"))) {
                            org.json.JSONArray array = response.optJSONArray("data");
                            int count = (array != null) ? array.length() : 0;
                            tvHeroReportCount.setText(String.valueOf(count));
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

        tvAccountDetails.setOnClickListener(v -> startActivity(new Intent(this, UserDetailsActivity.class)));
        tvResetPassword.setOnClickListener(v -> startActivity(new Intent(this, UserResetPasswordActivity.class)));
        tvOrganizers.setOnClickListener(v -> startActivity(new Intent(this, OrganizersListActivity.class)));
        tvPrivacyPolicy.setOnClickListener(v -> startActivity(new Intent(this, UserPrivacyActivity.class)));
        tvHelpSupport.setOnClickListener(v -> startActivity(new Intent(this, UserHelpActivity.class)));

        btnLogout.setOnClickListener(v -> {
            getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).edit().clear().apply();
            Intent intent = new Intent(this, SplashActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });

        TextView navHome = findViewById(R.id.navHome);
        TextView navHistory = findViewById(R.id.navHistory);
        TextView navAi = findViewById(R.id.navAi);
        Button btnNavUpload = findViewById(R.id.btnNavUpload);

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

        navAi.setOnClickListener(v -> {
            startActivity(new Intent(this, UserAiActivity.class));
            overridePendingTransition(0, 0);
            finish();
        });
        
        btnNavUpload.setOnClickListener(v -> {
            startActivity(new Intent(this, UploadPhotoActivity.class));
        });
    }
}
