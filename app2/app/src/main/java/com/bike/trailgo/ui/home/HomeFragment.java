package com.bike.trailgo.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.Navigation;

import com.bike.trailgo.R;
import com.bike.trailgo.dto.RidingRecord;
import com.bike.trailgo.ui.BaseFragment;
import com.bike.trailgo.ui.riding.StartActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment {
    private Call<List<RidingRecord>> myInfo;
    private View ridingStart;
    private TextView mainTime;
    private TextView mainKm;
    private TextView rider;
    private TextView mainDistance;
    private TextView goalPercent;
    private TextView recentDate;
    private TextView recentDistance;
    private TextView recentHeight;
    private TextView recentDuration;
    private TextView recentCourse;
    private TextView monthCount;
    private TextView totalHeight;
    private TextView totalTime;
    private ProgressBar goalProgress;

    public static HomeFragment newInstance() {
        return new HomeFragment();
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home_redesign, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ridingStart = view.findViewById(R.id.bottom_nav_ride);
        mainTime = view.findViewById(R.id.main_time);
        mainKm = view.findViewById(R.id.main_km);
        rider = view.findViewById(R.id.idinfo);
        mainDistance = view.findViewById(R.id.main_dis);
        goalPercent = view.findViewById(R.id.home_goal_percent);
        goalProgress = view.findViewById(R.id.home_goal_progress);
        recentDate = view.findViewById(R.id.home_recent_date);
        recentDistance = view.findViewById(R.id.home_recent_distance);
        recentHeight = view.findViewById(R.id.home_recent_height);
        recentDuration = view.findViewById(R.id.home_recent_duration);
        recentCourse = view.findViewById(R.id.home_recent_course);
        monthCount = view.findViewById(R.id.home_month_count);
        totalHeight = view.findViewById(R.id.home_total_height);
        totalTime = view.findViewById(R.id.home_total_time);

        String riderId = getRiderId();
        bindKoreanLabels(view);
        bindNavigation(view);
        bindHeader(view);

        ridingStart.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), StartActivity.class);
            startActivity(intent);
        });

        loadRidingSummary(riderId);
    }

    private String getRiderId() {
        String riderId = model.r_Id.getValue();
        if (riderId != null && riderId.length() > 0) {
            return riderId;
        }

        SharedPreferences auto = requireContext().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        return auto.getString("LoginId", "admin");
    }

    private void bindKoreanLabels(View view) {
        setText(view, R.id.home_recent_title, "최근 기록");
        setText(view, R.id.home_recent_more, "전체 보기");
    }

    private void setText(View root, int textViewId, String text) {
        TextView textView = root.findViewById(textViewId);
        if (textView != null) {
            textView.setText(text);
        }
    }

    private void bindNavigation(View view) {
        setNavigationClick(view, R.id.recent_record_card, R.id.action_nav_home_to_nav_records);
        setNavigationClick(view, R.id.bottom_nav_records, R.id.action_nav_home_to_nav_records);
        setNavigationClick(view, R.id.bottom_nav_course, R.id.action_nav_home_to_nav_course_view);
        setNavigationClick(view, R.id.bottom_nav_profile, R.id.action_nav_home_to_setting);
        TextView home = view.findViewById(R.id.bottom_nav_home);
        if (home != null) {
            home.setTextColor(android.graphics.Color.parseColor("#1F7A2E"));
            home.setTypeface(null, android.graphics.Typeface.BOLD);
        }
    }

    private void setNavigationClick(View root, int viewId, int actionId) {
        View item = root.findViewById(viewId);
        if (item != null) {
            item.setOnClickListener(v -> navigate(v, actionId));
        }
    }

    private void bindHeader(View view) {
        View menuButton = view.findViewById(R.id.home_menu_button);
        if (menuButton != null) {
            SpannableString brand = new SpannableString("TrailGO");
            brand.setSpan(
                    new ForegroundColorSpan(android.graphics.Color.parseColor("#1F7A2E")),
                    5,
                    brand.length(),
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            );
            ((TextView) menuButton).setText(brand);
            menuButton.setOnClickListener(v -> {
                DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(androidx.core.view.GravityCompat.START);
                }
            });
        }
    }

    private void navigate(View view, int actionId) {
        Navigation.findNavController(view).navigate(actionId);
    }

    private void loadRidingSummary(String riderId) {
        if (myInfo != null) {
            myInfo.cancel();
        }
        myInfo = serverApi.getMyRecord(riderId);
        myInfo.enqueue(new Callback<List<RidingRecord>>() {
            @Override
            public void onResponse(Call<List<RidingRecord>> call, Response<List<RidingRecord>> response) {
                if (response.code() == 200) {
                    updateSummary(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<RidingRecord>> call, Throwable t) {
                if (!call.isCanceled()) {
                    t.printStackTrace();
                }
            }
        });
    }

    private void updateSummary(List<RidingRecord> records) {
        if (records == null) {
            return;
        }

        ArrayList<RidingRecord> sorted = new ArrayList<>(records);
        Collections.sort(sorted, new Comparator<RidingRecord>() {
            @Override
            public int compare(RidingRecord left, RidingRecord right) {
                String leftDate = left.getRr_date() == null ? "" : left.getRr_date();
                String rightDate = right.getRr_date() == null ? "" : right.getRr_date();
                return rightDate.compareTo(leftDate);
            }
        });

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
        int todayDistance = 0;
        int todaySeconds = 0;
        int totalDistance = 0;
        int totalHeightMeters = 0;
        int totalSeconds = 0;
        int ridesThisMonth = 0;
        String currentMonth = new SimpleDateFormat("yyyy-MM", Locale.KOREA).format(new Date());

        for (RidingRecord record : sorted) {
            int distance = record.getRr_distance();
            totalDistance += distance;
            totalHeightMeters += record.getRr_high();
            totalSeconds += record.getRr_time();

            String date = record.getRr_date();
            if (date != null && date.startsWith(currentMonth)) {
                ridesThisMonth++;
            }
            if (date != null && date.length() >= 10 && today.equals(date.substring(0, 10))) {
                todayDistance += distance;
                todaySeconds += record.getRr_time();
            }
        }

        setTextIfPresent(mainKm, formatDistance(todayDistance));
        setTextIfPresent(mainTime, formatDuration(todaySeconds));
        setTextIfPresent(mainDistance, formatDistance(totalDistance));
        setTextIfPresent(monthCount, ridesThisMonth + " 회");
        setTextIfPresent(totalHeight, String.format(Locale.KOREA, "%,d m", totalHeightMeters));
        setTextIfPresent(totalTime, formatClockDuration(totalSeconds));

        int distanceGoalMeters = 10000;
        int goalPercentValue = Math.min(100, (int) Math.round((todayDistance / (double) distanceGoalMeters) * 100.0));
        if (goalProgress != null) {
            goalProgress.setProgress(goalPercentValue);
        }
        setTextIfPresent(goalPercent, goalPercentValue + "%");

        if (!sorted.isEmpty()) {
            bindRecentRecord(sorted.get(0));
        }
    }

    private void bindRecentRecord(RidingRecord recentRecord) {
        setTextIfPresent(recentDate, formatRecentDate(recentRecord.getRr_date()));
        setTextIfPresent(recentDistance, formatDistance(recentRecord.getRr_distance()));
        setTextIfPresent(recentHeight, recentRecord.getRr_high() + " m");
        setTextIfPresent(recentDuration, "~ " + formatDuration(recentRecord.getRr_time()));
        String courseName = recentRecord.getRr_name();
        if (courseName == null || courseName.trim().isEmpty()) {
            courseName = recentRecord.getRr_area();
        }
        setTextIfPresent(recentCourse, courseName == null || courseName.trim().isEmpty()
                ? "대구 앞산 라이딩" : courseName);
    }

    private void setTextIfPresent(TextView textView, String text) {
        if (textView != null) {
            textView.setText(text);
        }
    }

    private String formatDistance(int meters) {
        if (meters >= 1000) {
            return String.format(Locale.KOREA, "%.1f km", meters / 1000.0);
        }
        return meters + " m";
    }

    private String formatDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;

        if (hours > 0) {
            return hours + "\uC2DC\uAC04 " + minutes + "\uBD84";
        }
        if (minutes > 0) {
            return minutes + "\uBD84";
        }
        return seconds + "\uCD08";
    }

    private String formatClockDuration(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remainingSeconds = seconds % 60;
        return String.format(Locale.KOREA, "%02d:%02d:%02d", hours, minutes, remainingSeconds);
    }

    private String formatRecentDate(String rawDate) {
        if (rawDate == null || rawDate.length() < 16) {
            return rawDate == null ? "-" : rawDate;
        }

        String datePart = rawDate.substring(0, 10);
        String timePart = rawDate.substring(11, 16);

        try {
            Date parsedDate = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.KOREA).parse(datePart + " " + timePart);
            if (parsedDate != null) {
                return new SimpleDateFormat("yyyy년 M월 d일 · a h:mm", Locale.KOREA).format(parsedDate);
            }
        } catch (Exception ignored) {
        }

        return rawDate;
    }

    @Override
    public void onResume() {
        super.onResume();
        setAppBarVisible(false);
        if (getView() != null) {
            loadRidingSummary(getRiderId());
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    private void setAppBarVisible(boolean visible) {
        View appBar = requireActivity().findViewById(R.id.app_bar_layout);
        if (appBar != null) {
            appBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
