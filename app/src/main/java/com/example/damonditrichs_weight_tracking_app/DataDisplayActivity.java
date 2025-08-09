package com.example.damonditrichs_weight_tracking_app;

import android.Manifest;
import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DataDisplayActivity extends AppCompatActivity {
    private static final int SMS_PERMISSION_CODE = 1;
    private AppDatabase db;
    private WeightAdapter adapter;
    private List<Weight> weightList;
    private SharedPreferences preferences;
    private TextView goalWeightDisplay;
    private TextView averageWeightDisplay;
    private TextView changeWeightDisplay;
    private String userPhoneNumber = "+1 555-123-4567";
    private Button toggleSmsButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_data_display);

        // Initialize Room database
        db = Room.databaseBuilder(getApplicationContext(), AppDatabase.class, "database-name")
                .fallbackToDestructiveMigration()
                .allowMainThreadQueries()
                .build();

        // Initialize SharedPreferences for storing app settings like goal weight
        preferences = getSharedPreferences("app_prefs", MODE_PRIVATE);

        // Retrieve all previously logged weights from the database
        weightList = db.weightDao().getAllWeights();

        // Set up RecyclerView for displaying weight logs
        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new WeightAdapter(weightList, db, this);
        recyclerView.setAdapter(adapter);

        // Initialize TextViews for displaying goal, average, and change stats
        goalWeightDisplay = findViewById(R.id.goal_weight_display);
        averageWeightDisplay = findViewById(R.id.average_weight_display);
        changeWeightDisplay = findViewById(R.id.change_weight_display);

        updateGoalWeightDisplay();
        updateWeightStats(); // Initialize stats display on load

        EditText bottomInput = findViewById(R.id.bottom_input);
        Button actionButton = findViewById(R.id.action_button);

        // Handle weight entry submission
        actionButton.setOnClickListener(v -> {
            String weightStr = bottomInput.getText().toString().trim();

            // Input validation: check for empty, invalid, or negative input
            if (weightStr.isEmpty()) {
                Toast.makeText(DataDisplayActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            float weightValue;
            try {
                weightValue = Float.parseFloat(weightStr);
            } catch (NumberFormatException e) {
                Toast.makeText(DataDisplayActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }

            if (weightValue < 0) {
                Toast.makeText(DataDisplayActivity.this, "Weight cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            String currentDate = new SimpleDateFormat("MM-dd-yyyy", Locale.getDefault()).format(new Date());

            Weight weight = new Weight();
            weight.weight = weightValue;
            weight.date = currentDate;

            // Try inserting weight into database and updating UI
            try {
                db.weightDao().insert(weight);
                weightList.add(weight);
                adapter.notifyItemInserted(weightList.size() - 1);
                bottomInput.setText("");

                updateWeightStats();

                checkAndSendSMS(weightValue);

                Toast.makeText(DataDisplayActivity.this, "Weight logged", Toast.LENGTH_SHORT).show();
            } catch (Exception ex) {
                Toast.makeText(DataDisplayActivity.this, "Failed to log weight", Toast.LENGTH_SHORT).show();
            }
        });

        // Opens dialog for setting goal weight
        Button settingsButton = findViewById(R.id.settings_button);
        settingsButton.setOnClickListener(v -> showGoalWeightDialog());

        // Opens dialog for toggling SMS permissions
        toggleSmsButton = findViewById(R.id.toggle_sms_button);
        toggleSmsButton.setOnClickListener(v -> showSmsPermissionDialog());

        updateSmsButtonState();
    }

    /**
     * Calculates and displays the average weight and the difference between
     * the first and most recent entry in the user's weight history.
     * If there are no entries, displays "No entries to display".
     */
    public void updateWeightStats() {
        if (weightList != null && !weightList.isEmpty()) {
            float total = 0f;
            for (Weight w : weightList) {
                total += w.weight;
            }
            float average = total / weightList.size();
            // Display the calculated average weight
            averageWeightDisplay.setText("Average weight: " + String.format("%.2f", average));

            float firstWeight = weightList.get(0).weight;
            float lastWeight = weightList.get(weightList.size() - 1).weight;
            float weightChange = lastWeight - firstWeight;
            // Display the weight change since the first entry
            changeWeightDisplay.setText("Change since first entry: " + String.format("%.2f", weightChange));
        }
    }

    /**
     * Displays a dialog for the user to grant or deny SMS permission.
     * Updates the SMS button state based on user selection.
     */
    private void showSmsPermissionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_sms_permission, null);
        builder.setView(dialogView);

        RadioGroup smsPermissionGroup = dialogView.findViewById(R.id.sms_permission_group);
        RadioButton smsPermissionYes = dialogView.findViewById(R.id.sms_permission_yes);
        RadioButton smsPermissionNo = dialogView.findViewById(R.id.sms_permission_no);
        Button smsPermissionButton = dialogView.findViewById(R.id.sms_permission_button);

        AlertDialog dialog = builder.create();

        smsPermissionButton.setOnClickListener(v -> {
            int selectedId = smsPermissionGroup.getCheckedRadioButtonId();
            if (selectedId == R.id.sms_permission_yes) {
                requestSmsPermission();
            } else if (selectedId == R.id.sms_permission_no) {
                updateSmsButtonState();
            }
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Requests SEND_SMS permission from the user if not already granted.
     */
    private void requestSmsPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, SMS_PERMISSION_CODE);
        } else {
            updateSmsButtonState();
        }
    }

    /**
     * Updates the toggleSmsButton text to reflect current SMS permission status.
     */
    private void updateSmsButtonState() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            toggleSmsButton.setText("SMS Permission ON");
        } else {
            toggleSmsButton.setText("SMS Permission OFF");
        }
    }

    /**
     * Checks if SMS alert should be sent upon logging weight,
     * and sends SMS if current weight is less than or equal to goal.
     */
    private void checkAndSendSMS(float weightValue) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            float goalWeight = preferences.getFloat("goal_weight", -1);
            if (goalWeight != -1 && weightValue <= goalWeight) {
                sendSMS(userPhoneNumber, "Congratulations you have hit your goal weight");
            }
        }
    }

    /**
     * Sends an SMS message to the given phone number.
     */
    private void sendSMS(String phoneNumber, String message) {
        SmsManager smsManager = SmsManager.getDefault();
        smsManager.sendTextMessage(phoneNumber, null, message, null, null);
    }

    /**
     * Displays a dialog for the user to set or change their goal weight.
     * Includes input validation for empty, invalid, and negative values.
     */
    private void showGoalWeightDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_goal_weight, null);
        builder.setView(dialogView);

        EditText goalInput = dialogView.findViewById(R.id.goal_input);
        Button goalButton = dialogView.findViewById(R.id.goal_button);

        AlertDialog dialog = builder.create();
        goalButton.setOnClickListener(v -> {
            String goalStr = goalInput.getText().toString().trim();

            // Enhanced input validation for goal weight
            if (goalStr.isEmpty()) {
                Toast.makeText(DataDisplayActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }
            float goalValue;
            try {
                goalValue = Float.parseFloat(goalStr);
            } catch (NumberFormatException e) {
                Toast.makeText(DataDisplayActivity.this, "Please enter a valid number", Toast.LENGTH_SHORT).show();
                return;
            }
            if (goalValue < 0) {
                Toast.makeText(DataDisplayActivity.this, "Goal weight cannot be negative", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences.Editor editor = preferences.edit();
            editor.putFloat("goal_weight", goalValue);
            editor.apply();
            updateGoalWeightDisplay();
            Toast.makeText(DataDisplayActivity.this, "Goal weight set", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });

        dialog.show();
    }

    /**
     * Updates the goal weight display text view with current goal weight.
     * If no goal has been set, shows "Goal weight: Not set".
     */
    private void updateGoalWeightDisplay() {
        float goalWeight = preferences.getFloat("goal_weight", -1);
        if (goalWeight != -1) {
            goalWeightDisplay.setText("Goal weight: " + goalWeight);
        } else {
            goalWeightDisplay.setText("Goal weight: Not set");
        }
    }

    /**
     * Handles the result of the SMS permission request and updates the UI accordingly.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == SMS_PERMISSION_CODE) {
            updateSmsButtonState();
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "SMS Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "SMS Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}