package com.example.testapplication.ui.home;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.Navigation;

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.riding.StartActivity;

import java.text.SimpleDateFormat;
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

        ridingStart = view.findViewById(R.id.ridingstart);
        mainTime = view.findViewById(R.id.main_time);
        mainKm = view.findViewById(R.id.main_km);
        rider = view.findViewById(R.id.idinfo);
        mainDistance = view.findViewById(R.id.main_dis);

        String riderId = getRiderId();
        bindKoreanLabels(view, riderId);
        bindNavigation(view);

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

    private void bindKoreanLabels(View view, String riderId) {
        setText(view, R.id.home_hello, "\uC548\uB155\uD558\uC138\uC694, " + riderId + "\uB2D8!");
        setText(view, R.id.home_today_label, "\uC624\uB298 \uD65C\uB3D9:");
        setText(view, R.id.home_goal_label, "\uC624\uB298\uC758 \uBAA9\uD45C \uB2EC\uC131\uB3C4");
        setText(view, R.id.home_recent_title, "\uB098\uC758 \uCD5C\uADFC \uAE30\uB85D   >");
        setText(view, R.id.home_recent_course, "\uB2E8\uBAA9 \u00B7 LS7\uAE38");
        setText(view, R.id.home_recent_meta, "0.62 km / 12\uBD84 / 158 km");
        setText(view, R.id.home_danger_label, "\uC704\uD5D8 \uBCF4\uC548");
        setText(view, R.id.home_course_search_label, "\uCF54\uC2A4 \uAC80\uC0C9");
        setText(view, R.id.home_scrap_label, "\uC2A4\uD06C\uB7A9");
        setText(view, R.id.home_setting_label, "\uC124\uC815");
        setText(view, R.id.home_nav_home_label, "\uD648");
        setText(view, R.id.home_nav_records_label, "\uAE30\uB85D");
        setText(view, R.id.home_nav_course_label, "\uCF54\uC2A4");
        setText(view, R.id.home_nav_community_label, "\uCEE4\uBBA4\uB2C8\uD2F0");
        setText(view, R.id.home_nav_profile_label, "\uD504\uB85C\uD544");
        setText(view, R.id.ridingstart, "\uB77C\uC774\uB529 \uC2DC\uC791");
        rider.setText(riderId + "\uB2D8 \uC800\uD76C\uC640 \uD568\uAED8\n\uB77C\uC774\uB529\uC744 \uC8FC\uD589\uD558\uC138\uC694");
    }

    private void setText(View root, int textViewId, String text) {
        ((TextView) root.findViewById(textViewId)).setText(text);
    }

    private void bindNavigation(View view) {
        view.findViewById(R.id.recent_record_card).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_nav_records));
        view.findViewById(R.id.home_nav_records).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_nav_records));
        view.findViewById(R.id.home_nav_course).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_nav_course_view));
        view.findViewById(R.id.home_course_search).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_nav_course_search));
        view.findViewById(R.id.home_scrap).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_nav_course_scrap));
        view.findViewById(R.id.home_danger).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_nav_danger_view));
        view.findViewById(R.id.home_setting).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_setting));
        view.findViewById(R.id.home_nav_profile).setOnClickListener(v -> navigate(v, R.id.action_nav_home_to_setting));
    }

    private void navigate(View view, int actionId) {
        Navigation.findNavController(view).navigate(actionId);
    }

    private void loadRidingSummary(String riderId) {
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
                t.printStackTrace();
            }
        });
    }

    private void updateSummary(List<RidingRecord> records) {
        if (records == null) {
            return;
        }

        String today = new SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(new Date());
        int todayDistance = 0;
        int todaySeconds = 0;
        int totalDistance = 0;

        for (RidingRecord record : records) {
            int distance = record.getRr_distance();
            totalDistance += distance;

            String date = record.getRr_date();
            if (date != null && date.length() >= 10 && today.equals(date.substring(0, 10))) {
                todayDistance += distance;
                todaySeconds += record.getRr_time();
            }
        }

        mainKm.setText(formatDistance(todayDistance));
        mainTime.setText(formatDuration(todaySeconds));
        mainDistance.setText("\uB204\uC801: " + formatDistance(totalDistance));
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
}
