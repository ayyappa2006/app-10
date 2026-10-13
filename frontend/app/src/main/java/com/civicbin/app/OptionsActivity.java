package com.civicbin.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OptionsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_options);

        Button btnOrganization = findViewById(R.id.btnOrganization);
        Button btnUser = findViewById(R.id.btnUser);

        btnOrganization.setOnClickListener(v -> {
            Intent intent = new Intent(OptionsActivity.this, OrgLoginActivity.class);
            startActivity(intent);
        });

        btnUser.setOnClickListener(v -> {
            Intent intent = new Intent(OptionsActivity.this, LoginActivity.class);
            startActivity(intent);
        });
    }
}
