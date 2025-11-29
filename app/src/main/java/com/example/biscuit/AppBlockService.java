package com.example.biscuit;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Telephony;
import android.telecom.TelecomManager;
import android.view.Gravity;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.core.app.NotificationCompat;

import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AppBlockService extends Service {

    private static final String CHANNEL_ID = "BlockServiceChannel";
    private WindowManager windowManager;
    private LinearLayout overlay;
    private TextView overlayText;
    private Handler handler = new Handler(Looper.getMainLooper());
    private Runnable runnable;
    private List<String> launcherPackages;

    @Override
    public void onCreate() {
        super.onCreate();
        // No operations here, wait for onStartCommand for foreground promotion
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        createNotificationChannel();
        
        Intent notificationIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this,
                0, notificationIntent, PendingIntent.FLAG_IMMUTABLE);

        Notification notification = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("Biscuit App Blocker")
                .setContentText("Monitoring app usage...")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentIntent(pendingIntent)
                .build();

        startForeground(1, notification);

        // Initialize Logic
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        launcherPackages = getLauncherPackages();

        runnable = new Runnable() {
            @Override
            public void run() {
                String foregroundApp = getForegroundApp();
                checkAndBlock(foregroundApp);
                handler.postDelayed(this, 1000); // Check every second
            }
        };

        handler.post(runnable);

        return START_STICKY;
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel serviceChannel = new NotificationChannel(
                    CHANNEL_ID,
                    "App Block Service Channel",
                    NotificationManager.IMPORTANCE_DEFAULT
            );
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(serviceChannel);
            }
        }
    }

    private List<String> getLauncherPackages() {
        List<String> packages = new ArrayList<>();
        PackageManager pm = getPackageManager();
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);

        for (ResolveInfo resolveInfo : resolveInfos) {
            packages.add(resolveInfo.activityInfo.packageName);
        }

        return packages;
    }

    private String getForegroundApp() {
        UsageStatsManager usm = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long time = System.currentTimeMillis();
        List<UsageStats> appList = usm.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, time - 1000 * 10, time);

        if (appList != null && !appList.isEmpty()) {
            SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
            for (UsageStats usageStats : appList) {
                sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
            }
            if (!sortedMap.isEmpty()) {
                return sortedMap.get(sortedMap.lastKey()).getPackageName();
            }
        }

        return "";
    }

    private void checkAndBlock(String packageName) {
        if (packageName == null || packageName.isEmpty() || launcherPackages.contains(packageName)) {
            removeOverlay();
            return;
        }

        // 1. Check Bedtime Mode First
        if (isBedtimeActive()) {
            if (!isWhitelisted(packageName)) {
                showOverlay("Bedtime Mode Active");
                return;
            } else {
                removeOverlay();
                return;
            }
        }

        // 2. Check Individual App Rules
        SharedPreferences prefs = getSharedPreferences("block_rules", MODE_PRIVATE);
        String json = prefs.getString(packageName, null);

        if (json != null) {
            Gson gson = new Gson();
            BlockRule rule = gson.fromJson(json, BlockRule.class);

            if (rule.blocked && isWithinBlockTime(rule)) {
                showOverlay("This app is blocked!");
            } else {
                removeOverlay();
            }
        } else {
            removeOverlay(); 
        }
    }

    private boolean isBedtimeActive() {
        SharedPreferences prefs = getSharedPreferences("BedtimePrefs", MODE_PRIVATE);
        boolean enabled = prefs.getBoolean("BEDTIME_ENABLED", false);
        
        if (!enabled) return false;

        // Retrieve values with the new keys, fallback to old keys or defaults
        int startHour = prefs.getInt("BEDTIME_START_HOUR", prefs.getInt("BEDTIME_HOUR", 21));
        int startMinute = prefs.getInt("BEDTIME_START_MINUTE", prefs.getInt("BEDTIME_MINUTE", 0));
        
        int endHour = prefs.getInt("BEDTIME_END_HOUR", 7);
        int endMinute = prefs.getInt("BEDTIME_END_MINUTE", 0);

        Calendar now = Calendar.getInstance();
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int startMinutes = startHour * 60 + startMinute;
        int endMinutes = endHour * 60 + endMinute;

        if (startMinutes < endMinutes) {
            // e.g., 1 PM to 3 PM
            return currentMinutes >= startMinutes && currentMinutes < endMinutes;
        } else {
            // e.g., 9 PM to 7 AM (Overnight)
            return currentMinutes >= startMinutes || currentMinutes < endMinutes;
        }
    }

    private boolean isWhitelisted(String packageName) {
        // 1. Allow Biscuit App itself
        if (packageName.equals(getPackageName())) return true;

        // 2. Allow Settings
        if (packageName.equals("com.android.settings")) return true;

        // 3. Allow Default Dialer (Phone)
        TelecomManager telecomManager = (TelecomManager) getSystemService(Context.TELECOM_SERVICE);
        if (telecomManager != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String defaultDialer = telecomManager.getDefaultDialerPackage();
            if (packageName.equals(defaultDialer)) return true;
        }
        // Fallback checks for common dialers
        if (packageName.contains("dialer") || packageName.contains("phone")) return true;

        // 4. Allow Default SMS (Messages)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            String defaultSms = Telephony.Sms.getDefaultSmsPackage(this);
            if (packageName.equals(defaultSms)) return true;
        }
        if (packageName.contains("messaging") || packageName.contains("mms")) return true;

        // 5. Allow Contacts
        if (packageName.contains("contacts")) return true;

        // 6. Allow Clock / Alarm
        if (packageName.contains("deskclock") || packageName.contains("alarm") || packageName.contains("clock")) return true;

        return false;
    }

    private boolean isWithinBlockTime(BlockRule rule) {
        Calendar now = Calendar.getInstance();
        int currentMinutes = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
        int startMinutes = rule.startHour * 60 + rule.startMinute;
        int endMinutes = rule.endHour * 60 + rule.endMinute;

        if (startMinutes <= endMinutes) {
            return currentMinutes >= startMinutes && currentMinutes <= endMinutes;
        } else {
            return currentMinutes >= startMinutes || currentMinutes <= endMinutes;
        }
    }

    private void showOverlay(String message) {
        if (overlay != null) {
            // If overlay exists, just update text if needed
            if (overlayText != null && !overlayText.getText().toString().equals(message)) {
                new Handler(Looper.getMainLooper()).post(() -> overlayText.setText(message));
            }
            return;
        }

        new Handler(Looper.getMainLooper()).post(() -> {
            if (overlay != null) return; 

            overlay = new LinearLayout(this);
            overlay.setBackgroundColor(0xCC000000); // Semi-transparent black
            overlay.setGravity(Gravity.CENTER);
            overlay.setOrientation(LinearLayout.VERTICAL);

            overlayText = new TextView(this);
            overlayText.setText(message);
            overlayText.setTextColor(0xFFFFFFFF); // White
            overlayText.setTextSize(24);
            overlayText.setGravity(Gravity.CENTER);
            overlay.addView(overlayText);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(
                    WindowManager.LayoutParams.MATCH_PARENT,
                    WindowManager.LayoutParams.MATCH_PARENT,
                    Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ?
                            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY :
                            WindowManager.LayoutParams.TYPE_PHONE,
                    WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | 
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                    PixelFormat.TRANSLUCENT
            );

            try {
                windowManager.addView(overlay, params);
            } catch (Exception e) {
                e.printStackTrace();
                overlay = null;
            }
        });
    }

    private void removeOverlay() {
        if (overlay != null) {
            new Handler(Looper.getMainLooper()).post(() -> {
                if (overlay != null && overlay.isAttachedToWindow()) {
                    try {
                        windowManager.removeView(overlay);
                    } catch (Exception e) {
                        // View might already be gone
                    }
                    overlay = null;
                    overlayText = null;
                }
            });
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        handler.removeCallbacks(runnable); 
        removeOverlay();
    }
}
