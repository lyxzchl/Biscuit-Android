package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;

public class SignupActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        MaterialButton registerButton = findViewById(R.id.btn_register_submit);

        registerButton.setOnClickListener(v -> {
            // TODO: Add registration logic (validate input, create user, API call, etc.)

            // For now, show a toast and navigate to the MainActivity on success
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(SignupActivity.this, MainActivity.class);
            // Clear the activity stack so the user can't go back to the signup screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }
}
