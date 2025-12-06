package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.ForegroundColorSpan;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.email.EmailService;
import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.button.MaterialButton;

import java.security.SecureRandom;

public class ConfirmAddressActivity extends AppCompatActivity {
    private final EmailService emailService = new EmailService(this);
    private final DatabaseHelper databaseHelper = new DatabaseHelper(this);
    EditText code_input_1;
    EditText code_input_2;
    EditText code_input_3;
    EditText code_input_4;
    EditText code_input_5;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_confirm_address);

        code_input_1 = findViewById(R.id.otp_box_1);
        code_input_2 = findViewById(R.id.otp_box_2);
        code_input_3 = findViewById(R.id.otp_box_3);
        code_input_4 = findViewById(R.id.otp_box_4);
        code_input_5 = findViewById(R.id.otp_box_5);
        String token = code_input_1.toString() + code_input_2.toString() + code_input_3.toString() + code_input_4.toString() + code_input_5.toString();

        MaterialButton verifyButton = findViewById(R.id.btn_verify);
        verifyButton.setOnClickListener(v -> {
            String email = Session.getEmail();
            boolean isTokenValid = databaseHelper.isTokenValid(token, email);
            if (isTokenValid) {
                databaseHelper.validateEmail(email);
            Intent intent = new Intent(ConfirmAddressActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            } else {
                Toast.makeText(this, "Invalid code!", Toast.LENGTH_SHORT).show();
            }
        });

        TextView tvResend = findViewById(R.id.tv_resend);
        tvResend.setOnClickListener(v -> {
            String code = emailService.generateValidationCode();
            emailService.sendEmail(code, Session.getEmail());
            Toast.makeText(this, "Validation code resent!", Toast.LENGTH_SHORT).show();
        });
    }
}