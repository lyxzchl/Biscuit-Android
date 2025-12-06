package com.example.biscuit;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class EditProfileActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private EditText emailInput;
    private EditText oldPasswordInput;
    private EditText newPasswordInput;
    private EditText confirmNewPasswordInput;
    private Button saveButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        databaseHelper = new DatabaseHelper(this);
        
        firstNameInput = findViewById(R.id.input_firstname);
        lastNameInput = findViewById(R.id.input_lastname);
        emailInput = findViewById(R.id.input_email);
        oldPasswordInput = findViewById(R.id.input_old_password);
        newPasswordInput = findViewById(R.id.input_new_password);
        confirmNewPasswordInput = findViewById(R.id.input_confirm_new_password);
        saveButton = findViewById(R.id.btn_save_changes);
        ImageButton btnBack = findViewById(R.id.btnBack);

        // Load current user data
        String currentEmail = Session.getEmail();
        if (currentEmail == null) {
             SharedPreferences prefs = getSharedPreferences("BiscuitPrefs", MODE_PRIVATE);
             currentEmail = prefs.getString("KEY_EMAIL", null);
        }

        if (currentEmail != null) {
            emailInput.setText(currentEmail);
            firstNameInput.setText(databaseHelper.getFirstName(currentEmail));
            lastNameInput.setText(databaseHelper.getLastName(currentEmail));
        }
        
        // Disable email editing if you want email to be immutable or handle it carefully (often email is ID)
        // Here we allow editing but note it changes login ID.
        
        final String finalCurrentEmail = currentEmail;

        saveButton.setOnClickListener(v -> {
            String newFirstName = firstNameInput.getText().toString().trim();
            String newLastName = lastNameInput.getText().toString().trim();
            String newEmail = emailInput.getText().toString().trim();
            String oldPassword = oldPasswordInput.getText().toString().trim();
            String newPassword = newPasswordInput.getText().toString().trim();
            String confirmNewPassword = confirmNewPasswordInput.getText().toString().trim();

            // Validation
            if (newFirstName.isEmpty() || newLastName.isEmpty() || newEmail.isEmpty() || oldPassword.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields (Name, Email, Old Password)", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verify Old Password
            if (!databaseHelper.login(finalCurrentEmail, oldPassword)) {
                Toast.makeText(this, "Incorrect Old Password", Toast.LENGTH_SHORT).show();
                return;
            }

            // Handle New Password Logic
            String passwordToSave = oldPassword;
            if (!newPassword.isEmpty()) {
                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(this, "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (newPassword.length() < 8) {
                     Toast.makeText(this, "New password too short", Toast.LENGTH_SHORT).show();
                     return;
                }
                passwordToSave = newPassword;
            }

            // Update Database
            boolean success = databaseHelper.update(finalCurrentEmail, newEmail, passwordToSave, newFirstName, newLastName);

            if (success) {
                Toast.makeText(this, "Profile Updated Successfully!", Toast.LENGTH_SHORT).show();
                
                // Update Session and Prefs
                Session.setEmail(newEmail);
                if (!newPassword.isEmpty()) {
                    Session.setPasswordLength(newPassword.length());
                }
                
                SharedPreferences prefs = getSharedPreferences("BiscuitPrefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("KEY_EMAIL", newEmail);
                editor.apply();
                
                finish(); // Close activity and return to Profile
            } else {
                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
            }
        });

        btnBack.setOnClickListener(v -> finish());
    }
}
