package com.example.testapplication.ui.recycler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapplication.MainViewModel;
import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;

public class MyReportAdapter extends RecyclerView.Adapter<MyReportAdapter.MyRecordHolder>{

    public Context mContext;
    public ArrayList<RidingRecord> itemList;
    String Save_Path;
    public NavController controller;
    public MainViewModel model;


    public MyReportAdapter(Context mContext, ArrayList<RidingRecord> itemList, NavController controller, MainViewModel model) {
        this.mContext = mContext;
        this.itemList = itemList;
        this.controller = controller;
        this.model = model;
    }

    @Override
    public MyRecordHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {

        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.myreport_item , viewGroup,false);

        return new MyRecordHolder(v);
    }

    @SuppressLint("ResourceType")
    @Override
    public void onBindViewHolder(@NonNull final MyRecordHolder testViewHolder, final int position) {
        //item = itemList.get(position);
        int hour;
        int min;
        int sec;
        sec = (itemList.get(position).getRr_time());

        min = sec/60;
        hour = min/60;
        sec = sec % 60;
        min = min % 60;
        if(hour == 0){
            hour=00;
        }
        if(min==0){
            min=00;
        }

        //휴식시간 계산
        int b_hour, b_min, b_sec = itemList.get(position).getRr_breaktime();
        b_min = b_sec/60; b_hour = b_min/60; b_sec = b_sec % 60; b_min = b_min % 60;

        int des = (itemList.get(position).getRr_distance());
        float km = (float) (des/1000.0);
        String total = km +"Km";
        String hour_s = String.valueOf(hour);



        Picasso.get().load("http://13.209.229.237:8080/app/getGPX/rider/" + model.r_Image.getValue())
                .into(testViewHolder.my_image);


        String substring = itemList.get(position).getRr_date();
        String sub1 = substring.substring(0,10);
        String substring2 = itemList.get(position).getRr_date();
        String sub2 = substring2.substring(11,19);


        testViewHolder.my_rec_name.setText(itemList.get(position).getRr_name());
        testViewHolder.my_rec_date.setText(sub1+" " +sub2);
        testViewHolder.my_rec_adress.setText(itemList.get(position).getRr_area());
        testViewHolder.my_rec_dis.setText(total);
        testViewHolder.my_rec_get.setText(itemList.get(position).getRr_high() +"m");
        testViewHolder.my_rec_time.setText(hour_s+"시간 "+min+"분 "+ sec+"초");

        int finalMin = min;
        int finalSec = sec;
        int finalB_min = b_min;
        int finalB_sec = b_sec;
        testViewHolder.mView.setOnClickListener(v -> {
            controller.navigate(R.id.action_nav_records_to_myDetailFragment);
            model.r_num.setValue(String.valueOf(itemList.get(position).getRr_num()));
            model.my_rec_name.setValue(itemList.get(position).getRr_name());
            model.my_rec_date.setValue(itemList.get(position).getRr_date());
            model.my_rec_adress.setValue(itemList.get(position).getRr_area());
            model.my_rec_dis.setValue(total);
            model.my_rec_get.setValue(itemList.get(position).getRr_high() +"m");
            model.my_rec_time.setValue(hour_s+"시간 "+ finalMin +"분 "+ finalSec +"초");
            model.my_rec_rest.setValue(b_hour+"시간 "+ finalB_min +"분 "+ finalB_sec +"초");
            model.my_rec_open.setValue(itemList.get(position).getRr_open());
            model.my_rec_max.setValue(itemList.get(position).getRr_topspeed() +"km/h");
            model.my_rec_avg.setValue(itemList.get(position).getRr_avgspeed() +"km/h");
            model.my_rec_gpx.setValue(itemList.get(position).getRr_gpx());
        });
    }


    class MyRecordHolder extends RecyclerView.ViewHolder {
        public TextView my_rec_name, my_rec_date,my_rec_adress, my_rec_dis, my_rec_get, my_rec_time;
        public ImageView my_image;
        public LinearLayout viewClick;
        public final View mView;
        public MyRecordHolder( View itemView) {
            super(itemView);
            mView = itemView;
            my_rec_name = itemView.findViewById(R.id.record_name);
            my_rec_date = itemView.findViewById(R.id.record_date);
            my_rec_adress = itemView.findViewById(R.id.record_adress);
            my_rec_dis = itemView.findViewById(R.id.my_dis);
            my_rec_get = itemView.findViewById(R.id.my_get);
            my_rec_time = itemView.findViewById(R.id.my_time);
            my_image = itemView.findViewById(R.id.myimage);
            viewClick = itemView.findViewById(R.id.viewClick);
        }

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }
}