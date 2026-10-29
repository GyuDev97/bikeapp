package com.bike.trailgo.ui.riding;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.bike.trailgo.R;
import com.bike.trailgo.dto.DangerousArea;
import com.bike.trailgo.net.Server;
import com.bike.trailgo.rideassistant.RideAssistant;
import com.bike.trailgo.setting.RideSettings;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/** Keeps location delivery alive while a ride is active, including in the background. */
public class RideTrackingService extends Service implements LocationListener {
    public static final String ACTION_START = "com.bike.trailgo.action.START_RIDE_TRACKING";
    public static final String ACTION_STOP = "com.bike.trailgo.action.STOP_RIDE_TRACKING";
    public static final String ACTION_PAUSE = "com.bike.trailgo.action.PAUSE_RIDE_ASSISTANT";
    public static final String ACTION_RESUME = "com.bike.trailgo.action.RESUME_RIDE_ASSISTANT";
    public static final String ACTION_COURSE_GUIDANCE = "com.bike.trailgo.action.COURSE_GUIDANCE";
    public static final String ACTION_LOCATION_UPDATE = "com.bike.trailgo.action.RIDE_LOCATION_UPDATE";
    public static final String EXTRA_LOCATION = "ride_location";
    public static final String EXTRA_COURSE_GUIDANCE_MESSAGE = "course_guidance_message";

    private static final String CHANNEL_ID = "ride_tracking";
    private static final int NOTIFICATION_ID = 3102;

    private LocationManager locationManager;
    private FusedLocationProviderClient fusedLocationClient;
    private LocationRequest fusedLocationRequest;
    private RideAssistant rideAssistant;
    private Call<List<DangerousArea>> dangerAreaCall;
    private boolean tracking;
    private boolean usingFusedLocation;
    private final LocationCallback fusedLocationCallback = new LocationCallback() {
        @Override
        public void onLocationResult(LocationResult result) {
            if (result == null || result.getLastLocation() == null) return;
            dispatchLocation(result.getLastLocation());
        }
    };
    private final BatteryAlertPolicy batteryPolicy = new BatteryAlertPolicy();
    /** Sticky broadcast: also delivers the current level right away, so a low battery at the start is noticed. */
    private final BroadcastReceiver batteryReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (rideAssistant == null || !RideSettings.areBatteryAlertsEnabled(RideTrackingService.this)) return;
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean charging = intent.getIntExtra(BatteryManager.EXTRA_PLUGGED, 0) != 0
                    || status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;
            int percent = level < 0 || scale <= 0 ? -1 : Math.round(level * 100f / scale);
            String message = batteryPolicy.evaluate(percent, charging);
            if (message != null) rideAssistant.announceBattery(message);
        }
    };
    private SharedPreferences rideSettings;
    private final SharedPreferences.OnSharedPreferenceChangeListener settingsListener = (preferences, key) -> {
        if (RideSettings.KEY_LOCATION_INTERVAL.equals(key) || RideSettings.KEY_BATTERY_SAVER.equals(key)) {
            restartTracking();
        } else if (RideSettings.KEY_VOICE_GUIDE.equals(key) && rideAssistant != null) {
            rideAssistant.setVoiceGuidanceEnabled(RideSettings.isVoiceGuidanceEnabled(this));
        } else if (RideSettings.KEY_DANGER_ALERTS.equals(key) && rideAssistant != null) {
            boolean enabled = RideSettings.areDangerAlertsEnabled(this);
            rideAssistant.setDangerAlertsEnabled(enabled);
            if (enabled) loadDangerAreas();
        }
    };

    public static void start(Context context) {
        Intent intent = new Intent(context, RideTrackingService.class).setAction(ACTION_START);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent);
        } else {
            context.startService(intent);
        }
    }

    public static void stop(Context context) {
        context.stopService(new Intent(context, RideTrackingService.class));
    }

    public static void pauseAssistant(Context context) {
        sendCommand(context, ACTION_PAUSE);
    }

    public static void resumeAssistant(Context context) {
        sendCommand(context, ACTION_RESUME);
    }

    public static void announceCourseGuidance(Context context, String message) {
        if (message == null || message.trim().isEmpty()) return;
        Intent intent = new Intent(context, RideTrackingService.class)
                .setAction(ACTION_COURSE_GUIDANCE)
                .putExtra(EXTRA_COURSE_GUIDANCE_MESSAGE, message.trim());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent);
        else context.startService(intent);
    }

    private static void sendCommand(Context context, String action) {
        Intent intent = new Intent(context, RideTrackingService.class).setAction(action);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) context.startForegroundService(intent);
        else context.startService(intent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        RideSettings.migrate(this);
        rideSettings = RideSettings.preferences(this);
        rideSettings.registerOnSharedPreferenceChangeListener(settingsListener);
        rideAssistant = new RideAssistant(this);
        rideAssistant.setAlertListener((kind, message) -> RideAlertFeedback.alert(this, kind, message));
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        rideAssistant.setVoiceGuidanceEnabled(RideSettings.isVoiceGuidanceEnabled(this));
        rideAssistant.setDangerAlertsEnabled(RideSettings.areDangerAlertsEnabled(this));
        rideAssistant.start();
        if (RideSettings.areDangerAlertsEnabled(this)) loadDangerAreas();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent == null ? null : intent.getAction();
        if (ACTION_STOP.equals(action)) {
            stopTracking();
            stopForeground(true);
            stopSelf();
            return START_NOT_STICKY;
        }

        // A pause/resume command can also recreate a killed foreground service.  Promote it
        // before handling the command so Android's foreground-service deadline is always met.
        startForeground(NOTIFICATION_ID, buildNotification());
        if (ACTION_PAUSE.equals(action)) {
            rideAssistant.pause(System.currentTimeMillis());
            return START_STICKY;
        }
        if (ACTION_RESUME.equals(action)) {
            rideAssistant.resume(System.currentTimeMillis());
            return START_STICKY;
        }
        if (ACTION_COURSE_GUIDANCE.equals(action)) {
            if (rideAssistant != null) {
                rideAssistant.announceCourseGuidance(intent.getStringExtra(EXTRA_COURSE_GUIDANCE_MESSAGE));
            }
            return START_STICKY;
        }

        RideSessionStore.Snapshot snapshot = RideSessionStore.load(this);
        if (snapshot != null && snapshot.status == 2) {
            rideAssistant.pause(System.currentTimeMillis());
        }
        startTracking();
        // The OS may reclaim this process while the screen is off.  Restarting the foreground
        // service is safer than silently ending an unfinished ride; ACTION_STOP remains explicit.
        return START_STICKY;
    }

    private void startTracking() {
        if (tracking || !RidePermissionFlow.hasPreciseLocation(this)) {
            return;
        }

        try {
            long intervalMillis = RideSettings.locationIntervalMillis(this);
            fusedLocationRequest = LocationRequest.create()
                    .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                    .setInterval(intervalMillis)
                    .setFastestInterval(Math.min(intervalMillis, 1_000L));
            tracking = true;
            usingFusedLocation = true;
            fusedLocationClient.requestLocationUpdates(
                    fusedLocationRequest,
                    fusedLocationCallback,
                    Looper.getMainLooper()
            ).addOnFailureListener(error -> {
                // Some devices disable Google Play services or fused delivery.  Preserve the
                // previous GPS-provider path as a fallback instead of leaving the ride waiting.
                if (!tracking || !usingFusedLocation) return;
                usingFusedLocation = false;
                startGpsFallback();
            });
            fusedLocationClient.getLastLocation().addOnSuccessListener(location -> {
                if (location != null && tracking && usingFusedLocation) {
                    dispatchLocation(location);
                }
            });
        } catch (SecurityException ignored) {
            tracking = false;
            usingFusedLocation = false;
            stopSelf();
        }
    }

    private void startGpsFallback() {
        if (locationManager == null || !RidePermissionFlow.hasPreciseLocation(this)) return;
        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    RideSettings.locationIntervalMillis(this),
                    2f,
                    this
            );
            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown != null) {
                dispatchLocation(lastKnown);
            }
        } catch (SecurityException ignored) {
            tracking = false;
            stopSelf();
        }
    }

    private void stopTracking() {
        if (fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(fusedLocationCallback);
        }
        if (locationManager != null) {
            locationManager.removeUpdates(this);
        }
        tracking = false;
        usingFusedLocation = false;
    }

    private void restartTracking() {
        if (!tracking) return;
        stopTracking();
        startTracking();
    }

    @Override
    public void onLocationChanged(Location location) {
        dispatchLocation(location);
    }

    private void dispatchLocation(Location location) {
        if (rideAssistant != null) rideAssistant.onLocation(location);
        Intent update = new Intent(ACTION_LOCATION_UPDATE)
                .setPackage(getPackageName())
                .putExtra(EXTRA_LOCATION, location);
        sendBroadcast(update);
    }

    @Override
    public void onProviderDisabled(String provider) {
        // The activity surfaces GPS availability to the rider.
    }

    @Override
    public void onProviderEnabled(String provider) {
        startTracking();
    }

    @Override
    public void onDestroy() {
        stopTracking();
        try {
            unregisterReceiver(batteryReceiver);
        } catch (IllegalArgumentException ignored) {
            // not registered
        }
        if (rideSettings != null) {
            rideSettings.unregisterOnSharedPreferenceChangeListener(settingsListener);
        }
        if (dangerAreaCall != null) dangerAreaCall.cancel();
        if (rideAssistant != null) rideAssistant.shutdown();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification buildNotification() {
        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.riding_ic_pin)
                .setContentTitle("TrailGo 주행 기록 중")
                .setContentText("백그라운드에서도 위치를 기록하고 있습니다.")
                .setOngoing(true)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel = new NotificationChannel(
                CHANNEL_ID,
                "주행 기록",
                NotificationManager.IMPORTANCE_LOW
        );
        channel.setDescription("TrailGo 주행 중 위치 기록 알림");
        NotificationManager manager = getSystemService(NotificationManager.class);
        if (manager != null) {
            manager.createNotificationChannel(channel);
        }
    }

    private void loadDangerAreas() {
        if (!RideSettings.areDangerAlertsEnabled(this)) {
            return;
        }
        try {
            dangerAreaCall = Server.getInstance().getApi().getDangerArea();
            dangerAreaCall.enqueue(new Callback<List<DangerousArea>>() {
                @Override
                public void onResponse(Call<List<DangerousArea>> call, Response<List<DangerousArea>> response) {
                    if (response.isSuccessful() && response.body() != null && rideAssistant != null) {
                        rideAssistant.setDangerAreas(response.body());
                    }
                }

                @Override
                public void onFailure(Call<List<DangerousArea>> call, Throwable ignored) {
                    // Existing ride tracking remains available when danger data cannot be refreshed.
                }
            });
        } catch (RuntimeException ignored) {
            // Keep the foreground service alive even if the legacy server client is unavailable.
        }
    }
}
