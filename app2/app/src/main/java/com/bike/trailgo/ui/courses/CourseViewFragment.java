package com.bike.trailgo.ui.courses;

import android.graphics.Typeface;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bike.trailgo.R;
import com.bike.trailgo.dto.RidingRecord;
import com.bike.trailgo.ui.BaseFragment;
import com.bike.trailgo.ui.recycler.CourseAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CourseViewFragment extends BaseFragment {
    private Call<ArrayList<RidingRecord>> request;
    private List<RidingRecord> items;
    private Call<List<RidingRecord>> getCourseList;

    private RecyclerView recyclerView;
    String LoginId, Nickname, r_image;
    ImageView userImage;

    CourseAdapter courseAdapter, likeAdapter, highAdapter, disAdapter;
    Button moreButton;
    TextView textView1, textView2, textView3, textView4;
    ArrayList<RidingRecord> itemList = new ArrayList<>();
    ArrayList<RidingRecord> likeList = new ArrayList<>();
    ArrayList<RidingRecord> highList = new ArrayList<>();
    ArrayList<RidingRecord> disList = new ArrayList<>();


    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 최소한의 코드
        return inflater.inflate(R.layout.fragment_course_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        //request = serverApi.getRecord(model.r_Id.getValue());
        r_image = model.r_Image.getValue();
        moreButton = view.findViewById(R.id.more_button);
        textView1 = view.findViewById(R.id.textView17);
        textView2 = view.findViewById(R.id.textView18);
        textView3 = view.findViewById(R.id.textView19);
        textView4 = view.findViewById(R.id.textView20);

        getCourseList = serverApi.getCourseList();
        getCourseList.enqueue(new Callback<List<RidingRecord>>() {
            @Override
            public void onResponse(Call<List<RidingRecord>> call, Response<List<RidingRecord>> response) {

                List<RidingRecord> body = response.body();
                if (!isAdded() || !response.isSuccessful() || body == null) {
                    return;
                }

                    itemList.clear();
                    itemList.addAll(body);
                    if (itemList.isEmpty()) {
                        itemList.addAll(createPreviewCourses());
                    }
                    likeList.clear();
                    likeList.addAll(body);
                    highList.clear();
                    highList.addAll(body);
                    disList.clear();
                    disList.addAll(body);

                    Collections.sort(likeList, (left, right) -> Integer.compare(right.getRr_like(), left.getRr_like()));
                    Collections.sort(highList, (left, right) -> Integer.compare(right.getRr_high(), left.getRr_high()));
                    Collections.sort(disList, (left, right) -> Integer.compare(right.getRr_distance(), left.getRr_distance()));

                    courseAdapter = new CourseAdapter(requireContext().getApplicationContext(), itemList, r_image, controller, model);
                    likeAdapter = new CourseAdapter(requireContext().getApplicationContext(), likeList, r_image, controller, model);
                    highAdapter = new CourseAdapter(requireContext().getApplicationContext(), highList, r_image, controller, model);
                    disAdapter = new CourseAdapter(requireContext().getApplicationContext(), disList, r_image, controller, model);

                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(courseAdapter);
                    moreButton.setVisibility(itemList.size() > courseAdapter.count ? View.VISIBLE : View.GONE);
                }

            @Override
            public void onFailure(Call<List<RidingRecord>> call, Throwable t) {
                bindCourseList(createPreviewCourses());
            }
        });


        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {

            @Override
            public void onScrollStateChanged(@NonNull RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
            }

            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager) recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition() + 1;
                int totalCount = recyclerView.getAdapter().getItemCount();

                Log.e("끝포지션", String.valueOf(totalCount));
                Log.e("라스트포지션", String.valueOf(lastPosition));


                if(lastPosition == totalCount){
                    if ( courseAdapter.itemList.size() == courseAdapter.count){
                        moreButton.setVisibility(View.GONE);
                    }
                }

            }
        });

        moreButton.setOnClickListener(v -> {
            CourseAdapter activeAdapter = (CourseAdapter) recyclerView.getAdapter();
            if (activeAdapter == null) {
                return;
            }
            activeAdapter.count = Math.min(activeAdapter.itemList.size(), activeAdapter.count + 5);
            activeAdapter.notifyDataSetChanged();
            moreButton.setVisibility(activeAdapter.count >= activeAdapter.itemList.size() ? View.GONE : View.VISIBLE);
        });

        textView1.setOnClickListener( v -> {
            recyclerView.setAdapter(courseAdapter);
            selectTab(textView1);
        });

        textView2.setOnClickListener( v -> {
            recyclerView.setAdapter(likeAdapter);
            selectTab(textView2);
        });

        textView3.setOnClickListener( v -> {
            recyclerView.setAdapter(highAdapter);
            selectTab(textView3);
        });

        textView4.setOnClickListener( v -> {
            recyclerView.setAdapter(disAdapter);
            selectTab(textView4);
        });
    }

    private void bindCourseList(ArrayList<RidingRecord> courses) {
        itemList.clear();
        itemList.addAll(courses);
        likeList.clear();
        likeList.addAll(courses);
        highList.clear();
        highList.addAll(courses);
        disList.clear();
        disList.addAll(courses);
        courseAdapter = new CourseAdapter(requireContext().getApplicationContext(), itemList, r_image, controller, model);
        likeAdapter = new CourseAdapter(requireContext().getApplicationContext(), likeList, r_image, controller, model);
        highAdapter = new CourseAdapter(requireContext().getApplicationContext(), highList, r_image, controller, model);
        disAdapter = new CourseAdapter(requireContext().getApplicationContext(), disList, r_image, controller, model);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext()));
        recyclerView.setAdapter(courseAdapter);
        moreButton.setVisibility(View.GONE);
    }

    private ArrayList<RidingRecord> createPreviewCourses() {
        ArrayList<RidingRecord> previews = new ArrayList<>();
        previews.add(previewCourse(1, "팔공산 순환 코스", 32450, 1250, 8100, 18));
        previews.add(previewCourse(2, "비슬산 임월 코스", 22170, 1250, 7200, 17));
        previews.add(previewCourse(3, "금호강 자전거길", 18320, 120, 4350, 22));
        previews.add(previewCourse(4, "대명저수지 둘레길", 9840, 210, 3900, 15));
        return previews;
    }

    private RidingRecord previewCourse(
            int id, String name, int distance, int height, int seconds, int averageSpeed
    ) {
        RidingRecord record = new RidingRecord();
        record.setRr_num(id);
        record.setRr_name(name);
        record.setRr_distance(distance);
        record.setRr_high(height);
        record.setRr_time(seconds);
        record.setRr_avgspeed(averageSpeed);
        record.setRr_rider("TrailGO");
        record.setRr_date("2026-05-16 09:41:00");
        record.setRr_area("대구광역시");
        return record;
    }

    private void selectTab(TextView selected) {
        TextView[] tabs = {textView1, textView2, textView3, textView4};
        for (TextView tab : tabs) {
            boolean isSelected = tab == selected;
            tab.setTypeface(null, isSelected ? Typeface.BOLD : Typeface.NORMAL);
            tab.setTextColor(Color.parseColor(isSelected ? "#FFFFFF" : "#667069"));
            tab.setBackgroundResource(isSelected ? R.drawable.course_badge_bg : android.R.color.transparent);
        }
        CourseAdapter activeAdapter = (CourseAdapter) recyclerView.getAdapter();
        moreButton.setVisibility(activeAdapter != null && activeAdapter.count < activeAdapter.itemList.size()
                ? View.VISIBLE : View.GONE);
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
