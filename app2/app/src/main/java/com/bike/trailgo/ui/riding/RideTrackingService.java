package com.bike.trailgo.ui.riding;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Build;
import android.os.IBinder;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;

import com.bike.trailgo.R;

/** Keeps location delivery alive while a ride is active, including in the background. */
public class RideTrackingService extends Service implements LocationListener {
    public static final String ACTION_START = "com.bike.trailgo.action.START_RIDE_TRACKING";
    public static final String ACTION_STOP = "com.bike.trailgo.action.STOP_RIDE_TRACKING";
    public static final String ACTION_LOCATION_UPDATE = "com.bike.trailgo.action.RIDE_LOCATION_UPDATE";
    public static final String EXTRA_LOCATION = "ride_location";

    private static final String CHANNEL_ID = "ride_tracking";
    private static final int NOTIFICATION_ID = 3102;

    private LocationManager locationManager;
    private boolean tracking;

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

    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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

        startForeground(NOTIFICATION_ID, buildNotification());
        startTracking();
        return START_NOT_STICKY;
    }

    private void startTracking() {
        if (tracking || locationManager == null
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        try {
            locationManager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    1000L,
                    2f,
                    this
            );
            tracking = true;

            Location lastKnown = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (lastKnown != null) {
                dispatchLocation(lastKnown);
            }
        } catch (SecurityException ignored) {
            stopSelf();
        }
    }

    private void stopTracking() {
        if (locationManager != null && tracking) {
            locationManager.removeUpdates(this);
        }
        tracking = false;
    }

    @Override
    public void onLocationChanged(Location location) {
        dispatchLocation(location);
    }

    private void dispatchLocation(Location location) {
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
}
