package com.civicbin.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OrgResetPasswordActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_reset_password);

        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        EditText etNewPassword = findViewById(R.id.etNewPassword);
        EditText etReEnterPassword = findViewById(R.id.etReEnterPassword);
        Button btnSave = findViewById(R.id.btnSave);

        btnSave.setOnClickListener(v -> {
            String p1 = etNewPassword.getText().toString();
            String p2 = etReEnterPassword.getText().toString();
            
            if (p1.isEmpty() || p2.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            } else if (!p1.equals(p2)) {
                Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Password reset successfully!", Toast.LENGTH_SHORT).show();
                finish();
            }
        });
    }
}
