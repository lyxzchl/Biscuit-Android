package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.button.MaterialButton;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // --- Handle the "restore" link ---
        handleRestoreTextView();

        // --- Set up Login Button Click ---
        MaterialButton loginSubmitButton = findViewById(R.id.btn_login_submit);
        loginSubmitButton.setOnClickListener(v -> {
            // TODO: Add login logic (validate input, API call, etc.)

            // For now, navigate to MainActivity on successful login
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            // Clear the activity stack so the user can't go back to the login screen
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void handleRestoreTextView() {
        TextView tvForgotPassword = findViewById(R.id.tv_forgot_password);
        String fullText = getString(R.string.forgot_password_prompt);

        SpannableString spannableString = new SpannableString(fullText);

        // The word we want to make clickable and colored
        String targetWord = "restore";
        int startIndex = fullText.indexOf(targetWord);
        int endIndex = startIndex + targetWord.length();

        // 1. Create a ClickableSpan
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // TODO: Navigate to a "Restore Password" activity or show a dialog
                Toast.makeText(LoginActivity.this, "Restore password clicked!", Toast.LENGTH_SHORT).show();
            }
        };

        // 2. Get the color for the link
        int restoreColor = ContextCompat.getColor(this, R.color.splash_register_link); // Reusing the same color

        // 3. Apply the spans
        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(restoreColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        // 4. Set the text and make it clickable
        tvForgotPassword.setText(spannableString);
        tvForgotPassword.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
    