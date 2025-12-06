package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.email.EmailService;

public class ResetPasswordActivity extends AppCompatActivity {

    private EditText codeInput, passwordInput, confirmPasswordInput;
    private Button resetBtn;
    private DatabaseHelper databaseHelper;
    private String email;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        databaseHelper = new DatabaseHelper(this);
        email = getIntent().getStringExtra("email");

        codeInput = findViewById(R.id.input_code);
        passwordInput = findViewById(R.id.input_new_password);
        confirmPasswordInput = findViewById(R.id.input_confirm_password);
        resetBtn = findViewById(R.id.btn_reset_password);

        resetBtn.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String code = codeInput.getText().toString().trim();
        String password = passwordInput.getText().toString().trim();
        String confirmPassword = confirmPasswordInput.getText().toString().trim();

        if (code.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.isTokenValid(code, email)) {
            // Update password
            // Note: we need a method in DatabaseHelper to update password by email without old password
            // For now, we can reuse update if we fetch old details, or create a dedicated method.
            // Let's add a dedicated method or use a trick. 
            // Since we don't have updatePassword(email, newPass), we'll add it or modify DatabaseHelper.
            // Assuming I will add updatePassword(email, newPass) to DatabaseHelper next.
            
            boolean success = databaseHelper.updatePassword(email, password);
            
            if (success) {
                Toast.makeText(this, "Password reset successful!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ResetPasswordActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Failed to update password", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(this, "Invalid reset code", Toast.LENGTH_SHORT).show();
        }
    }
}
