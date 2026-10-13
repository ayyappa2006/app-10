package com.civicbin.app;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OrgForgotPasswordActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_forgot_password);

        EditText etEmail = findViewById(R.id.etEmail);
        EditText etNewPassword = findViewById(R.id.etNewPassword);
        Button btnSave = findViewById(R.id.btnSave);
        TextView tvBackToLogin = findViewById(R.id.tvBackToLogin);

        btnSave.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String newPassword = etNewPassword.getText().toString().trim();
            
            if (email.isEmpty() || newPassword.isEmpty()) {
                Toast.makeText(this, "Please enter email and new password", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSave.setText("Saving...");
            btnSave.setEnabled(false);

            try {
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("email", email);
                payload.put("new_password", newPassword);

                ApiClient.post("org_reset_password.php", payload, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject response) {
                        btnSave.setText("Save");
                        btnSave.setEnabled(true);
                        
                        if ("success".equals(response.optString("status"))) {
                            Toast.makeText(OrgForgotPasswordActivity.this, "Password updated successfully!", Toast.LENGTH_SHORT).show();
                            finish();
                        } else {
                            Toast.makeText(OrgForgotPasswordActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        btnSave.setText("Save");
                        btnSave.setEnabled(true);
                        Toast.makeText(OrgForgotPasswordActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                btnSave.setText("Save");
                btnSave.setEnabled(true);
            }
        });

        tvBackToLogin.setOnClickListener(v -> {
            finish(); // Returns to OrgLoginActivity
        });
    }
}
