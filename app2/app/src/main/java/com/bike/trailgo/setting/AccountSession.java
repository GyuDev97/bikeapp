package com.bike.trailgo.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.bike.trailgo.ui.member.LoginActivity;

/** Single source of truth for an account session versus the local-only guest mode. */
public final class AccountSession {
    private static final String PREFS_AUTO = "auto";
    private static final String KEY_GUEST_MODE = "guest_mode";
    private static final String KEY_LOGIN_ID = "LoginId";

    private AccountSession() {
    }

    public static boolean isGuest(Context context) {
        SharedPreferences preferences = preferences(context);
        return preferences.getBoolean(KEY_GUEST_MODE, false)
                && isBlank(preferences.getString(KEY_LOGIN_ID, ""));
    }

    public static boolean isMember(Context context) {
        SharedPreferences preferences = preferences(context);
        return !preferences.getBoolean(KEY_GUEST_MODE, false)
                && !isBlank(preferences.getString(KEY_LOGIN_ID, ""));
    }

    public static void beginGuest(Context context) {
        preferences(context).edit()
                .remove(KEY_LOGIN_ID)
                .remove("r_nickname")
                .remove("r_image")
                .remove("r_clubname")
                .putBoolean(KEY_GUEST_MODE, true)
                .apply();
    }

    public static void completeMemberLogin(Context context, String loginId) {
        preferences(context).edit()
                .putString(KEY_LOGIN_ID, loginId)
                .remove(KEY_GUEST_MODE)
                .apply();
    }

    public static void clear(Context context) {
        preferences(context).edit().remove(KEY_GUEST_MODE).apply();
    }

    public static boolean requireMember(Activity activity, String featureName) {
        if (isMember(activity)) {
            return true;
        }
        new AlertDialog.Builder(activity)
                .setTitle("로그인이 필요한 기능")
                .setMessage(featureName + "은(는) 회원 기능입니다. 로그인하면 서버 동기화와 계정 기반 기능을 사용할 수 있습니다. "
                        + "이 기기의 게스트 주행 기록은 로그인해도 자동으로 서버에 업로드되지 않습니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("로그인", (dialog, which) ->
                        activity.startActivity(new Intent(activity, LoginActivity.class)))
                .show();
        return false;
    }

    private static SharedPreferences preferences(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_AUTO, Context.MODE_PRIVATE);
    }

    private static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}
