package com.example.testapplication.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapplication.R;
import com.example.testapplication.dto.DangerousArea;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;


public class DangerAdapter extends RecyclerView.Adapter<DangerAdapter.DangerHolder> {
    public Context mContext;
    public ArrayList<DangerousArea> itemList;
    public GoogleMap mMap;

    public DangerAdapter(Context mContext, ArrayList<DangerousArea> itemList, GoogleMap mMap) {
        this.mContext = mContext;
        this.itemList = itemList;
        this.mMap = mMap;
    }

    @NonNull
    @Override
    public DangerHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_dangeritem , viewGroup,false);

        return new DangerHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull DangerHolder holder, int position) {
        String status ="";

        holder.mView.setOnClickListener( v -> {
            mMap.clear();

            MarkerOptions mOptions = new MarkerOptions();
            // 마커 타이틀
            mOptions.title("마커 좌표");

            mOptions.position(new LatLng(Double.parseDouble(itemList.get(position).getDa_latitude()), Double.parseDouble(itemList.get(position).getDa_longitude())));

            mMap.addMarker(mOptions);

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(Double.parseDouble(itemList.get(position).getDa_latitude()), Double.parseDouble(itemList.get(position).getDa_longitude())), 15));
        });

        //holder.imageView.setImageBitmap(myBitmap);
        holder.textView1.setText(itemList.get(position).getDa_addr());
        holder.textView2.setText(itemList.get(position).getDa_content());
        if (itemList.get(position).getDa_status().equals("0"))
            status = "신청중";
        if (itemList.get(position).getDa_status().equals("1"))
            status = "수락";
        if (itemList.get(position).getDa_status().equals("2"))
            status = "거절";
        holder.textView3.setText(status);

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class DangerHolder extends RecyclerView.ViewHolder {
        public TextView textView1, textView2, textView3;
        public LinearLayout viewClick;
        public final View mView;

        public DangerHolder( View itemView) {
            super(itemView);
            mView = itemView;
            textView1 = itemView.findViewById(R.id.textView32);
            textView2 = itemView.findViewById(R.id.textView34);
            textView3 = itemView.findViewById(R.id.textView35);
            viewClick = itemView.findViewById(R.id.viewClick);
        }
    }
}