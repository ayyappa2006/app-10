package com.civicbin.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        EditText etName = findViewById(R.id.etName);
        EditText etEmail = findViewById(R.id.etEmail);
        EditText etPassword = findViewById(R.id.etPassword);
        Button btnSignup = findViewById(R.id.btnSignup);
        TextView tvLogin = findViewById(R.id.tvLogin);

        btnSignup.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String password = etPassword.getText().toString().trim();

            if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            btnSignup.setText("Creating...");
            btnSignup.setEnabled(false);

            try {
                org.json.JSONObject payload = new org.json.JSONObject();
                payload.put("name", name);
                payload.put("email", email);
                payload.put("password", password);

                ApiClient.post("user_signup.php", payload, new ApiClient.ApiCallback() {
                    @Override
                    public void onSuccess(org.json.JSONObject response) {
                        btnSignup.setText("Create Account");
                        btnSignup.setEnabled(true);

                        if ("success".equals(response.optString("status"))) {
                            getSharedPreferences("CivicBinPrefs", MODE_PRIVATE)
                                .edit()
                                .putInt("user_id", response.optInt("user_id"))
                                .putString("user_name", response.optString("name"))
                                .apply();
                            
                            startActivity(new Intent(SignupActivity.this, UserDashboardActivity.class));
                            finish();
                        } else {
                            Toast.makeText(SignupActivity.this, response.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onError(String error) {
                        btnSignup.setText("Create Account");
                        btnSignup.setEnabled(true);
                        Toast.makeText(SignupActivity.this, error, Toast.LENGTH_SHORT).show();
                    }
                });
            } catch (Exception e) {
                e.printStackTrace();
                btnSignup.setText("Create Account");
                btnSignup.setEnabled(true);
            }
        });

        tvLogin.setOnClickListener(v -> {
            finish(); // Returns to LoginActivity
        });
    }
}
