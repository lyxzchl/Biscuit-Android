package com.example.biscuit;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MenuItem;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

public class ApplicationActivity extends AppCompatActivity {
    private AppListAdapter adapter;
    private BottomNavigationView bottomNav;
    private ImageButton btnMenu;
    private ImageView imgProfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_applist);
        
        // Check for permissions on start
        checkPermissions();

        RecyclerView recycler = findViewById(R.id.recyclerInstalledApps);
        recycler.setLayoutManager(new LinearLayoutManager(this));

        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN, null);
        intent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> apps = pm.queryIntentActivities(intent, 0);

        adapter = new AppListAdapter(this, apps, pm);
        recycler.setAdapter(adapter);

        SearchView searchView = findViewById(R.id.searchView);
        // Ensure search view is expanded or easily accessible
        searchView.setIconifiedByDefault(false);
        searchView.setQueryHint("Search apps...");

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.getFilter().filter(newText);
                return false;
            }
        });
        
        // Setup Bottom Navigation
        bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setSelectedItemId(R.id.nav_home); 
        
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.nav_home) {
                   Intent i = new Intent(ApplicationActivity.this, MainActivity.class);
                   startActivity(i);
                   finish();
                   return true;
                } else if (itemId == R.id.nav_profile) {
                   Intent i = new Intent(ApplicationActivity.this, ProfileActivity.class);
                   startActivity(i);
                   return true;
                }
                return false;
            }
        });

        // Setup Header Buttons
        btnMenu = findViewById(R.id.btnMenu);
        imgProfile = findViewById(R.id.imgProfile);

        if (btnMenu != null) {
            btnMenu.setOnClickListener(view -> Toast.makeText(ApplicationActivity.this, "Menu clicked", Toast.LENGTH_SHORT).show());
        }
        if (imgProfile != null) {
             imgProfile.setOnClickListener(view -> Toast.makeText(ApplicationActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show());
        }

        // Start the blocking service if permissions are granted
        startBlockService();
    }
    
    private void startBlockService() {
        Intent serviceIntent = new Intent(this, AppBlockService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(serviceIntent);
        } else {
            startService(serviceIntent);
        }
    }

    private void checkPermissions() {
        // Check Usage Stats Permission
        if (!hasUsageStatsPermission()) {
            new AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Biscuit needs usage access to detect which app is running. Please grant this permission.")
                .setPositiveButton("Grant", (dialog, which) -> {
                    startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                })
                .setNegativeButton("Cancel", null)
                .show();
        }

        // Check Overlay Permission (Only needed for API 23+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(this)) {
                new AlertDialog.Builder(this)
                    .setTitle("Permission Required")
                    .setMessage("Biscuit needs to draw over other apps to block them. Please grant this permission.")
                    .setPositiveButton("Grant", (dialog, which) -> {
                        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                                Uri.parse("package:" + getPackageName()));
                        startActivity(intent);
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            }
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }
    
    @Override
    protected void onResume() {
        super.onResume();
        // Restart service if stopped or just to ensure it's running
        startBlockService();
    }
}
