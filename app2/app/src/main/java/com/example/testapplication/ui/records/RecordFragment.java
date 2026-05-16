package com.example.testapplication.ui.records;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
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
        view.findViewById(R.id.records_nav_home).setOnClickListener(v -> navigateTo(R.id.nav_home));
        view.findViewById(R.id.records_nav_course).setOnClickListener(v -> navigateTo(R.id.nav_course_view));
        view.findViewById(R.id.records_nav_community).setOnClickListener(v -> navigateTo(R.id.nav_competition_view));
        view.findViewById(R.id.records_nav_profile).setOnClickListener(v -> navigateTo(R.id.setting));
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

                    bindSummary(itemList);
                    MyReportAdapter adapter = new MyReportAdapter(requireContext().getApplicationContext(), itemList, controller, model);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(adapter);
                } else {
                    Log.d("RecordFragment", "record response failed: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ArrayList<RidingRecord>> call, Throwable t) {
                t.printStackTrace();
                Log.d("RecordFragment", "record request failed");
            }
        });
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
        setAppBarVisible(true);
    }

    private void setAppBarVisible(boolean visible) {
        View appBar = requireActivity().findViewById(R.id.app_bar_layout);
        if (appBar != null) {
            appBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }
}
