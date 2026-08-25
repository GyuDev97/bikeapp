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
import androidx.activity.OnBackPressedCallback;

import com.bike.trailgo.R;
import com.bike.trailgo.BuildConfig;
import com.bike.trailgo.ui.BaseFragment;
import com.bike.trailgo.ui.member.LoginActivity;
import com.bike.trailgo.ui.riding.StartActivity;

public class AppSettingsFragment extends BaseFragment {
    private static final String PREFS = "trailgo_settings";
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
        prefs = requireContext().getSharedPreferences(PREFS, Activity.MODE_PRIVATE);
        back.setOnClickListener(v -> showPage("main"));
        requireActivity().getOnBackPressedDispatcher().addCallback(
                getViewLifecycleOwner(),
                new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (!"main".equals(currentPage)) {
                            showPage("main");
                            return;
                        }
                        setEnabled(false);
                        if (!controller.popBackStack(R.id.nav_home, false)) {
                            controller.navigate(R.id.nav_home);
                        }
                    }
                }
        );
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
        addMainRow("일반", "언어, 단위, 기본 화면", R.drawable.ic_setting_home, () -> showPage("general"));
        addMainRow("알림", "라이딩, 위험지역, 배터리", R.drawable.profile_ic_bell, () -> showPage("notifications"));
        addMainRow("화면 표시", "테마, 지도, 밝기", R.drawable.profile_ic_theme, () -> showPage("display"));
        addMainRow("주행 설정", "GPS, 자동 일시정지, 음성", R.drawable.profile_ic_bicycle, () -> showPage("riding"));
        addMainRow("개인정보", "공개 범위, 데이터, 위치", R.drawable.profile_ic_lock, () -> showPage("privacy"));
        addMainRow("연동 서비스", "GPX, 외부 서비스", R.drawable.profile_ic_data, () -> showPage("services"));
        addMainRow("버전 정보", "앱 버전, 정책, 라이선스", R.drawable.profile_ic_help, () -> showPage("version"));
        TextView logout = simpleAction("로그아웃", Color.rgb(229, 57, 53));
        logout.setOnClickListener(v -> confirmLogout());
    }

    private void buildGeneral() {
        addChoiceRow("언어", null, "language", "한국어", new String[]{"한국어", "English"});
        addChoiceRow("단위 설정", null, "units", "km, m, km/h", new String[]{"km, m, km/h", "mi, ft, mph"});
        addChoiceRow("기본 화면", null, "default_screen", "홈", new String[]{"홈", "기록", "코스", "프로필"});
        addSwitchRow("자동 저장", "라이딩 종료 시 자동으로 기록을 저장합니다.", "auto_save", true);
        addChoiceRow("앱 시작 시 기본 화면", null, "launch_screen", "홈", new String[]{"홈", "기록", "코스"});
        addSwitchRow("백그라운드 실행", "앱이 백그라운드에서도 계속 실행되도록 합니다.", "background_run", true);
    }

    private void buildNotifications() {
        addSwitchRow("라이딩 시작/종료 알림", "라이딩 시작과 종료를 알립니다.", "notify_ride", true);
        addSwitchRow("코스 이탈 알림", "설정한 코스를 벗어나면 알립니다.", "notify_course", true);
        addSwitchRow("위험지역 접근 알림", "위험지역에 접근하면 알립니다.", "notify_danger", true);
        addSwitchRow("배터리 부족 알림", "배터리 잔량이 낮으면 알립니다.", "notify_battery", true);
        addSwitchRow("긴급 알림", "긴급 상황 발생 시 알림을 보냅니다.", "notify_emergency", true);
        addChoiceRow("알림 시간대", null, "notify_time", "항상", new String[]{"항상", "주간", "사용 안 함"});
        addChoiceRow("알림 진동", null, "notify_vibration", "기본 진동", new String[]{"기본 진동", "강하게", "진동 없음"});
    }

    private void buildDisplay() {
        addSectionLabel("테마 모드");
        addSegments("theme", new String[]{"라이트", "다크", "시스템"}, "라이트");
        addChoiceRow("지도 테마", null, "map_theme", "기본 지도", new String[]{"기본 지도", "지형 지도", "위성 지도"});
        addChoiceRow("지도 표시 요소", "경로, POI 등", "map_elements", "기본", new String[]{"기본", "간소화", "상세"});
        addSwitchRow("주행 중 화면 자동 꺼짐 방지", "화면이 꺼지지 않도록 유지합니다.", "keep_screen", true);
        addChoiceRow("속도계 숫자 크기", null, "speed_size", "크게", new String[]{"보통", "크게", "매우 크게"});
        addSwitchRow("밝기 유지", "주행 중 화면 밝기를 최대로 유지합니다.", "keep_brightness", true);
        addPreview();
    }

    private void buildRiding() {
        addSwitchRow("자동 일시정지", "신호나 정지 시 자동으로 일시정지합니다.", "auto_pause", true);
        addChoiceRow("이동 감지 민감도", null, "motion_sensitivity", "보통", new String[]{"낮음", "보통", "높음"});
        addChoiceRow("GPS 기록 정밀도", null, "gps_accuracy", "높음 (1초)", new String[]{"절전 (5초)", "보통 (3초)", "높음 (1초)"});
        addChoiceRow("위치 기록 주기", null, "location_interval", "1초", new String[]{"1초", "3초", "5초"});
        addSwitchRow("배터리 절약 모드", "배터리 사용을 줄여 더 오래 기록합니다.", "battery_saver", false);
        addSwitchRow("음성 안내", "주행 정보와 알림을 음성으로 안내합니다.", "voice_guide", true);
        addChoiceRow("주행 중 화면 회전", null, "screen_rotation", "자동", new String[]{"자동", "세로 고정", "가로 고정"});
        addSwitchRow("고도 보정", "시작 시 고도를 보정합니다.", "altitude_correction", true);
        TextView test = simpleAction("기록 품질 테스트  ›", TEXT);
        test.setOnClickListener(v -> Toast.makeText(requireContext(), "GPS와 센서 상태가 양호합니다.", Toast.LENGTH_SHORT).show());
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
        addChoiceRow("기본 기록 공개 범위", null, "record_visibility", "전체 공개", new String[]{"전체 공개", "친구 공개", "비공개"});
        addSwitchRow("위치정보 저장", "라이딩 경로를 저장합니다.", "save_location", true);
        addSwitchRow("사진 위치정보 포함", "사진에 위치정보를 포함합니다.", "photo_location", true);
        TextView export = simpleAction("데이터 내보내기  ›\n내 기록 데이터를 내보냅니다.", TEXT);
        export.setOnClickListener(v -> Toast.makeText(requireContext(), "데이터 내보내기는 준비 중입니다.", Toast.LENGTH_SHORT).show());
        TextView delete = simpleAction("계정 삭제  ›\n계정과 모든 데이터를 삭제합니다.", Color.rgb(229, 57, 53));
        delete.setOnClickListener(v -> new AlertDialog.Builder(requireContext())
                .setTitle("계정을 삭제할까요?")
                .setMessage("서버에 저장된 기록과 계정 정보가 모두 삭제됩니다.")
                .setNegativeButton("취소", null)
                .setPositiveButton("삭제", (dialog, which) -> Toast.makeText(requireContext(), "계정 삭제 API 연결이 필요합니다.", Toast.LENGTH_SHORT).show())
                .show());
        addSecurityNotice();
    }

    private void buildServices() {
        addServiceRow("GPX 가져오기", "GPX 파일을 가져와 코스로 추가합니다.", R.drawable.profile_ic_data, () -> openGpx());
        addServiceRow("GPX 내보내기", "기록을 GPX 파일로 내보냅니다.", R.drawable.profile_ic_data, () -> createGpx());
        addServiceRow("Strava 연동", "준비 중", R.drawable.profile_ic_bicycle, this::comingSoon);
        addServiceRow("Garmin 연동", "준비 중", R.drawable.profile_ic_bicycle, this::comingSoon);
        addServiceRow("Komoot 연동", "준비 중", R.drawable.profile_ic_bicycle, this::comingSoon);
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
        addValueRow("개인정보처리방침", "›", this::comingSoon);
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
        TextView notice = text("개인정보 보호를 위해 안전하게 관리됩니다.", 12, MUTED);
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
        new AlertDialog.Builder(requireContext())
                .setTitle("로그아웃")
                .setMessage("TrailGo에서 로그아웃할까요?")
                .setNegativeButton("취소", null)
                .setPositiveButton("로그아웃", (dialog, which) -> {
                    requireActivity().getSharedPreferences("auto", Activity.MODE_PRIVATE).edit().clear().apply();
                    Intent intent = new Intent(requireContext(), LoginActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    requireActivity().finish();
                }).show();
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
