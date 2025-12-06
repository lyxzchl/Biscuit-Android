package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.email.EmailService;
import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.button.MaterialButton;

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

        MaterialButton verifyButton = findViewById(R.id.btn_verify);
        verifyButton.setOnClickListener(v -> {
            // Retrieve the code when the button is clicked
            String token = code_input_1.getText().toString() + 
                           code_input_2.getText().toString() + 
                           code_input_3.getText().toString() + 
                           code_input_4.getText().toString() + 
                           code_input_5.getText().toString();
            
            String email = Session.getEmail();
            if (email == null || email.isEmpty()) {
                 Toast.makeText(this, "Session expired, please login again", Toast.LENGTH_SHORT).show();
                 return;
            }

            boolean isTokenValid = databaseHelper.isTokenValid(token, email);
            if (isTokenValid) {
                databaseHelper.validateEmail(email);
                Toast.makeText(this, "Email verified successfully!", Toast.LENGTH_SHORT).show();
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
            String email = Session.getEmail();
            if(email != null) {
                emailService.sendEmail(email, code);
                Toast.makeText(this, "Validation code resent!", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
