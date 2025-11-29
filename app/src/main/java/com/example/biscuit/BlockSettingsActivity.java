package com.example.biscuit;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

public class BlockSettingsActivity extends AppCompatActivity {

    ImageView imgAppIcon;
    TextView tvAppName, tvStartTimeLabel, tvEndTimeLabel;
    Switch switchBlock;
    TimePicker timeStart, timeEnd;
    Button btnSave;

    String packageName;
    String appName;
    Drawable appIcon;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_block_settings);

        imgAppIcon = findViewById(R.id.imgAppIcon);
        tvAppName = findViewById(R.id.tvAppName);
        switchBlock = findViewById(R.id.switchBlock);
        timeStart = findViewById(R.id.timeStart);
        timeEnd = findViewById(R.id.timeEnd);
        btnSave = findViewById(R.id.btnSaveRules);
        tvStartTimeLabel = findViewById(R.id.tvStartTimeLabel);
        tvEndTimeLabel = findViewById(R.id.tvEndTimeLabel);

        // Get data from AppListActivity
        packageName = getIntent().getStringExtra("package");
        appName = getIntent().getStringExtra("name");

        // Get icon from package name
        try {
            appIcon = getPackageManager().getApplicationIcon(packageName);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            // Optionally set a default icon
        }

        // Fill UI
        tvAppName.setText(appName);
        imgAppIcon.setImageDrawable(appIcon);

        // Set TimePickers to 24-hour view
        timeStart.setIs24HourView(true);
        timeEnd.setIs24HourView(true);

        // Load saved rule and update UI
        loadRule();

        // Add listener to switch
        switchBlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            updateTimePickerState(isChecked);
        });

        // Handle Save Click
        btnSave.setOnClickListener(v -> saveRules());
    }

    private void loadRule() {
        SharedPreferences prefs = getSharedPreferences("block_rules", MODE_PRIVATE);
        String json = prefs.getString(packageName, null);

        if (json != null) {
            Gson gson = new Gson();
            BlockRule rule = gson.fromJson(json, BlockRule.class);

            switchBlock.setChecked(rule.blocked);
            
            if (Build.VERSION.SDK_INT >= 23) {
                timeStart.setHour(rule.startHour);
                timeStart.setMinute(rule.startMinute);
                timeEnd.setHour(rule.endHour);
                timeEnd.setMinute(rule.endMinute);
            } else {
                timeStart.setCurrentHour(rule.startHour);
                timeStart.setCurrentMinute(rule.startMinute);
                timeEnd.setCurrentHour(rule.endHour);
                timeEnd.setCurrentMinute(rule.endMinute);
            }
        }

        // Update the UI based on the loaded state
        updateTimePickerState(switchBlock.isChecked());
    }

    private void updateTimePickerState(boolean isEnabled) {
        timeStart.setEnabled(isEnabled);
        timeEnd.setEnabled(isEnabled);

        tvStartTimeLabel.setAlpha(isEnabled ? 1.0f : 0.5f);
        tvEndTimeLabel.setAlpha(isEnabled ? 1.0f : 0.5f);
        timeStart.setAlpha(isEnabled ? 1.0f : 0.5f);
        timeEnd.setAlpha(isEnabled ? 1.0f : 0.5f);
    }

    private void saveRules() {
        boolean isBlocked = switchBlock.isChecked();
        
        int startHour, startMin, endHour, endMin;

        if (Build.VERSION.SDK_INT >= 23) {
            startHour = timeStart.getHour();
            startMin = timeStart.getMinute();
            endHour = timeEnd.getHour();
            endMin = timeEnd.getMinute();
        } else {
            startHour = timeStart.getCurrentHour();
            startMin = timeStart.getCurrentMinute();
            endHour = timeEnd.getCurrentHour();
            endMin = timeEnd.getCurrentMinute();
        }

        BlockRule rule = new BlockRule(isBlocked, startHour, startMin, endHour, endMin);

        SharedPreferences prefs = getSharedPreferences("block_rules", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        // Convert object to JSON
        Gson gson = new Gson();
        String json = gson.toJson(rule);

        // Save JSON with the app's package name as key
        editor.putString(packageName, json);
        editor.apply(); // save asynchronously

        Toast.makeText(this, "Rules saved locally!", Toast.LENGTH_SHORT).show();
        finish(); // close activity
    }
}
