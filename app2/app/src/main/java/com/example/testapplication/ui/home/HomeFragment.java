package com.example.testapplication.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.riding.StartActivity;
import com.squareup.picasso.Picasso;


import java.io.File;
import java.util.List;

import retrofit2.Call;

import java.text.SimpleDateFormat;
import java.util.Date;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends BaseFragment {
    private Call<List<RidingRecord>> MyInfo;
    private ImageView ridingstart;
    private TextView mainTime, mainKm, rider, main_dis;
    public static HomeFragment newInstance(){
        return new HomeFragment();
    }
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_home, container, false);

    }

    @SuppressLint("WrongViewCast")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);


        String Save_Path = requireContext().getFilesDir().getPath();

        SharedPreferences auto = requireContext().getSharedPreferences("auto", Activity.MODE_PRIVATE);
        String r_image = auto.getString("r_image", "");



        ridingstart = (ImageView) view.findViewById(R.id.ridingstart);
        mainTime = (TextView) view.findViewById(R.id.main_time);
        mainKm = (TextView) view.findViewById(R.id.main_km);
        rider = (TextView) view.findViewById(R.id.idinfo);
        main_dis = (TextView) view.findViewById(R.id.main_dis);
        rider.setText(model.r_Nickname.getValue()+"님 저희와 함께");
        ridingstart.setOnClickListener(v->{
           Intent intent = new Intent(getActivity(), StartActivity.class);
           startActivity(intent);
        });

        MyInfo = serverApi.getMyRecord(model.r_Id.getValue());
        MyInfo.enqueue(new Callback<List<RidingRecord>>() {
            @Override
            public void onResponse(Call<List<RidingRecord>> call, Response<List<RidingRecord>> response) {
                if(response.code()==200){
                    String oTime="";//현재날짜

                    SimpleDateFormat mSimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd");
                    Date currentTime = new Date();
                    oTime = mSimpleDateFormat.format(currentTime);

                    try{
                        List<RidingRecord> record = response.body();
                        int dis = 0;
                        int riding_time = 0;
                        int killlo=0;
                        int total_dis=0;
                        String date = "";
                        for(RidingRecord test : record) {
                            dis += test.getRr_distance();
                            riding_time += test.getRr_time();
                            date = test.getRr_date();
                            total_dis += test.getRr_distance();
                            //오늘 주행한거
                            Log.d("내용확인",oTime+" "+ date.substring(0,10));
                            if(oTime.equals(date.substring(0,10))) {
                                Log.d("if문임","if문임");
                                int hour;
                                int min;
                                int sec = riding_time;

                                min = sec / 60;
                                hour = min / 60;
                                sec = sec % 60;
                                min = min % 60;
                                if (hour == 0) {
                                    hour = 0;
                                }
                                if (min == 0) {
                                    min = 0;
                                }
                                mainTime.setText(hour + "시간 " + min + "분 " + sec + "초");

                                killlo = (int) (dis / 1000.0);

                                if (dis >= 1000) {
                                    mainKm.setText(killlo + "km");
                                } else {
                                    mainKm.setText(dis + "m");
                                }

                            }else{
                                Log.d("dfdf","if문밖");

                            }
                            killlo = (int) (total_dis / 1000.0);
                            Log.d("메인", String.valueOf(killlo));
                            if (total_dis >= 1000) {

                                main_dis.setText(killlo +"km를 주행하셨어요");
                            }else{
                                main_dis.setText(total_dis+"m");
                            }
                        }
                    }catch(Exception e){
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<List<RidingRecord>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}