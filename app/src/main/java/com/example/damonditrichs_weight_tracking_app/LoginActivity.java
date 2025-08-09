package com.example.damonditrichs_weight_tracking_app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.room.Room;

public class LoginActivity extends AppCompatActivity {
    private EditText usernameEditText;
    private EditText passwordEditText;
    private TextView loginTitle;
    private Button loginButton;
    private Button createAccountButton;
    private AppDatabase db;
    private boolean isCreatingAccount = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        SharedPreferences preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        usernameEditText = findViewById(R.id.username);
        passwordEditText = findViewById(R.id.password);
        loginTitle = findViewById(R.id.loginTitle);
        loginButton = findViewById(R.id.login_button);
        createAccountButton = findViewById(R.id.create_account_button);

        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please enter a username and password", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isCreatingAccount) {
                User newUser = new User();
                newUser.username = username;
                newUser.password = password;
                db.userDao().insert(newUser);
                Toast.makeText(LoginActivity.this, "Account created. Please login.", Toast.LENGTH_SHORT).show();
                switchToLoginMode();
            } else {
                User user = db.userDao().getUser(username, password);
                if (user != null) {
                    // Save login state
                    SharedPreferences.Editor editor = preferences.edit();
                    editor.putBoolean("is_logged_in", true);
                    editor.putString("username", username);
                    editor.apply();

                    Toast.makeText(LoginActivity.this, "Login successful", Toast.LENGTH_SHORT).show();

                    boolean isSmsPermissionGranted = preferences.getBoolean("is_sms_permission_granted", false);
                    if (isSmsPermissionGranted) {
                        // Redirect to main activity
                        Intent intent = new Intent(LoginActivity.this, DataDisplayActivity.class);
                        startActivity(intent);
                    } else {
                        // Redirect to SMS permission activity
                        Intent intent = new Intent(LoginActivity.this, SmsPermissionActivity.class);
                        startActivity(intent);
                    }
                    finish();
                } else {
                    Toast.makeText(LoginActivity.this, "Invalid credentials. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        createAccountButton.setOnClickListener(v -> {
            if (isCreatingAccount) {
                switchToLoginMode();
            } else {
                switchToCreateAccountMode();
            }
        });
    }

    private void switchToLoginMode() {
        isCreatingAccount = false;
        loginTitle.setText("Login");
        loginButton.setText("Login");
        createAccountButton.setText("Create Account");
    }

    private void switchToCreateAccountMode() {
        isCreatingAccount = true;
        loginTitle.setText("Create Account");
        loginButton.setText("Create Account");
        createAccountButton.setText("Back to Login");
    }
}
