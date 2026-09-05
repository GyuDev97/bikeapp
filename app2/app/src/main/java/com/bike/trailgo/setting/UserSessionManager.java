package com.bike.trailgo.setting;

import android.content.Context;

import com.bike.trailgo.ui.riding.RideLocalData;

/** Keeps local logout cleanup consistent across the two settings entry points. */
public final class UserSessionManager {
    private UserSessionManager() {
    }

    public static boolean hasUnfinishedRide(Context context) {
        return RideLocalData.hasUnfinishedRide(context);
    }

    public static void clearLocalUserData(Context context) {
        Context appContext = context.getApplicationContext();
        // "auto" contains the remembered account/profile fields and SOS contact.
        appContext.getSharedPreferences("auto", Context.MODE_PRIVATE).edit().clear().apply();
        // These profile customizations are associated with the signed-in rider, not the device.
        appContext.getSharedPreferences("profile_features", Context.MODE_PRIVATE).edit().clear().apply();
        RideLocalData.clear(appContext);
    }
}
