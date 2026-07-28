package com.example.testapplication;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;

import com.kakao.vectormap.KakaoMapSdk;

public class MtbCraftApplication extends Application {
    private static final String TAG = "MtbCraftApplication";
    private static final String KAKAO_APP_KEY_META_DATA = "com.kakao.sdk.AppKey";

    @Override
    public void onCreate() {
        super.onCreate();
        String appKey = getKakaoNativeAppKey();
        if (appKey == null || appKey.trim().isEmpty()) {
            Log.e(TAG, "Kakao native app key is missing.");
            return;
        }
        Log.d(TAG, "Using Kakao native app key " + maskAppKey(appKey));
        KakaoMapSdk.init(this, "0619e3c07c193137428169313e5f38c1");
    }

    private String maskAppKey(String appKey) {
        if (appKey.length() <= 8) {
            return "****";
        }
        return appKey.substring(0, 4) + "..." + appKey.substring(appKey.length() - 4);
    }

    private String getKakaoNativeAppKey() {
        try {
            ApplicationInfo appInfo = getPackageManager().getApplicationInfo(
                    getPackageName(),
                    PackageManager.GET_META_DATA
            );
            Bundle metaData = appInfo.metaData;
            if (metaData == null) {
                return null;
            }
            return metaData.getString(KAKAO_APP_KEY_META_DATA);
        } catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "Unable to read Kakao native app key.", e);
            return null;
        }
    }
}
