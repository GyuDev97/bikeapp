package com.example.testapplication.Adapter;

import android.content.Context;
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
import com.example.testapplication.dto.CompClub;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;


public class CompClubAdapter extends RecyclerView.Adapter<CompClubAdapter.CompClubHolder> {
    public Context mContext;
    public ArrayList<CompClub> itemList;
    String Save_Path;
    public NavController controller;
    public MainViewModel model;

    public CompClubAdapter(Context mContext, ArrayList<CompClub> itemList, String save_path, NavController controller, MainViewModel model) {
        this.mContext = mContext;
        this.itemList = itemList;
        this.Save_Path = save_path;
        this.controller = controller;
        this.model = model;
    }

    @NonNull
    @Override
    public CompClubHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.activity_competitionclubitem , viewGroup,false);

        return new CompClubHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull CompClubHolder holder, int position) {

        //Bitmap myBitmap = BitmapFactory.decodeFile(new File(Save_Path + "/" + File_Name).getAbsolutePath());
        String name = itemList.get(position).getCb_name();
        int score = itemList.get(position).getCs_score();
        String Club_image = itemList.get(position).getCb_image();

        Picasso.get().load("http://13.209.229.237:8080/app/getGPX/club/" + Club_image)
                .into(holder.imageView);

        //holder.imageView.setImageBitmap(myBitmap);
        holder.textView1.setText((position+1) + "등");
        holder.textView2.setText(name);
        holder.textView3.setText(String.valueOf(score));

    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    class CompClubHolder extends RecyclerView.ViewHolder {
        public TextView textView1, textView2, textView3;
        public ImageView imageView;
        public LinearLayout viewClick;
        public final View mView;

        public CompClubHolder( View itemView) {
            super(itemView);
            mView = itemView;
            textView1 = itemView.findViewById(R.id.textView10);
            textView2 = itemView.findViewById(R.id.textView11);
            textView3 = itemView.findViewById(R.id.textView12);
            imageView = itemView.findViewById(R.id.imageView7);
            viewClick = itemView.findViewById(R.id.viewClick);
        }
    }
}