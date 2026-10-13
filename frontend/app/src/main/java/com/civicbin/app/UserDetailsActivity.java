package com.civicbin.app;

import android.content.Context;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class UserDetailsActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_details);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvProfileName = findViewById(R.id.tvProfileName);
        TextView tvProfileEmail = findViewById(R.id.tvProfileEmail);

        String userName = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getString("user_name", "Citizen");
        String userEmail = getSharedPreferences("CivicBinPrefs", Context.MODE_PRIVATE).getString("user_email", "user@example.com");

        tvProfileName.setText(userName);
        tvProfileEmail.setText(userEmail);
    }
}
