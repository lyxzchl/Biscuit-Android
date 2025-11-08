package com.example.biscuit;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import com.example.biscuit.R;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private MaterialCardView cardStatistics;
    private MaterialCardView cardApplications;
    private MaterialCardView cardBedtime;
    private ImageButton btnMenu;
    private ImageView imgProfile;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        cardStatistics = findViewById(R.id.cardStatistics);
        cardApplications = findViewById(R.id.cardApplications);
        cardBedtime = findViewById(R.id.cardBedtime);
        btnMenu = findViewById(R.id.btnMenu);
        imgProfile = findViewById(R.id.imgProfile);
        bottomNav = findViewById(R.id.bottom_navigation);

        cardStatistics.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Open Statistics", Toast.LENGTH_SHORT).show();
                // Intent i = new Intent(MainActivity.this, StatisticsActivity.class);
                // startActivity(i);
            }
        });

        cardApplications.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Open Applications", Toast.LENGTH_SHORT).show();
                // Intent i = new Intent(MainActivity.this, ApplicationsActivity.class);
                // startActivity(i);
            }
        });

        cardBedtime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(MainActivity.this, "Open Bedtime mode", Toast.LENGTH_SHORT).show();
                // Intent i = new Intent(MainActivity.this, BedtimeActivity.class);
                // startActivity(i);
            }
        });

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Menu clicked", Toast.LENGTH_SHORT).show();
                // open navigation drawer or menu
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(MainActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
                // open profile or settings screen
            }
        });

        // Bottom navigation listener
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId(); // Get the ID once

                if (itemId == R.id.nav_home) {
                    // Already home
                    return true;
                } else if (itemId == R.id.nav_profile) {
                    Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                    // Intent i = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(i);
                    return true;
                }
                return false;
            }
        });// Bottom navigation listener
    }
}