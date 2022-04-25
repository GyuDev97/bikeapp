package com.example.testapplication.ui.records;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.ui.BaseFragment;
import com.example.testapplication.ui.recycler.MyReportAdapter;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecordFragment extends BaseFragment{
    private Call<ArrayList<RidingRecord>> request;
    private List<RidingRecord> items;

    private RecyclerView recyclerView;
    private MyReportAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        // 최소한의 코드
        return inflater.inflate(R.layout.fragment_records, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        recyclerView = view.findViewById(R.id.recyclerView);
        request = serverApi.getRecord(model.r_Id.getValue());


        ArrayList<RidingRecord> itemList = new ArrayList<>();
        request.enqueue(new Callback<ArrayList<RidingRecord>>() {
            @Override
            public void onResponse(Call<ArrayList<RidingRecord>> call, Response<ArrayList<RidingRecord>> response) {
                if(response.code()==200){
                    items = response.body();

                    for(RidingRecord item: items){
                        itemList.add(item);
                    }

                    MyReportAdapter adapter = new MyReportAdapter(requireContext().getApplicationContext(), itemList, controller, model);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(adapter);

                }
            }

            @Override
            public void onFailure(Call<ArrayList<RidingRecord>> call, Throwable t) {
                t.printStackTrace();
                Log.d("안됨","안댐");
            }
        });
    }
}