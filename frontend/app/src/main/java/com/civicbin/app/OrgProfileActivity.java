package com.civicbin.app;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class OrgProfileActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_profile);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = findViewById(R.id.tvProfileEmail);

        String orgName = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getString("org_name", "Organization");
        String orgEmail = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getString("email", "org@example.com");

        tvProfileName.setText(orgName);
        tvProfileEmail.setText(orgEmail);
    }
}
