package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // --- Initialize Buttons and Set Listeners ---
        MaterialButton loginButton = findViewById(R.id.btn_login);
        MaterialButton scheduleButton = findViewById(R.id.btn_schedule);

        loginButton.setOnClickListener(v -> {
            // TODO: Replace with your actual LoginActivity
            Intent intent = new Intent(SplashActivity.this, LoginActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Logged-In", Toast.LENGTH_SHORT).show();
        });

        scheduleButton.setOnClickListener(v -> {
            // TODO: Replace with your actual ScheduleActivity or navigate to MainActivity
            Intent intent = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(intent);
            Toast.makeText(this, "Schedule Clicked", Toast.LENGTH_SHORT).show();
        });


        // --- Style and Handle the "Register" TextView ---
        handleRegisterTextView();
    }

    private void handleRegisterTextView() {
        TextView tvRegister = findViewById(R.id.tv_register);
        String fullText = getString(R.string.splash_register_prompt); // Using a string resource is best practice

        SpannableString spannableString = new SpannableString(fullText);

        // Define the clickable part of the string
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // TODO: Replace with your actual SignupActivity
                Intent intent = new Intent(SplashActivity.this, SignupActivity.class);
                startActivity(intent);
                Toast.makeText(SplashActivity.this, "Register link clicked!", Toast.LENGTH_SHORT).show();
            }
        };

        // Find the start and end of the word "register"
        String registerText = "register";
        int startIndex = fullText.indexOf(registerText);
        int endIndex = startIndex + registerText.length();

        // Get the color from your colors.xml
        int registerColor = ContextCompat.getColor(this, R.color.splash_register_link);

        // Apply the clickable span and the color span
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(registerColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // Set the text and make the link clickable
        tvRegister.setText(spannableString);
        tvRegister.setMovementMethod(android.text.method.LinkMovementMethod.getInstance()); // This is crucial for making links clickable
    }
}
