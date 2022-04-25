package com.example.testapplication.ui.dangers;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;

import com.example.testapplication.Adapter.DangerAdapter;
import com.example.testapplication.R;
import com.example.testapplication.dto.DangerousArea;
import com.example.testapplication.ui.BaseFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangerViewFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{

    public GoogleMap mMap;
    private DrawerLayout mDrawerLayout;
    private RecyclerView recyclerView;
    private ImageView userImage;
    private Call<List<DangerousArea>> getDanger;
    private Button registerButton;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danger_view, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        recyclerView = view.findViewById(R.id.recycle_danger);
        registerButton = view.findViewById(R.id.button3);

        getDanger = serverApi.getDanger(model.r_Id.getValue());
        getDanger.enqueue(new Callback<List<DangerousArea>>() {
            @Override
            public void onResponse(Call<List<DangerousArea>> call, Response<List<DangerousArea>> response) {
                if ( response.code() == 200 ) {
                    List<DangerousArea> body = response.body();

                    DangerAdapter nowAdapter = new DangerAdapter(requireContext().getApplicationContext(), (ArrayList<DangerousArea>)body, mMap);
                    recyclerView.setLayoutManager(new LinearLayoutManager(requireContext().getApplicationContext(), LinearLayoutManager.VERTICAL, false));
                    recyclerView.setAdapter(nowAdapter);
                }
            }

            @Override
            public void onFailure(Call<List<DangerousArea>> call, Throwable t) {

            }
        });

        registerButton.setOnClickListener( v -> {
            controller.navigate(R.id.action_nav_danger_view_to_dangerRegisterFragment);

        });

    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        LatLng Seoul = new LatLng(37.5642135, 127.0016985);
        mMap.addMarker(new MarkerOptions().position(Seoul).title("초기 설정 서울"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Seoul, 15));

        UiSettings mapUiSettings = mMap.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(true);  // 줌버튼
    }
}