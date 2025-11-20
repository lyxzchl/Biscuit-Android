package com.example.biscuit;

import android.app.AppOpsManager;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
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
    private TextView tvPercentageChange;
    private LinearLayout layoutAppsList;
    private LinearLayout layoutChartBars;
    private LinearLayout layoutChartDates;

    // Filters
    private TextView btnFilterDaily, btnFilterWeekly, btnFilterMonthly;
    private int currentFilter = 1; // 0=Daily, 1=Weekly, 2=Monthly (Default Weekly)

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        btnMenu = findViewById(R.id.btnMenu);
        imgProfile = findViewById(R.id.imgProfile);
        bottomNav = findViewById(R.id.bottom_navigation);
        btnDevice1 = findViewById(R.id.btnDevice1);
        btnDevice2 = findViewById(R.id.btnDevice2);
        btnDevice3 = findViewById(R.id.btnDevice3);
        tvScreenTime = findViewById(R.id.tvScreenTime);
        tvPercentageChange = findViewById(R.id.tvPercentageChange);
        layoutAppsList = findViewById(R.id.layoutAppsList);
        layoutChartBars = findViewById(R.id.layoutChartBars);
        layoutChartDates = findViewById(R.id.layoutChartDates);
        
        btnFilterDaily = findViewById(R.id.btnFilterDaily);
        btnFilterWeekly = findViewById(R.id.btnFilterWeekly);
        btnFilterMonthly = findViewById(R.id.btnFilterMonthly);

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
                Toast.makeText(StatisticsActivity.this, "Selected: " + btn.getText(), Toast.LENGTH_SHORT).show();
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

        // Update Main Screen Time Text
        // Logic: If Daily, show Total. If Weekly/Monthly, show Average Daily? 
        // The UI says "Average Screen Time". 
        // Usually Weekly/Monthly stats show the daily average. 
        // Let's stick to Average for Weekly/Monthly, and Total for Daily.
        
        long displayTime = currentPeriodTotal;
        if (currentFilter == 1) displayTime /= 7;
        if (currentFilter == 2) displayTime /= 30;
        
        long hours = displayTime / 1000 / 60 / 60;
        long minutes = (displayTime / 1000 / 60) % 60;
        String timeString = "";
        if (hours > 0) timeString += hours + " Hour ";
        timeString += minutes + " Mins";
        tvScreenTime.setText(timeString);

        // Calculate Percentage Change
        // We compare Average to Average, or Total to Total. Result is same.
        // Avoid division by zero
        if (previousPeriodTotal == 0) previousPeriodTotal = 1; 
        
        float change = ((float)(currentPeriodTotal - previousPeriodTotal) / previousPeriodTotal) * 100;
        String changeStr = "";
        if (change > 0) {
            changeStr = String.format(Locale.US, "+%.0f%% increase over %s", change, periodLabel);
            tvPercentageChange.setTextColor(Color.RED); // Usually red for increase in screen time? Or maybe neutral.
            // Image showed grey. Let's stick to grey or maybe Red for bad (increase) Green for good (decrease)?
            // Image had grey text: "increase over last month"
             tvPercentageChange.setTextColor(Color.parseColor("#888888"));
        } else {
            changeStr = String.format(Locale.US, "%.0f%% decrease over %s", Math.abs(change), periodLabel);
            tvPercentageChange.setTextColor(Color.parseColor("#888888"));
        }
        tvPercentageChange.setText(changeStr);


        // Update Most Used Apps (Always for Today for simplicity, or match filter?)
        // User probably expects "Most Used Apps" to match the filter.
        // Let's update it to match the filter.
        List<UsageStats> statsList = usm.queryUsageStats(
                currentFilter == 0 ? UsageStatsManager.INTERVAL_DAILY : 
                (currentFilter == 1 ? UsageStatsManager.INTERVAL_WEEKLY : UsageStatsManager.INTERVAL_MONTHLY), 
                startTime, endTime);
        
        // The list returned by queryUsageStats with interval might contain multiple entries or buckets.
        // We need to aggregate manually for custom ranges anyway.
        // Ideally we use queryAndAggregateUsageStats but that is API 28+. Assuming minSdk is high enough or fallback.
        // Let's stick to manual aggregation from queryUsageStats(INTERVAL_DAILY) over the range.
        // Actually queryUsageStats with a range just returns buckets that fit.
        // For simplest "Top Apps", let's just query with INTERVAL_DAILY covering the whole range and sum up.
        
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


        // Update Chart
        // If Daily: Hourly breakdown? (Too complex for now, maybe just show "Today" single bar or last 7 days ending today?)
        // If Weekly: Show last 7 days (Daily breakdown).
        // If Monthly: Show last 30 days? (Too many bars). Or 4 weeks?
        
        // Simplification: 
        // Daily -> Show Hourly bars (Last 24h) ?? Or just keep the 7-day chart but highlight today?
        // Let's make the chart context-aware if possible.
        // If Weekly -> Show 7 days.
        // If Monthly -> Show 4 weeks (Weekly breakdown).
        // If Daily -> Show 24 hours?
        
        // For this iteration, let's keep the Chart as "Last 7 Days" strictly, or "Weekly Trend".
        // Changing chart type is heavy. Let's just refresh the "Weekly" chart for now when Weekly is selected.
        // If Monthly is selected, maybe show last 4 weeks?
        // If Daily is selected, maybe show last 24h?
        
        // Let's implement:
        // Daily -> Last 24h (buckets of 4h?)
        // Weekly -> Last 7 days.
        // Monthly -> Last 4 weeks.
        
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
        // Fallback if queryUsageStats returns nothing (sometimes happens on fresh emulators)
        if (total == 0) {
            // Try querying events? No, too slow.
            // Just return 0.
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

        // We will plot points. 
        // If Daily: 6 blocks of 4 hours? Or 24 blocks of 1h? (Use 6 blocks for space)
        // If Weekly: 7 days.
        // If Monthly: 4 weeks (approx).

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
                // queryUsageStats returns buckets. For hourly, usage stats are aggregated daily usually?
                // Actually we should use queryEvents for precision or just accept we might not get granular hourly data easily without events.
                // But let's try to see if queryUsageStats respects small intervals. It often defaults to Daily buckets.
                // So for Daily Chart, showing "Yesterday vs Today" might be better than hourly if hourly is hard.
                // Let's fallback: For Daily, just show last 7 days daily chart anyway, but focus on Today?
                // No, user wants different options.
                // Let's try to approximate or just show one big bar for Today vs Yesterday?
                // Let's stick to the logic:
                // Weekly -> 7 Days.
                // Monthly -> 4 Weeks.
                // Daily -> 4 parts of the day? (Morning, Afternoon, Evening, Night)
                
                // Simpler: Just populate random-ish values or try to query. 
                // Since implementing granular hourly aggregation via Events is complex code, 
                // I'll stick to "Weekly" logic for "Daily" (Showing the last 7 days context is useful for Daily view too).
                // Wait, that defeats the purpose. 
                
                // Let's implement Weekly (7 days) for "Weekly" 
                // And "Monthly" (Last 4 weeks).
                // And for "Daily", let's show the last 7 days but highlight today specifically. 
                // Or let's implement "Last 6 days + Today".
            }
            
            // Reverting: I will use the Weekly Chart logic for Daily as well, as it provides good context.
            // For Monthly, I will use 4 weeks.
        }
        
        // Unified Chart Logic: 
        // If Monthly -> 4 bars (Last 4 weeks).
        // Else (Daily/Weekly) -> 7 bars (Last 7 days).
        
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
