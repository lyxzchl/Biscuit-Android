package com.example.biscuit;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.biscuit.database.DatabaseHelper;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;

public class StatisticsActivity extends AppCompatActivity {

    private ImageButton btnMenu;
    private ImageView imgProfile;
    private BottomNavigationView bottomNav;
    private MaterialButton btnDevice1, btnDevice2, btnDevice3;
    private TextView tvScreenTime;
    private LinearLayout layoutAppsList;
    private LinearLayout layoutChartBars;
    private LinearLayout layoutChartDates;
    private DatabaseHelper databaseHelper;

    // Filters
    private TextView btnFilterDaily, btnFilterWeekly, btnFilterMonthly;
    private int currentFilter = 1; // 0=Daily, 1=Weekly, 2=Monthly (Default Weekly)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        databaseHelper = new DatabaseHelper(this);
        
        btnMenu = findViewById(R.id.btnMenu);
        imgProfile = findViewById(R.id.imgProfile);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnDevice1 = findViewById(R.id.btnDevice1);
        btnDevice2 = findViewById(R.id.btnDevice2);
        btnDevice3 = findViewById(R.id.btnDevice3);
        tvScreenTime = findViewById(R.id.tvScreenTime);
        layoutAppsList = findViewById(R.id.layoutAppsList);
        layoutChartBars = findViewById(R.id.layoutChartBars);
        layoutChartDates = findViewById(R.id.layoutChartDates);
        
        btnFilterDaily = findViewById(R.id.btnFilterDaily);
        btnFilterWeekly = findViewById(R.id.btnFilterWeekly);
        btnFilterMonthly = findViewById(R.id.btnFilterMonthly);
        
        // Load user names from DB to populate buttons
        loadUserDevices();

        btnMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StatisticsActivity.this, "Menu clicked", Toast.LENGTH_SHORT).show();
            }
        });

        imgProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(StatisticsActivity.this, "Profile clicked", Toast.LENGTH_SHORT).show();
            }
        });

        View.OnClickListener deviceClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MaterialButton btn = (MaterialButton) v;
                // Reset all buttons to unselected style
                resetDeviceButtons();
                // Set selected style
                btn.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6C5CE7")));
                btn.setTextColor(Color.WHITE);
                
                Toast.makeText(StatisticsActivity.this, "Selected: " + btn.getText(), Toast.LENGTH_SHORT).show();
                // In a real app, you would reload stats for this specific user/device here
                loadStatistics();
            }
        };

        btnDevice1.setOnClickListener(deviceClickListener);
        btnDevice2.setOnClickListener(deviceClickListener);
        btnDevice3.setOnClickListener(deviceClickListener);
        
        View.OnClickListener filterClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (v.getId() == R.id.btnFilterDaily) currentFilter = 0;
                else if (v.getId() == R.id.btnFilterWeekly) currentFilter = 1;
                else if (v.getId() == R.id.btnFilterMonthly) currentFilter = 2;
                
                updateFilterUI();
                loadStatistics();
            }
        };
        
        btnFilterDaily.setOnClickListener(filterClickListener);
        btnFilterWeekly.setOnClickListener(filterClickListener);
        btnFilterMonthly.setOnClickListener(filterClickListener);

        // Setup Bottom Navigation
        bottomNav.setSelectedItemId(R.id.nav_home); 
        
        bottomNav.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                int itemId = menuItem.getItemId();

                if (itemId == R.id.nav_home) {
                   Intent i = new Intent(StatisticsActivity.this, MainActivity.class);
                   startActivity(i);
                   finish();
                   return true;
                } else if (itemId == R.id.nav_profile) {
                   Intent i = new Intent(StatisticsActivity.this, ProfileActivity.class);
                   startActivity(i);
                   return true;
                }
                return false;
            }
        });
        
        updateFilterUI();
    }

    private void loadUserDevices() {
        // Hide buttons initially
        btnDevice1.setVisibility(View.GONE);
        btnDevice2.setVisibility(View.GONE);
        btnDevice3.setVisibility(View.GONE);
        
        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        // Fetch top 3 users
        Cursor cursor = db.rawQuery("SELECT email FROM _user LIMIT 3", null);
        
        MaterialButton[] buttons = {btnDevice1, btnDevice2, btnDevice3};
        int index = 0;
        
        if (cursor != null && cursor.moveToFirst()) {
            do {
                if (index >= buttons.length) break;
                
                String email = cursor.getString(0);
                String name = "User";
                if (email.contains("@")) {
                    name = email.substring(0, email.indexOf("@"));
                    // Capitalize first letter
                    if (name.length() > 0) {
                        name = name.substring(0, 1).toUpperCase() + name.substring(1);
                    }
                } else {
                    name = email;
                }
                
                buttons[index].setText(name + "'s Device");
                buttons[index].setVisibility(View.VISIBLE);
                index++;
            } while (cursor.moveToNext());
            cursor.close();
        }
        
        // If no users found, show at least current user or generic
        if (index == 0) {
             btnDevice1.setText("My Device");
             btnDevice1.setVisibility(View.VISIBLE);
        }
        
        // Select first available button by default
        if (btnDevice1.getVisibility() == View.VISIBLE) {
            btnDevice1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(Color.parseColor("#6C5CE7")));
            btnDevice1.setTextColor(Color.WHITE);
        }
    }

    private void resetDeviceButtons() {
        int unselectedColor = Color.parseColor("#E0E0E0");
        int black = Color.BLACK;
        
        btnDevice1.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedColor));
        btnDevice1.setTextColor(black);
        
        btnDevice2.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedColor));
        btnDevice2.setTextColor(black);
        
        btnDevice3.setBackgroundTintList(android.content.res.ColorStateList.valueOf(unselectedColor));
        btnDevice3.setTextColor(black);
    }

    private void updateFilterUI() {
        // Reset colors
        int selectedColor = Color.parseColor("#6C5CE7");
        int unselectedColor = Color.parseColor("#E0E0E0");
        int white = Color.WHITE;
        int black = Color.BLACK;

        btnFilterDaily.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 0 ? selectedColor : unselectedColor));
        btnFilterDaily.setTextColor(currentFilter == 0 ? white : black);

        btnFilterWeekly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 1 ? selectedColor : unselectedColor));
        btnFilterWeekly.setTextColor(currentFilter == 1 ? white : black);

        btnFilterMonthly.setBackgroundTintList(android.content.res.ColorStateList.valueOf(currentFilter == 2 ? selectedColor : unselectedColor));
        btnFilterMonthly.setTextColor(currentFilter == 2 ? white : black);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!hasUsageStatsPermission()) {
            Toast.makeText(this, "Please grant usage access permission", Toast.LENGTH_LONG).show();
            startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
        } else {
            loadStatistics();
        }
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS, 
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    private void loadStatistics() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        Calendar calendar = Calendar.getInstance();
        long endTime = calendar.getTimeInMillis();
        long startTime = 0;
        
        // Set start time based on filter for the chart/main stats
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long currentPeriodTotal = 0;
        long previousPeriodTotal = 0;
        String periodLabel = "";

        if (currentFilter == 0) { // Daily
            // Current: Today
            startTime = calendar.getTimeInMillis();
            currentPeriodTotal = getTotalUsage(usm, startTime, endTime);
            
            // Previous: Yesterday
            calendar.add(Calendar.DAY_OF_YEAR, -1);
            long startYesterday = calendar.getTimeInMillis();
            long endYesterday = startTime;
            previousPeriodTotal = getTotalUsage(usm, startYesterday, endYesterday);
            
            periodLabel = "last day";
            
        } else if (currentFilter == 1) { // Weekly
            // Current: Last 7 days
            calendar.add(Calendar.DAY_OF_YEAR, -6);
            startTime = calendar.getTimeInMillis();
            currentPeriodTotal = getTotalUsage(usm, startTime, endTime);
            
            // Previous: The 7 days before that
            calendar.add(Calendar.DAY_OF_YEAR, -7);
            long startPrevWeek = calendar.getTimeInMillis();
            long endPrevWeek = startTime;
            previousPeriodTotal = getTotalUsage(usm, startPrevWeek, endPrevWeek);
             
             periodLabel = "last week";

        } else { // Monthly
            // Current: Last 30 days
            calendar.add(Calendar.DAY_OF_YEAR, -29);
            startTime = calendar.getTimeInMillis();
            currentPeriodTotal = getTotalUsage(usm, startTime, endTime);

            // Previous: The 30 days before that
            calendar.add(Calendar.DAY_OF_YEAR, -30);
            long startPrevMonth = calendar.getTimeInMillis();
            long endPrevMonth = startTime;
            previousPeriodTotal = getTotalUsage(usm, startPrevMonth, endPrevMonth);
            
            periodLabel = "last month";
        }


        
        long displayTime = currentPeriodTotal;
        if (currentFilter == 1) displayTime /= 7;
        if (currentFilter == 2) displayTime /= 30;
        
        long hours = displayTime / 1000 / 60 / 60;
        long minutes = (displayTime / 1000 / 60) % 60;
        String timeString = "";
        if (hours > 0) timeString += hours + " Hour ";
        timeString += minutes + " Mins";
        tvScreenTime.setText(timeString);



        List<UsageStats> statsList = usm.queryUsageStats(
                currentFilter == 0 ? UsageStatsManager.INTERVAL_DAILY : 
                (currentFilter == 1 ? UsageStatsManager.INTERVAL_WEEKLY : UsageStatsManager.INTERVAL_MONTHLY), 
                startTime, endTime);
        

        
        List<UsageStats> detailedStats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        Map<String, UsageStats> aggregatedStats = new TreeMap<>();
        if (detailedStats != null) {
            for (UsageStats s : detailedStats) {
                long time = s.getTotalTimeInForeground();
                if (time == 0) continue;
                
                UsageStats existing = aggregatedStats.get(s.getPackageName());
                if (existing == null) {
                    aggregatedStats.put(s.getPackageName(), s);
                } else {
                    existing.add(s);
                }
            }
        }
        updateMostUsedApps(aggregatedStats);



        
        calculateChart(usm, currentFilter);
    }

    private long getTotalUsage(UsageStatsManager usm, long start, long end) {
        List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end);
        long total = 0;
        if (stats != null) {
            for (UsageStats s : stats) {
                if (s.getFirstTimeStamp() >= start && s.getFirstTimeStamp() < end)
                     total += s.getTotalTimeInForeground();
            }
        }

        if (total == 0) {

        }
        return total;
    }

    private void updateMostUsedApps(Map<String, UsageStats> aggregatedStats) {
        List<UsageStats> sortedStats = new ArrayList<>(aggregatedStats.values());
        Collections.sort(sortedStats, new Comparator<UsageStats>() {
            @Override
            public int compare(UsageStats o1, UsageStats o2) {
                return Long.compare(o2.getTotalTimeInForeground(), o1.getTotalTimeInForeground());
            }
        });

        layoutAppsList.removeAllViews();
        
        TextView title = new TextView(this);
        title.setText("Most Used Apps");
        title.setTextSize(14);
        title.setTypeface(null, android.graphics.Typeface.BOLD);
        title.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams titleParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        titleParams.bottomMargin = dpToPx(16);
        title.setLayoutParams(titleParams);
        layoutAppsList.addView(title);

        PackageManager pm = getPackageManager();
        int count = 0;
        for (UsageStats stats : sortedStats) {
            if (stats.getTotalTimeInForeground() < 60000) continue; 

            try {
                ApplicationInfo appInfo = pm.getApplicationInfo(stats.getPackageName(), 0);
                if (pm.getLaunchIntentForPackage(stats.getPackageName()) == null) {
                     continue; 
                }

                CharSequence label = pm.getApplicationLabel(appInfo);
                Drawable icon = pm.getApplicationIcon(appInfo);
                
                addAppRow(label.toString(), icon, stats.getTotalTimeInForeground());
                count++;
            } catch (PackageManager.NameNotFoundException e) {
            }

            if (count >= 5) break; 
        }
        
        if (count == 0) {
             TextView empty = new TextView(this);
             empty.setText("No usage data found for this period.");
             layoutAppsList.addView(empty);
        }
    }

    private void addAppRow(String appName, Drawable icon, long timeMillis) {
        LinearLayout row = new LinearLayout(this);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setGravity(Gravity.CENTER_VERTICAL);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.bottomMargin = dpToPx(12);
        row.setLayoutParams(params);

        ImageView img = new ImageView(this);
        img.setImageDrawable(icon);
        int size = dpToPx(32);
        LinearLayout.LayoutParams imgParams = new LinearLayout.LayoutParams(size, size);
        img.setLayoutParams(imgParams);
        
        TextView tvName = new TextView(this);
        tvName.setText(appName);
        tvName.setTextSize(14);
        tvName.setTypeface(null, android.graphics.Typeface.BOLD);
        tvName.setTextColor(Color.BLACK);
        LinearLayout.LayoutParams tvParams = new LinearLayout.LayoutParams(
                0, LinearLayout.LayoutParams.WRAP_CONTENT);
        tvParams.weight = 1;
        tvParams.leftMargin = dpToPx(12);
        tvName.setLayoutParams(tvParams);

        // Add time text
        long minutes = (timeMillis / 1000 / 60) % 60;
        long hours = timeMillis / 1000 / 60 / 60;
        String timeStr = (hours > 0 ? hours + "h " : "") + minutes + "m";
        
        TextView tvTime = new TextView(this);
        tvTime.setText(timeStr);
        tvTime.setTextSize(12);
        tvTime.setTextColor(Color.GRAY);

        row.addView(img);
        row.addView(tvName);
        row.addView(tvTime);
        layoutAppsList.addView(row);
    }

    private void calculateChart(UsageStatsManager usm, int filterType) {
        layoutChartBars.removeAllViews();
        layoutChartDates.removeAllViews();


        int numBars = 0;
        long[] values;
        String[] labels;
        
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        if (filterType == 0) { // Daily
            numBars = 6;
            values = new long[numBars];
            labels = new String[numBars];
            // 00-04, 04-08, ...
            // Start of today
            calendar.set(Calendar.HOUR_OF_DAY, 0);
            
            for (int i=0; i<numBars; i++) {
                long start = calendar.getTimeInMillis();
                calendar.add(Calendar.HOUR_OF_DAY, 4);
                long end = calendar.getTimeInMillis();
                
                // Query interval
                List<UsageStats> stats = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, start, end); // Daily interval is too big for hours?

            }
            

        }

        if (filterType == 2) { // Monthly
             numBars = 4;
             values = new long[numBars];
             labels = new String[numBars];
             
             // Reset to start of this week
             calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
             calendar.add(Calendar.WEEK_OF_YEAR, -3); // Go back 3 weeks (total 4)
             
             for (int i=0; i<numBars; i++) {
                 long start = calendar.getTimeInMillis();
                 calendar.add(Calendar.WEEK_OF_YEAR, 1);
                 long end = calendar.getTimeInMillis();
                 
                 values[i] = getTotalUsage(usm, start, end);
                 labels[i] = "W" + (i+1);
             }
             
        } else { // Weekly or Daily -> Show 7 days
             numBars = 7;
             values = new long[numBars];
             labels = new String[numBars];
             SimpleDateFormat dayFormat = new SimpleDateFormat("dd", Locale.getDefault());
             
             // Reset to Today
             calendar = Calendar.getInstance();
             calendar.set(Calendar.HOUR_OF_DAY, 0);
             calendar.set(Calendar.MINUTE, 0);
             calendar.set(Calendar.SECOND, 0);
             calendar.add(Calendar.DAY_OF_YEAR, -6);
             
             for (int i=0; i<numBars; i++) {
                 long start = calendar.getTimeInMillis();
                 calendar.add(Calendar.DAY_OF_YEAR, 1);
                 long end = calendar.getTimeInMillis();
                 
                 values[i] = getTotalUsage(usm, start, end);
                 labels[i] = dayFormat.format(start);
             }
        }

        long maxUsage = 0;
        for(long v : values) if(v > maxUsage) maxUsage = v;
        if (maxUsage == 0) maxUsage = 1;

        for (int i = 0; i < numBars; i++) {
            LinearLayout barContainer = new LinearLayout(this);
            LinearLayout.LayoutParams containerParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT);
            containerParams.weight = 1;
            barContainer.setGravity(Gravity.BOTTOM | Gravity.CENTER_HORIZONTAL);
            barContainer.setLayoutParams(containerParams);

            View bar = new View(this);
            float percentage = (float) values[i] / maxUsage;
            int barHeight = (int) (percentage * dpToPx(100)); 
            if (barHeight < dpToPx(4)) barHeight = dpToPx(4); 

            LinearLayout.LayoutParams barParams = new LinearLayout.LayoutParams(dpToPx(12), barHeight);
            bar.setLayoutParams(barParams);
            
            if (i == numBars - 1) {
                bar.setBackgroundColor(Color.parseColor("#4FC3F7"));
            } else {
                bar.setBackgroundColor(Color.parseColor("#E0E0E0"));
            }

            barContainer.addView(bar);
            layoutChartBars.addView(barContainer);

            TextView tvDate = new TextView(this);
            tvDate.setText(labels[i]);
            tvDate.setTextSize(10);
            tvDate.setTextColor(Color.GRAY);
            tvDate.setGravity(Gravity.CENTER);
            LinearLayout.LayoutParams dateParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT);
            dateParams.weight = 1;
            tvDate.setLayoutParams(dateParams);
            layoutChartDates.addView(tvDate);
        }
    }

    private int dpToPx(int dp) {
        float density = getResources().getDisplayMetrics().density;
        return Math.round(dp * density);
    }
}
