package com.example.testapplication.ui.records;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.recycler.MyReportAdapter;
import com.example.testapplication.ui.riding.StartActivity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordFragment extends BaseFragment {
    private Call<ArrayList<RidingRecord>> request;
    private RecyclerView recyclerView;
    private TextView countText;
    private TextView totalDistanceText;
    private TextView totalHeightText;
    private TextView totalTimeText;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        countText = view.findViewById(R.id.records_count_text);
        totalDistanceText = view.findViewById(R.id.records_total_distance);
        totalHeightText = view.findViewById(R.id.records_total_height);
        totalTimeText = view.findViewById(R.id.records_total_time);

        bindHeader(view);
        bindBottomNavigation(view);
        loadRecords();
    }

    private void bindHeader(View view) {
        View menuButton = view.findViewById(R.id.records_menu_button);
        if (menuButton != null) {
            menuButton.setOnClickListener(v -> {
                DrawerLayout drawerLayout = requireActivity().findViewById(R.id.drawer_layout);
                if (drawerLayout != null) {
                    drawerLayout.openDrawer(GravityCompat.START);
                }
            });
        }
    }

    private void bindBottomNavigation(View view) {
        view.findViewById(R.id.bottom_nav_home).setOnClickListener(v -> navigateTo(R.id.nav_home));
        view.findViewById(R.id.bottom_nav_course).setOnClickListener(v -> navigateTo(R.id.nav_course_view));
        view.findViewById(R.id.bottom_nav_profile).setOnClickListener(v -> navigateTo(R.id.setting));
        view.findViewById(R.id.bottom_nav_ride).setOnClickListener(
                v -> startActivity(new Intent(requireContext(), StartActivity.class))
        );
        TextView records = view.findViewById(R.id.bottom_nav_records);
        records.setTextColor(android.graphics.Color.parseColor("#1F7A2E"));
        records.setTypeface(null, android.graphics.Typeface.BOLD);
    }

    private void navigateTo(int destinationId) {
        if (controller.getCurrentDestination() == null || controller.getCurrentDestination().getId() != destinationId) {
            controller.navigate(destinationId);
        }
    }

    private void loadRecords() {
        request = serverApi.getRecord(getRiderId());
        request.enqueue(new Callback<ArrayList<RidingRecord>>() {
            @Override
            public void onResponse(Call<ArrayList<RidingRecord>> call, Response<ArrayList<RidingRecord>> response) {
                if (!isAdded()) {
                    return;
                }

                if (response.code() == 200 && response.body() != null) {
                    ArrayList<RidingRecord> itemList = new ArrayList<>(response.body());
                    Collections.sort(itemList, new Comparator<RidingRecord>() {
                        @Override
                        public int compare(RidingRecord left, RidingRecord right) {
                            String leftDate = left.getRr_date() == null ? "" : left.getRr_date();
                            String rightDate = right.getRr_date() == null ? "" : right.getRr_date();
                            return rightDate.compareTo(leftDate);
                        }
                    });

                    showRecords(itemList);
                } else {
                    Log.d("RecordFragment", "record response failed: " + response.code());
                    showRecords(createPreviewRecords());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<RidingRecord>> call, Throwable t) {
                t.printStackTrace();
                Log.d("RecordFragment", "record request failed");
                if (isAdded()) {
                    showRecords(createPreviewRecords());
                }
            }
        });
    }

    private void showRecords(ArrayList<RidingRecord> records) {
        bindSummary(records);
        MyReportAdapter adapter = new MyReportAdapter(
                requireContext().getApplicationContext(),
                records,
                controller,
                model
        );
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
    }

    private ArrayList<RidingRecord> createPreviewRecords() {
        ArrayList<RidingRecord> records = new ArrayList<>();
        records.add(createPreviewRecord(-1, "2026-05-16 09:41:00", "대구 앞산 라이딩", 12450, 45, 6332, 34, 19));
        records.add(createPreviewRecord(-2, "2026-05-15 08:12:00", "팔공산 코스", 18320, 780, 9312, 39, 18));
        records.add(createPreviewRecord(-3, "2026-05-12 18:20:00", "금호강 라이딩", 9840, 120, 4368, 31, 17));
        records.add(createPreviewRecord(-4, "2026-05-10 07:30:00", "비슬산 업힐", 22170, 1250, 11905, 42, 16));
        records.add(createPreviewRecord(-5, "2026-05-07 16:05:00", "대명저수지 한바퀴", 8630, 210, 3861, 29, 15));
        return records;
    }

    private RidingRecord createPreviewRecord(
            int id,
            String date,
            String name,
            int distance,
            int height,
            int seconds,
            int topSpeed,
            int averageSpeed
    ) {
        RidingRecord record = new RidingRecord();
        record.setRr_num(id);
        record.setRr_date(date);
        record.setRr_name(name);
        record.setRr_area(name);
        record.setRr_distance(distance);
        record.setRr_high(height);
        record.setRr_time(seconds);
        record.setRr_topspeed(topSpeed);
        record.setRr_avgspeed(averageSpeed);
        return record;
    }

    private String getRiderId() {
        String riderId = model.r_Id.getValue();
        if (riderId != null && riderId.length() > 0) {
            return riderId;
        }

        SharedPreferences auto = requireContext().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        return auto.getString("LoginId", "admin");
    }

    private void bindSummary(ArrayList<RidingRecord> records) {
        int totalDistance = 0;
        int totalHeight = 0;
        int totalSeconds = 0;

        for (RidingRecord record : records) {
            totalDistance += record.getRr_distance();
            totalHeight += record.getRr_high();
            totalSeconds += record.getRr_time();
        }

        countText.setText("총 " + records.size() + "개의 라이딩 기록");
        totalDistanceText.setText(String.format(Locale.KOREA, "%.1f km", totalDistance / 1000.0));
        totalHeightText.setText(String.format(Locale.KOREA, "%,d m", totalHeight));
        totalTimeText.setText(formatTotalTime(totalSeconds));
    }

    private String formatTotalTime(int seconds) {
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        if (hours > 0) {
            return hours + "시간 " + minutes + "분";
        }
        return minutes + "분";
    }

    @Override
    public void onResume() {
        super.onResume();
        setAppBarVisible(false);
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
