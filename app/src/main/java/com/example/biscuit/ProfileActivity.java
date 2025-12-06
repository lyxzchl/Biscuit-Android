package com.example.biscuit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.database.DatabaseHelper;
import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {
    
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        
        databaseHelper = new DatabaseHelper(this);

        //back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            finish();
        });
        
        // Profile Name (Full Name)
        TextView tvProfileName = findViewById(R.id.tvProfileName);
        
        // Update logic moved to onResume to refresh data after returning from EditProfileActivity
        
        // fields
        EditText emailInput = findViewById(R.id.inputEmail);
        EditText passwordInput = findViewById(R.id.inputPassword);
        
        // Edit Profile Button Logic -> Now opens Activity
        TextView btnEditProfile = findViewById(R.id.btnEditProfile);
        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        // logout button
        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Clear session data
            Session.setEmail(null);
            Session.setPasswordLength(0);
            
            SharedPreferences sharedPreferences = getSharedPreferences("BiscuitPrefs", MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.clear();
            editor.apply();
            
            // Redirect to LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // go back to MainActivity
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    return true;
                }
                return false;
            }
        });
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        refreshUserData();
    }

    private void refreshUserData() {
        String email = Session.getEmail();
        
        if (email == null) {
            SharedPreferences prefs = getSharedPreferences("BiscuitPrefs", MODE_PRIVATE);
            email = prefs.getString("KEY_EMAIL", null);
        }
        
        if (email != null) {
            TextView tvProfileName = findViewById(R.id.tvProfileName);
            String fullName = databaseHelper.getUserName(email);
            tvProfileName.setText(fullName);
            
            EditText emailInput = findViewById(R.id.inputEmail);
            emailInput.setText(email);
            
            int passLen = Session.getPasswordLength();
            if (passLen == 0) {
                 passLen = 8; 
            }
            EditText passwordInput = findViewById(R.id.inputPassword);
            StringBuilder sb = new StringBuilder();
            for(int i=0; i<passLen; i++) sb.append("*");
            passwordInput.setText(sb.toString());
        }
    }
}
