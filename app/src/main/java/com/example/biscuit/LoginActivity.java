package com.example.biscuit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.email.EmailService;
import com.example.biscuit.sessionStorage.Session;

public class LoginActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;
    private EmailService emailService;
    private EditText emailInput;
    private EditText passwordInput;
    private Button loginButton;
    private TextView forgotPasswordText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        
        databaseHelper = new DatabaseHelper(this);
        emailService = new EmailService(this);

        emailInput = findViewById(R.id.input_email);
        passwordInput = findViewById(R.id.input_password);
        loginButton = findViewById(R.id.btn_login_submit);
        forgotPasswordText = findViewById(R.id.tv_forgot_password); 

        loginButton.setOnClickListener(v -> login());
        
        if (forgotPasswordText != null) {
            forgotPasswordText.setOnClickListener(v -> sendResetEmail());
        }
    }

    private void sendResetEmail() {
        String email = emailInput.getText().toString().trim();

        if (email.isEmpty()) {
            Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show();
            return;
        }
        
        // Check if user exists first (optional, but good UX)
        // We can assume if email format is valid, we send code.
        // Or checking database:
        // if (!databaseHelper.checkEmailExists(email)) ... 
        
        // Generate and Send Code
        String code = emailService.generateValidationCode();
        emailService.sendEmail(email, code);

        // Navigate to Reset Password Screen
        Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
        i.putExtra("email", email);
        startActivity(i);

        Toast.makeText(this, "A reset code has been sent to " + email, Toast.LENGTH_LONG).show();
    }

    private void login(){
        String email = emailInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter email and password", Toast.LENGTH_SHORT).show();
            return;
        }
        
        boolean auth = databaseHelper.login(email, password);

        if(auth){
            // save user session
            SharedPreferences sharedPreferences = getSharedPreferences("BiscuitPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putString("KEY_EMAIL", email);
            editor.putBoolean("KEY_IS_LOGGED_IN", true);
            editor.apply();
            
            // Update global session object as well
            Session.setEmail(email);
            Session.setPasswordLength(password.length());

            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish(); 
        } else {
            Toast.makeText(LoginActivity.this, "Error: Invalid credentials", Toast.LENGTH_SHORT).show();
        }
    }
}
