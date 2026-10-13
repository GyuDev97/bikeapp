package com.bike.trailgo.setting;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bike.trailgo.R;
import com.bike.trailgo.BuildConfig;
import com.bike.trailgo.ui.BaseFragment;
import com.bike.trailgo.ui.member.LoginActivity;
import com.bike.trailgo.ui.riding.StartActivity;

public class AppSettingsFragment extends BaseFragment {
    private static final String STATE_PAGE = "settings_page";
    private static final int GREEN = Color.rgb(46, 125, 50);
    private static final int TEXT = Color.rgb(20, 27, 23);
    private static final int MUTED = Color.rgb(111, 120, 114);
    private static final int DIVIDER = Color.rgb(232, 236, 233);

    private LinearLayout content;
    private TextView title;
    private ImageButton back;
    private View bottomNav;
    private SharedPreferences prefs;
    private String currentPage = "main";

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle state) {
        return inflater.inflate(R.layout.fragment_app_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle state) {
        super.onViewCreated(view, state);
        content = view.findViewById(R.id.settings_content);
        title = view.findViewById(R.id.settings_title);
        back = view.findViewById(R.id.settings_back);
        bottomNav = view.findViewById(R.id.settings_bottom_nav);
        RideSettings.migrate(requireContext());
        prefs = RideSettings.preferences(requireContext());
        back.setOnClickListener(v -> showPage("main"));
        bindBottomNavigation(view);
        showPage(state == null ? "main" : state.getString(STATE_PAGE, "main"));
    }

    private void showPage(String page) {
        currentPage = page;
        content.removeAllViews();
        boolean main = "main".equals(page);
        back.setVisibility(main ? View.GONE : View.VISIBLE);
        bottomNav.setVisibility(main ? View.VISIBLE : View.GONE);

        switch (page) {
            case "general": title.setText("일반"); buildGeneral(); break;
            case "notifications": title.setText("알림"); buildNotifications(); break;
            case "display": title.setText("화면 표시"); buildDisplay(); break;
            case "riding": title.setText("주행 설정"); buildRiding(); break;
            case "privacy": title.setText("개인정보"); buildPrivacy(); break;
            case "services": title.setText("연동 서비스"); buildServices(); break;
            case "version": title.setText("버전 정보"); buildVersion(); break;
            default: title.setText("설정"); buildMain(); break;
        }
    }

    private void buildMain() {
        addMainRow("일반", "1.0 기본 동작", R.drawable.ic_setting_home, () -> showPage("general"));
        addMainRow("알림", "위험지역 알림", R.drawable.profile_ic_bell, () -> showPage("notifications"));
        addMainRow("화면 표시", "화면 꺼짐·밝기 유지", R.drawable.profile_ic_theme, () -> showPage("display"));
        addMainRow("주행 설정", "기록 주기, 배터리, 음성", R.drawable.profile_ic_bicycle, () -> showPage("riding"));
        addMainRow("개인정보", "기본 공개 범위", R.drawable.profile_ic_lock, () -> showPage("privacy"));
        addMainRow("연동 서비스", "GPX, 외부 서비스", R.drawable.profile_ic_data, () -> showPage("services"));
        addMainRow("버전 정보", "앱 버전, 정책, 라이선스", R.drawable.profile_ic_help, () -> showPage("version"));
        if (!AccountSession.isGuest(requireContext())) {
            TextView logout = simpleAction("로그아웃", Color.rgb(229, 57, 53));
            logout.setOnClickListener(v -> confirmLogout());
        }
    }

    private void buildGeneral() {
        addUnavailableRow("언어와 단위", "한국어 · km 단위로 고정되어 있습니다.");
        addUnavailableRow("시작 화면", "현재는 홈 화면으로 고정되어 있습니다.");
        addUnavailableRow("자동 저장", "저장 전 기록 요약 화면에서 사용자가 확인합니다.");
        addUnavailableRow("백그라운드 주행", "주행 중에는 위치 기록 서비스가 항상 유지됩니다.");
    }

    private void buildNotifications() {
        addSwitchRow("위험지역 접근 음성 안내", "위험지역에 접근하면 주행 중 음성으로 안내합니다.", RideSettings.KEY_DANGER_ALERTS, true);
        addUnavailableRow("코스 이탈 알림", "코스 이탈 판정 기능이 준비되면 제공됩니다.");
        addUnavailableRow("배터리 부족 알림", "1.0에서는 시스템 배터리 알림을 사용합니다.");
        addUnavailableRow("알림 시간대·진동", "1.0에서는 기기 기본 설정을 따릅니다.");
    }

    private void buildDisplay() {
        addSwitchRow("주행 중 화면 자동 꺼짐 방지", "주행 화면이 보이는 동안 화면을 켜 둡니다.", RideSettings.KEY_KEEP_SCREEN, true);
        addSwitchRow("밝기 유지", "주행 화면이 보이는 동안 밝기를 최대로 유지합니다.", RideSettings.KEY_KEEP_BRIGHTNESS, true);
        addUnavailableRow("테마·지도 표시·속도계 크기", "1.0 화면은 기본 표시로 고정되어 있습니다.");
    }

    private void buildRiding() {
        addChoiceRow("위치 기록 주기", "다음 위치 요청부터 반영됩니다.", RideSettings.KEY_LOCATION_INTERVAL, "1초", new String[]{"1초", "3초", "5초"});
        addSwitchRow("배터리 절약 모드", "켜면 위치 기록 주기를 5초로 사용합니다.", RideSettings.KEY_BATTERY_SAVER, false);
        addSwitchRow("음성 안내", "위험지역 및 Ride Assistant 안내 음성을 켜거나 끕니다.", RideSettings.KEY_VOICE_GUIDE, true);
        addUnavailableRow("자동 일시정지", "주행 시간과 기록 정확도를 보장하기 위해 1.0에서는 수동 일시정지만 지원합니다.");
        addUnavailableRow("GPS 기록 정밀도", "위치 기록 주기 설정으로 통합되었습니다.");
        addUnavailableRow("이동 감지·화면 회전·고도 보정", "1.0에서는 기기 기본값과 GPS 고도값을 사용합니다.");
        if (BuildConfig.DEBUG) {
            TextView simulator = simpleAction("Ride Assistant 테스트  ›\n시뮬레이션 주행으로 AI 안내 흐름을 점검합니다.", TEXT);
            simulator.setOnClickListener(v -> {
                Intent intent = new Intent();
                intent.setClassName(requireContext(), "com.bike.trailgo.debug.RideAssistantDebugActivity");
                startActivity(intent);
            });
        }
    }

    private void buildPrivacy() {
        if (AccountSession.isGuest(requireContext())) {
            addUnavailableRow("기록 저장 위치", "게스트 기록과 GPX는 이 기기에만 비공개로 저장됩니다.");
        } else {
            addChoiceRow("기본 기록 공개 범위", "저장 화면의 초기 공개 범위에 반영됩니다.", RideSettings.KEY_RECORD_VISIBILITY, "전체 공개", new String[]{"전체 공개", "비공개"});
        }
        addValueRow("개인정보·위치정보 안내", "›", this::openPrivacyCenter);
        if (!AccountSession.isGuest(requireContext())) {
            addValueRow("계정 삭제", "삭제 범위 및 처리 상태  ›", this::openPrivacyCenter);
        }
        addSecurityNotice();
    }

    private void buildServices() {
        addUnavailableRow("GPX 가져오기", "코스 탭의 GPX 가져오기에서 사용할 수 있습니다.");
        addUnavailableRow("GPX 내보내기", "기록 상세 화면의 GPX 내보내기에서 사용할 수 있습니다.");
        addUnavailableRow("외부 서비스 연동", "Strava·Garmin·Komoot 연동은 게스트 MVP에서 제공하지 않습니다.");
    }

    private void buildVersion() {
        LinearLayout brand = new LinearLayout(requireContext());
        brand.setGravity(Gravity.CENTER_VERTICAL);
        brand.setPadding(dp(18), dp(18), dp(18), dp(18));
        ImageView logo = new ImageView(requireContext());
        logo.setImageResource(R.drawable.m_logo);
        brand.addView(logo, new LinearLayout.LayoutParams(dp(48), dp(48)));
        TextView name = text("TrailGo\n버전 " + appVersion(), 18, TEXT);
        name.setTypeface(null, android.graphics.Typeface.BOLD);
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
        np.setMargins(dp(14), 0, 0, 0);
        brand.addView(name, np);
        content.addView(brand);
        addDivider();
        addValueRow("업데이트 확인", "최신 버전입니다", this::comingSoon);
        addValueRow("개인정보처리방침", "›", this::openPrivacyCenter);
        addValueRow("이용약관", "›", this::comingSoon);
        addValueRow("오픈소스 라이선스", "›", this::comingSoon);
        TextView copyright = text("© 2026 TrailGo. All rights reserved.", 11, MUTED);
        copyright.setGravity(Gravity.CENTER);
        copyright.setPadding(0, dp(42), 0, dp(24));
        content.addView(copyright);
    }

    private void addMainRow(String heading, String subtitle, int iconRes, Runnable action) {
        LinearLayout row = baseRow(dp(72));
        ImageView icon = new ImageView(requireContext());
        icon.setImageResource(iconRes);
        icon.setColorFilter(GREEN);
        row.addView(icon, new LinearLayout.LayoutParams(dp(42), dp(26)));
        addLabels(row, heading, subtitle);
        TextView arrow = text("›", 25, MUTED);
        row.addView(arrow, new LinearLayout.LayoutParams(dp(24), ViewGroup.LayoutParams.WRAP_CONTENT));
        row.setOnClickListener(v -> action.run());
        content.addView(row);
        addDivider();
    }

    private void addServiceRow(String heading, String subtitle, int icon, Runnable action) {
        addMainRow(heading, subtitle, icon, action);
    }

    private void addChoiceRow(String heading, String subtitle, String key, String fallback, String[] choices) {
        LinearLayout row = baseRow(subtitle == null ? dp(58) : dp(68));
        addLabels(row, heading, subtitle);
        TextView value = text(prefs.getString(key, fallback) + "  ›", 13, TEXT);
        value.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        row.addView(value, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT));
        row.setOnClickListener(v -> showChoices(heading, key, choices, fallback, value));
        content.addView(row);
        addDivider();
    }

    private void addValueRow(String heading, String valueText, Runnable action) {
        LinearLayout row = baseRow(dp(58));
        addLabels(row, heading, null);
        TextView value = text(valueText, 12, MUTED);
        row.addView(value);
        row.setOnClickListener(v -> action.run());
        content.addView(row);
        addDivider();
    }

    /** A visible status row avoids presenting an unimplemented feature as a working switch. */
    private void addUnavailableRow(String heading, String detail) {
        LinearLayout row = baseRow(dp(68));
        row.setClickable(false);
        row.setFocusable(false);
        addLabels(row, heading, detail);
        TextView status = text("준비 중", 12, MUTED);
        status.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        row.addView(status, new LinearLayout.LayoutParams(dp(58), ViewGroup.LayoutParams.MATCH_PARENT));
        content.addView(row);
        addDivider();
    }

    private void addSwitchRow(String heading, String subtitle, String key, boolean fallback) {
        LinearLayout row = baseRow(dp(68));
        addLabels(row, heading, subtitle);
        Switch toggle = new Switch(requireContext());
        toggle.setChecked(prefs.getBoolean(key, fallback));
        int[][] states = {{android.R.attr.state_checked}, {}};
        toggle.setThumbTintList(new ColorStateList(states, new int[]{Color.WHITE, Color.WHITE}));
        toggle.setTrackTintList(new ColorStateList(states, new int[]{GREEN, Color.rgb(207, 214, 210)}));
        toggle.setOnCheckedChangeListener((button, checked) -> prefs.edit().putBoolean(key, checked).apply());
        row.addView(toggle, new LinearLayout.LayoutParams(dp(52), dp(40)));
        row.setOnClickListener(v -> toggle.setChecked(!toggle.isChecked()));
        content.addView(row);
        addDivider();
    }

    private LinearLayout baseRow(int minHeight) {
        LinearLayout row = new LinearLayout(requireContext());
        row.setMinimumHeight(minHeight);
        row.setGravity(Gravity.CENTER_VERTICAL);
        row.setOrientation(LinearLayout.HORIZONTAL);
        row.setPadding(dp(16), dp(8), dp(12), dp(8));
        row.setBackgroundColor(Color.WHITE);
        row.setClickable(true);
        row.setFocusable(true);
        return row;
    }

    private void addLabels(LinearLayout row, String heading, String subtitle) {
        LinearLayout labels = new LinearLayout(requireContext());
        labels.setOrientation(LinearLayout.VERTICAL);
        labels.setGravity(Gravity.CENTER_VERTICAL);
        TextView primary = text(heading, 14, TEXT);
        primary.setTypeface(null, android.graphics.Typeface.BOLD);
        labels.addView(primary);
        if (subtitle != null && !subtitle.isEmpty()) {
            TextView secondary = text(subtitle, 11, MUTED);
            secondary.setPadding(0, dp(3), 0, 0);
            labels.addView(secondary);
        }
        row.addView(labels, new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
    }

    private void addSegments(String key, String[] options, String fallback) {
        LinearLayout group = new LinearLayout(requireContext());
        group.setPadding(dp(14), 0, dp(14), dp(12));
        group.setOrientation(LinearLayout.HORIZONTAL);
        for (String option : options) {
            Button button = new Button(requireContext());
            button.setAllCaps(false);
            button.setText(option);
            button.setTextSize(12);
            LinearLayout.LayoutParams bp = new LinearLayout.LayoutParams(0, dp(42), 1);
            bp.setMargins(dp(3), 0, dp(3), 0);
            group.addView(button, bp);
            button.setOnClickListener(v -> {
                prefs.edit().putString(key, option).apply();
                refreshSegments(group, key, fallback);
            });
        }
        content.addView(group);
        refreshSegments(group, key, fallback);
        addDivider();
    }

    private void refreshSegments(LinearLayout group, String key, String fallback) {
        String selected = prefs.getString(key, fallback);
        for (int i = 0; i < group.getChildCount(); i++) {
            Button button = (Button) group.getChildAt(i);
            boolean active = button.getText().toString().equals(selected);
            button.setBackgroundResource(active ? R.drawable.settings_segment_selected : R.drawable.settings_segment_normal);
            button.setTextColor(active ? Color.WHITE : TEXT);
        }
    }

    private void addSectionLabel(String label) {
        TextView view = text(label, 12, TEXT);
        view.setTypeface(null, android.graphics.Typeface.BOLD);
        view.setPadding(dp(16), dp(16), dp(16), dp(10));
        content.addView(view);
    }

    private void addPreview() {
        addSectionLabel("미리보기");
        LinearLayout preview = new LinearLayout(requireContext());
        preview.setBackgroundResource(R.drawable.settings_preview_bg);
        preview.setGravity(Gravity.CENTER);
        preview.setPadding(dp(12), dp(12), dp(12), dp(12));
        TextView speed = text("속도 (km/h)\n23.5", 18, Color.WHITE);
        TextView distance = text("거리 (km)\n12.8", 18, Color.WHITE);
        speed.setGravity(Gravity.CENTER);
        distance.setGravity(Gravity.CENTER);
        preview.addView(speed, new LinearLayout.LayoutParams(0, dp(70), 1));
        preview.addView(distance, new LinearLayout.LayoutParams(0, dp(70), 1));
        LinearLayout.LayoutParams pp = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        pp.setMargins(dp(14), 0, dp(14), dp(18));
        content.addView(preview, pp);
    }

    private void addSecurityNotice() {
        TextView notice = text("주행 기록에는 위치 경로와 고도값이 포함될 수 있습니다. 수집·보관·삭제 기준은 개인정보 안내에서 확인하세요.", 12, MUTED);
        notice.setGravity(Gravity.CENTER_VERTICAL);
        notice.setBackgroundColor(Color.rgb(245, 247, 246));
        notice.setPadding(dp(16), dp(20), dp(16), dp(20));
        LinearLayout.LayoutParams np = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        np.setMargins(dp(14), dp(22), dp(14), dp(14));
        content.addView(notice, np);
    }

    private TextView simpleAction(String label, int color) {
        TextView action = text(label, 14, color);
        action.setGravity(Gravity.CENTER_VERTICAL);
        action.setPadding(dp(16), dp(12), dp(16), dp(12));
        action.setMinHeight(dp(58));
        content.addView(action, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
        addDivider();
        return action;
    }

    private void addDivider() {
        View divider = new View(requireContext());
        divider.setBackgroundColor(DIVIDER);
        content.addView(divider, new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
    }

    private TextView text(String value, int sp, int color) {
        TextView view = new TextView(requireContext());
        view.setText(value);
        view.setTextSize(sp);
        view.setTextColor(color);
        view.setLineSpacing(0, 1.08f);
        return view;
    }

    private void showChoices(String heading, String key, String[] options, String fallback, TextView target) {
        String selected = prefs.getString(key, fallback);
        int checked = 0;
        for (int i = 0; i < options.length; i++) if (options[i].equals(selected)) checked = i;
        new AlertDialog.Builder(requireContext())
                .setTitle(heading)
                .setSingleChoiceItems(options, checked, (dialog, which) -> {
                    prefs.edit().putString(key, options[which]).apply();
                    target.setText(options[which] + "  ›");
                    dialog.dismiss();
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void bindBottomNavigation(View root) {
        TextView settings = root.findViewById(R.id.bottom_nav_profile);
        settings.setText("설정");
        settings.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.drawer_ic_settings_custom, 0, 0);
        settings.setTextColor(GREEN);
        settings.setTypeface(null, android.graphics.Typeface.BOLD);
        root.findViewById(R.id.bottom_nav_home).setOnClickListener(v -> navigateTo(R.id.nav_home));
        root.findViewById(R.id.bottom_nav_records).setOnClickListener(v -> navigateTo(R.id.nav_records));
        root.findViewById(R.id.bottom_nav_course).setOnClickListener(v -> navigateTo(R.id.nav_course_view));
        root.findViewById(R.id.bottom_nav_ride).setOnClickListener(v -> startActivity(new Intent(requireContext(), StartActivity.class)));
    }

    private void navigateTo(int destination) {
        if (controller.getCurrentDestination() == null || controller.getCurrentDestination().getId() != destination) {
            controller.navigate(destination);
        }
    }

    private void confirmLogout() {
        if (UserSessionManager.hasUnfinishedRide(requireContext())) {
            new AlertDialog.Builder(requireContext())
                    .setTitle("주행 기록 중")
                    .setMessage("진행 중인 주행을 먼저 종료하고 저장한 뒤 로그아웃하세요. 기록 유실을 막기 위해 지금은 로그아웃할 수 없습니다.")
                    .setPositiveButton("확인", null)
                    .show();
            return;
        }
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("이 기기의 로그인·프로필·SOS 연락처와 임시 주행 데이터를 지웁니다. 서버의 계정과 저장된 기록은 유지됩니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    UserSessionManager.clearLocalUserData(requireContext());
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }).show();
    }

    private void openPrivacyCenter() {
        startActivity(new Intent(requireContext(), PrivacyCenterActivity.class));
    }

    private void openGpx() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("application/gpx+xml");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivity(intent);
    }

    private void createGpx() {
        Intent intent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
        intent.setType("application/gpx+xml");
        intent.putExtra(Intent.EXTRA_TITLE, "trailgo-record.gpx");
        startActivity(intent);
    }

    private void comingSoon() {
        Toast.makeText(requireContext(), "준비 중입니다.", Toast.LENGTH_SHORT).show();
    }

    private String appVersion() {
        try {
            return requireContext().getPackageManager().getPackageInfo(requireContext().getPackageName(), 0).versionName;
        } catch (Exception ignored) {
            return "1.0";
        }
    }

    private int dp(int value) {
        return Math.round(value * getResources().getDisplayMetrics().density);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putString(STATE_PAGE, currentPage);
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onResume() {
        super.onResume();
        View appBar = requireActivity().findViewById(R.id.app_bar_layout);
        if (appBar != null) appBar.setVisibility(View.GONE);
    }
}
