package com.example.testapplication.ui.courses;

import android.graphics.Typeface;
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

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.recycler.CourseAdapter;

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

                ArrayList<Integer> likeCount = new ArrayList<Integer>();
                ArrayList<Integer> highCount = new ArrayList<Integer>();
                ArrayList<Integer> disCount = new ArrayList<Integer>();

                Log.e("테스트", body.get(1).getRr_rider());
                for(RidingRecord item: body) {
                    itemList.add(item);

                    likeCount.add(item.getRr_like());
                    highCount.add(item.getRr_high());
                    disCount.add(item.getRr_distance());
                }

                    Collections.sort(likeCount);
                    Collections.sort(highCount);
                    Collections.sort(disCount);

                    Collections.reverse(likeCount);
                    Collections.reverse(highCount);
                    Collections.reverse(disCount);

                    for ( int i = 0; i < likeCount.size(); i++){
                        for ( int j = 0; j < itemList.size(); j++) {
                            if (likeCount.get(i) == itemList.get(j).getRr_like()){
                                likeList.add(itemList.get(j));
                            }

                            if (highCount.get(i) == itemList.get(j).getRr_high()){
                                highList.add(itemList.get(j));
                            }

                            if (disCount.get(i) == itemList.get(j).getRr_distance()) {
                                disList.add(itemList.get(j));
                            }
                        }
                    }

                    courseAdapter = new CourseAdapter(requireContext().getApplicationContext(), itemList, r_image, controller, model);
                    likeAdapter = new CourseAdapter(requireContext().getApplicationContext(), likeList, r_image, controller, model);
                    highAdapter = new CourseAdapter(requireContext().getApplicationContext(), highList, r_image, controller, model);
                    disAdapter = new CourseAdapter(requireContext().getApplicationContext(), disList, r_image, controller, model);

                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(courseAdapter);
                }

            @Override
            public void onFailure(Call<List<RidingRecord>> call, Throwable t) {
                showServerFailure();
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

                moreButton.setOnClickListener( v -> {
                    if ( courseAdapter.itemList.size() - (courseAdapter.count+5) > 0) {
                        courseAdapter.count += 5;
                        likeAdapter.count += 5;
                        highAdapter.count += 5;
                        disAdapter.count += 5;
                        recyclerView.requestLayout();
                    }
                    else{
                        courseAdapter.count += courseAdapter.itemList.size() - courseAdapter.count;
                        likeAdapter.count += courseAdapter.itemList.size() - courseAdapter.count;
                        highAdapter.count += courseAdapter.itemList.size() - courseAdapter.count;
                        disAdapter.count += courseAdapter.itemList.size() - courseAdapter.count;
                        recyclerView.requestLayout();
                    }
                });
            }
        });

        textView1.setOnClickListener( v -> {
            recyclerView.setAdapter(courseAdapter);
            textView1.setTypeface(null, Typeface.BOLD);
            textView2.setTypeface(null, Typeface.NORMAL);
            textView3.setTypeface(null, Typeface.NORMAL);
            textView4.setTypeface(null, Typeface.NORMAL);
        });

        textView2.setOnClickListener( v -> {
            recyclerView.setAdapter(likeAdapter);
            textView1.setTypeface(null, Typeface.NORMAL);
            textView2.setTypeface(null, Typeface.BOLD);
            textView3.setTypeface(null, Typeface.NORMAL);
            textView4.setTypeface(null, Typeface.NORMAL);
        });

        textView3.setOnClickListener( v -> {
            recyclerView.setAdapter(highAdapter);
            textView1.setTypeface(null, Typeface.NORMAL);
            textView2.setTypeface(null, Typeface.NORMAL);
            textView3.setTypeface(null, Typeface.BOLD);
            textView4.setTypeface(null, Typeface.NORMAL);
        });

        textView4.setOnClickListener( v -> {
            recyclerView.setAdapter(disAdapter);
            textView1.setTypeface(null, Typeface.NORMAL);
            textView2.setTypeface(null, Typeface.NORMAL);
            textView3.setTypeface(null, Typeface.NORMAL);
            textView4.setTypeface(null, Typeface.BOLD);
        });
    }
}