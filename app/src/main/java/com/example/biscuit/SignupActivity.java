package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.email.EmailService;
import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.button.MaterialButton;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.SecureRandom;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class SignupActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EditText emailInput;
    private EditText passwordInput;
    private EditText confirmPasswordInput;
    private EditText firstNameInput;
    private EditText lastNameInput;
    private MaterialButton registerButton;

    private EmailService emailService = new EmailService(this);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);
        
        // Initialize the DatabaseHelper
        databaseHelper = new DatabaseHelper(this);

        firstNameInput = findViewById(R.id.input_firstname);
        lastNameInput = findViewById(R.id.input_lastname);
        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        confirmPasswordInput = findViewById(R.id.input_confirm_password);

        registerButton = findViewById(R.id.btn_register_submit);

        registerButton.setOnClickListener(v -> register());
    }

    private void register(){
        String firstName = firstNameInput.getText().toString().trim();
        String lastName = lastNameInput.getText().toString().trim();
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();
        
        // Validate all fields are present
        if(firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
             Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
             return;
        }

        // Validate Passwords Match
        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        // Email Validation
        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            Toast.makeText(this, "Invalid email format. Please use: name@domain.com", Toast.LENGTH_SHORT).show();
            return;
        }

        // Password Validation Logic
        if (!isPasswordValid(password)) {
            Toast.makeText(this, "Password must be at least 8 chars, contain 1 Uppercase, 1 Digit, and 1 Special char (@, #, $, etc.)", Toast.LENGTH_LONG).show();
            return;
        }

        // Pass first and last name to register
        boolean status = databaseHelper.register(email, password, firstName, lastName);
        Log.d("activity_register", "email:" + email + " | status: " + status);

        if(status){
            // send validation email
            String validationCode = emailService.generateValidationCode();
            emailService.sendEmail(email, validationCode);
            Session.setEmail(email);
            Session.setPasswordLength(password.length());
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SignupActivity.this, ConfirmAddressActivity.class);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(SignupActivity.this, "Error: Registration failed (Email might already exist)", Toast.LENGTH_SHORT).show();
        }
    }

    private boolean isPasswordValid(String password) {
        // 1. Minimum 8 characters
        if (password.length() < 8) return false;
        
        // 2. At least one Uppercase letter
        if (!password.matches(".*[A-Z].*")) return false;
        
        // 3. At least one Digit
        if (!password.matches(".*[0-9].*")) return false;
        
        // 4. At least one Special character
        if (!password.matches(".*[*@#$%^&+=!._\\-].*")) return false;
        
        return true;
    }
}