package com.example.biscuit;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.InputType;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class BedtimeActivity extends AppCompatActivity {

    private NumberPicker npStartHours, npStartMinutes;
    private NumberPicker npEndHours, npEndMinutes;
    private RadioGroup radioGroup;
    private RadioButton radioOn, radioOff;
    private ImageButton btnMenu;
    private ImageView imgProfile;
    private BottomNavigationView bottomNav;
    private Button btnConfirm;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bedtime);
        
        databaseHelper = new DatabaseHelper(this);

        // === LINK UI ELEMENTS ===
        npStartHours = findViewById(R.id.npStartHours);
        npStartMinutes = findViewById(R.id.npStartMinutes);
        npEndHours = findViewById(R.id.npEndHours);
        npEndMinutes = findViewById(R.id.npEndMinutes);

        radioGroup = findViewById(R.id.radioGroupBedtime);
        radioOn = findViewById(R.id.radioOn);
        radioOff = findViewById(R.id.radioOff);
        
        btnMenu = findViewById(R.id.btnMenu);
        imgProfile = findViewById(R.id.imgProfile);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnConfirm = findViewById(R.id.btnConfirm);

        // === SETUP HOURS PICKER ===
        npStartHours.setMinValue(0);
        npStartHours.setMaxValue(23);
        npStartHours.setFormatter(value -> String.format("%02d", value));
        
        npEndHours.setMinValue(0);
        npEndHours.setMaxValue(23);
        npEndHours.setFormatter(value -> String.format("%02d", value));
        
        // === SETUP MINUTES PICKER ===
        npStartMinutes.setMinValue(0);
        npStartMinutes.setMaxValue(59);
        npStartMinutes.setFormatter(value -> String.format("%02d", value));
        
        npEndMinutes.setMinValue(0);
        npEndMinutes.setMaxValue(59);
        npEndMinutes.setFormatter(value -> String.format("%02d", value));

        // === LOAD SAVED STATE ===
        loadBedtimeSettings();

        // === RADIO BUTTON LOGIC ===
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            boolean enabled = (checkedId == R.id.radioOn);
            updatePickersState(enabled);
        });
        
        // === MENU & PROFILE ===
        btnMenu.setOnClickListener(view -> Toast.makeText(BedtimeActivity.this, "Menu clicked", Toast.LENGTH_SHORT).show());
        imgProfile.setOnClickListener(view -> Toast.makeText(BedtimeActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show());
        
        // === CONFIRM BUTTON ===
        btnConfirm.setOnClickListener(v -> {
            if (radioOn.isChecked()) {
                // Enabling Bedtime
                saveBedtimeSettings(true);
                Toast.makeText(BedtimeActivity.this, "Bedtime mode enabled", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                // Disabling Bedtime - Require Password
                SharedPreferences prefs = getSharedPreferences("BedtimePrefs", MODE_PRIVATE);
                boolean currentlyEnabled = prefs.getBoolean("BEDTIME_ENABLED", false);
                
                if (currentlyEnabled) {
                    showUnlockDialog();
                } else {
                    // It was already disabled, just save/close
                    saveBedtimeSettings(false);
                    finish();
                }
            }
        });

        // === BOTTOM NAV ===
        bottomNav.setSelectedItemId(R.id.nav_home); 
        
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.nav_home) {
                   Intent i = new Intent(BedtimeActivity.this, MainActivity.class);
                   startActivity(i);
                   finish();
                   return true;
                } else if (itemId == R.id.nav_profile) {
                   Intent i = new Intent(BedtimeActivity.this, ProfileActivity.class);
                   startActivity(i);
                   return true;
                }
                return false;
            }
        });
    }

    private void updatePickersState(boolean enabled) {
        npStartHours.setEnabled(enabled);
        npStartMinutes.setEnabled(enabled);
        npEndHours.setEnabled(enabled);
        npEndMinutes.setEnabled(enabled);
    }

    private void loadBedtimeSettings() {
        SharedPreferences prefs = getSharedPreferences("BedtimePrefs", MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("BEDTIME_ENABLED", false);
        
        int startHour = prefs.getInt("BEDTIME_HOUR", 21); // Legacy key
        int startMinute = prefs.getInt("BEDTIME_MINUTE", 0); // Legacy key
        
        // If new keys exist use them, else fallback
        if (prefs.contains("BEDTIME_START_HOUR")) {
            startHour = prefs.getInt("BEDTIME_START_HOUR", 21);
            startMinute = prefs.getInt("BEDTIME_START_MINUTE", 0);
        }
        
        int endHour = prefs.getInt("BEDTIME_END_HOUR", 7);
        int endMinute = prefs.getInt("BEDTIME_END_MINUTE", 0);

        npStartHours.setValue(startHour);
        npStartMinutes.setValue(startMinute);
        npEndHours.setValue(endHour);
        npEndMinutes.setValue(endMinute);

        if (enabled) {
            radioOn.setChecked(true);
            updatePickersState(true);
        } else {
            radioOff.setChecked(true);
            updatePickersState(false);
        }
    }

    private void saveBedtimeSettings(boolean enabled) {
        SharedPreferences prefs = getSharedPreferences("BedtimePrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("BEDTIME_ENABLED", enabled);
        
        // Use new keys
        editor.putInt("BEDTIME_START_HOUR", npStartHours.getValue());
        editor.putInt("BEDTIME_START_MINUTE", npStartMinutes.getValue());
        editor.putInt("BEDTIME_END_HOUR", npEndHours.getValue());
        editor.putInt("BEDTIME_END_MINUTE", npEndMinutes.getValue());
        
        // Maintain backward compatibility for AppBlockService if it reads old keys (though we should update it)
        editor.putInt("BEDTIME_HOUR", npStartHours.getValue());
        editor.putInt("BEDTIME_MINUTE", npStartMinutes.getValue());
        
        editor.apply();
    }

    private void showUnlockDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Unlock (Parent)");
        builder.setMessage("Enter login password to disable Bedtime mode:");

        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
        builder.setView(input);

        builder.setPositiveButton("Unlock", (dialog, which) -> {
            String password = input.getText().toString();
            String email = Session.getEmail(); // Get current user email
            
            if (email == null) {
                // Fallback if session lost, try to get from Prefs
                SharedPreferences loginPrefs = getSharedPreferences("BiscuitPrefs", MODE_PRIVATE);
                email = loginPrefs.getString("KEY_EMAIL", "");
            }

            if (databaseHelper.login(email, password)) {
                saveBedtimeSettings(false);
                Toast.makeText(BedtimeActivity.this, "Bedtime mode disabled", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(BedtimeActivity.this, "Incorrect password!", Toast.LENGTH_SHORT).show();
                // Re-select "On" since unlock failed
                radioOn.setChecked(true);
            }
        });

        builder.setNegativeButton("Cancel", (dialog, which) -> {
            dialog.cancel();
            // Re-select "On" since cancelled
            radioOn.setChecked(true);
        });

        builder.show();
    }
}
