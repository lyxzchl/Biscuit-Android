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

public class ConfirmAddressActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_address);

        // --- Handle the "resend" link ---
        handleResendTextView();

        MaterialButton verifyButton = findViewById(R.id.btn_verify);
        verifyButton.setOnClickListener(v -> {
            // TODO: Add OTP validation logic

            // For now, navigate to MainActivity on success
            Toast.makeText(this, "Verification Successful!", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(ConfirmAddressActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        });
    }

    private void handleResendTextView() {
        TextView tvResend = findViewById(R.id.tv_resend);
        String fullText = getString(R.string.resend_prompt);
        SpannableString spannableString = new SpannableString(fullText);

        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(@NonNull View widget) {
                // TODO: Add logic to resend the verification code
                Toast.makeText(ConfirmAddressActivity.this, "Resending code...", Toast.LENGTH_SHORT).show();
            }
        };

        String targetWord = "resend";
        int startIndex = fullText.indexOf(targetWord);
        int endIndex = startIndex + targetWord.length();

        int resendColor = ContextCompat.getColor(this, R.color.splash_register_link); // Reusing color

        spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(resendColor), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        tvResend.setText(spannableString);
        tvResend.setMovementMethod(LinkMovementMethod.getInstance());
    }
}
