package com.civicbin.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class OrgLoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_org_login);

        EditText etOrgName = findViewById(R.id.etOrgName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnLogin = findViewById(R.id.btnLogin);
        TextView tvForgotPassword = findViewById(R.id.tvForgotPassword);
        TextView tvSignUp = findViewById(R.id.tvSignUp);

        btnLogin.setOnClickListener(v -> {
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnLogin.setText("Logging in...");
            btnLogin.setEnabled(false);

            try {
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("email", email);
                payload.put("password", password);

                ApiClient.post("org_login.php", payload, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject response) {
                        btnLogin.setText("Login");
                        btnLogin.setEnabled(true);
                        
                        if ("success".equals(response.optString("status"))) {
                            getSharedPreferences("CivicBinPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("org_id", response.optInt("org_id"))
                                .putString("org_name", response.optString("org_name"))
                                .putString("email", email)
                                .apply();
                            
                            startActivity(new Intent(OrgLoginActivity.this, OrgDashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(OrgLoginActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        btnLogin.setText("Login");
                        btnLogin.setEnabled(true);
                        Toast.makeText(OrgLoginActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                btnLogin.setText("Login");
                btnLogin.setEnabled(true);
            }
        });

        tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(OrgLoginActivity.this, OrgForgotPasswordActivity.class);
            startActivity(intent);
        });

        tvSignUp.setOnClickListener(v -> {
            Intent intent = new Intent(OrgLoginActivity.this, OrgSignupActivity.class);
            startActivity(intent);
        });
    }
}
