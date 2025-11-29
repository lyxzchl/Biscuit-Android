package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.biscuit.sessionStorage.Session;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

public class ProfileActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //back button
        ImageButton btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> {
            // finishes the activity and returns to the previous one
            finish();
        });

        //fields
        EditText email = findViewById(R.id.inputEmail);
        EditText password = findViewById(R.id.inputPassword);

        email.setText(Session.getEmail());
        password.setText("*".repeat(Session.getPasswordLength()));

        // logout button
        TextView btnLogout = findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> {
            // Clear session data
            Session.setEmail(null);
            Session.setPasswordLength(0);
            
            // Redirect to LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            // Clear the back stack so user can't go back to profile
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // bottom navigation
        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        // set the "profile" item as selected
        bottomNav.setSelectedItemId(R.id.nav_profile);

        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.nav_home) {
                    // go back to MainActivity
                    Intent intent = new Intent(ProfileActivity.this, MainActivity.class);
                    // add flags to clear the activity stack and avoid creating a new instance of MainActivity
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    // we are already on the profile screen
                    return true;
                }
                return false;
            }
        });
    }
}
