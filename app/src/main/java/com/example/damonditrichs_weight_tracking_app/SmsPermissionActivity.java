package com.example.damonditrichs_weight_tracking_app;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class SmsPermissionActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 1;
    private Button requestPermissionButton;
    private Button declinePermissionButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sms_permission);

        requestPermissionButton = findViewById(R.id.request_permission_button);
        declinePermissionButton = findViewById(R.id.decline_permission_button);

        requestPermissionButton.setOnClickListener(v -> requestSmsPermission());
        declinePermissionButton.setOnClickListener(v -> onSmsPermissionDenied());
    }

    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            onSmsPermissionGranted();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                onSmsPermissionGranted();
            } else {
                onSmsPermissionDenied();
            }
        }
    }

    private void onSmsPermissionGranted() {
        // Save SMS permission state
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.putBoolean("is_sms_permission_granted", true);
        editor.apply();

        Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DataDisplayActivity.class));
        finish();
    }

    private void onSmsPermissionDenied() {
        // Save SMS permission state
        SharedPreferences.Editor editor = getSharedPreferences("app_prefs", MODE_PRIVATE).edit();
        editor.putBoolean("is_sms_permission_granted", false);
        editor.apply();

        Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
        startActivity(new Intent(this, DataDisplayActivity.class));
        finish();
    }
}
