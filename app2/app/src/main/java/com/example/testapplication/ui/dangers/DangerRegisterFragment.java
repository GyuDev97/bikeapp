package com.example.testapplication.ui.dangers;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.os.ParcelFileDescriptor;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.testapplication.R;
import com.example.testapplication.dto.DangerousArea;
import com.example.testapplication.net.Server;
import com.example.testapplication.ui.BaseFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.UiSettings;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class DangerRegisterFragment extends BaseFragment implements OnMapReadyCallback, GoogleMap.OnMapClickListener{
    private ImageView danger_image;
    private GoogleMap mMap;
    private Geocoder geocoder;
    private EditText searchEdit, contentEdit, image_edit;
    private Button searchButton, registerButton;
    private Uri temp;
    private Call<Void> upload;
    private Call<Void> insertDanger;

    private Double searchLatitude, searchLongitude, clickLatitude, clickLongitude;
    private boolean click = false;
    private String change_Name="", searchAddress="", clickAddress="", Save_Path;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_danger_register, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchEdit = view.findViewById(R.id.editText4);
        searchButton = view.findViewById(R.id.button7);
        contentEdit = view.findViewById(R.id.editText6);
        image_edit = view.findViewById(R.id.editText7);
        registerButton = view.findViewById(R.id.dg_register);
        danger_image = view.findViewById(R.id.danger_image);
        Save_Path = requireContext().getFilesDir().getPath();

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        danger_image.setOnClickListener(v -> {
            Intent register = new Intent();
            register.setType("image/*");
            register.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(register,1);
        });

        registerButton.setOnClickListener( v-> {
            try {
                ParcelFileDescriptor pf = requireActivity().getContentResolver().openFileDescriptor(temp, "r", null);
                if (pf != null) {
                    InputStream is = new FileInputStream(pf.getFileDescriptor());
                    File file = new File(Save_Path + "/" + change_Name);
                    try (OutputStream os = new FileOutputStream(file)) {
                        Log.e("네임", change_Name);
                        IOUtils.copy(is, os);
                        MultipartBody.Part part = Server.getFilePart(file.getPath(), change_Name);
                        upload = Server.getInstance().getApi().upload(part, "danger", change_Name);
                        upload.enqueue(new Callback<Void>() {
                            @Override
                            public void onResponse(Call<Void> call, Response<Void> response) {
                                Log.e("성공",String.valueOf(response.code()));
                            }
                            @Override
                            public void onFailure(Call<Void> call, Throwable t) {
                                Log.e("에러", t.toString());
                            }
                        });

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            DangerousArea d_area = new DangerousArea();
            d_area.setDa_rider(model.r_Id.getValue());
            if ( click == false )
                d_area.setDa_addr(searchAddress);
            else
                d_area.setDa_addr(clickAddress);
            d_area.setDa_content(String.valueOf(contentEdit.getText()));
            d_area.setDa_image(change_Name);
            if ( click == false ){
                d_area.setDa_latitude(String.valueOf(searchLatitude));
                d_area.setDa_longitude(String.valueOf(searchLongitude));
            }
            else {
                d_area.setDa_latitude(String.valueOf(clickLatitude));
                d_area.setDa_longitude(String.valueOf(clickLongitude));
            }
            insertDanger = serverApi.insertDanger(d_area);
            insertDanger.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    controller.popBackStack();
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    showServerFailure();
                }
            });

        });
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;
        geocoder = new Geocoder(requireContext());

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                googleMap.clear();
                MarkerOptions mOptions = new MarkerOptions();
                // 마커 타이틀
                mOptions.title("마커 좌표");
                clickLatitude = point.latitude; // 위도
                clickLongitude = point.longitude; // 경도

                mOptions.snippet(clickLatitude.toString() + ", " + clickLongitude.toString());

                mOptions.position(new LatLng(clickLatitude, clickLongitude));

                googleMap.addMarker(mOptions);

                Log.e("맵 좌표", "위도 : " + String.valueOf(point.latitude) + "\n 경도 : " + String.valueOf(point.longitude));

                List<Address> list = null;

                try {
                    list = geocoder.getFromLocation(
                            clickLatitude, // 위도
                            clickLongitude, // 경도
                            10); // 얻어올 값의 개수

                    if (list != null) {
                        if (list.size() == 0) {
                            Toast.makeText(requireContext(), "해당하는 위치의 주소가 없습니다", Toast.LENGTH_LONG).show();
                        } else {
                            click = true;
                            String[] splitStr = list.get(0).toString().split(",");
                            clickAddress = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
                            searchEdit.setText(clickAddress);
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        // 버튼 이벤트
        searchButton.setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                googleMap.clear();
                String str = searchEdit.getText().toString();
                List<Address> addressList = null;
                try {

                    addressList = geocoder.getFromLocationName(
                            str, // 주소
                            10);

                    if (addressList != null) {
                        if (addressList.size() == 0) {
                            Toast.makeText(requireContext(), "검색어를 다시 입력해주십시오.", Toast.LENGTH_LONG).show();
                        } else {
                            click = false;
                            Log.e("전체", addressList.get(0).toString());

                            // 콤마를 기준으로 split
                            String[] splitStr = addressList.get(0).toString().split(",");
                            searchAddress = splitStr[0].substring(splitStr[0].indexOf("\"") + 1, splitStr[0].length() - 2); // 주소
                            Log.e("주소", searchAddress);

                            searchLatitude = Double.parseDouble(splitStr[10].substring(splitStr[10].indexOf("=") + 1)); // 위도
                            searchLongitude = Double.parseDouble(splitStr[12].substring(splitStr[12].indexOf("=") + 1)); // 경도

                            // 좌표(위도, 경도) 생성
                            LatLng point = new LatLng(searchLatitude, searchLongitude);
                            // 마커 생성
                            MarkerOptions mOptions2 = new MarkerOptions();
                            mOptions2.title("검색 결과");
                            mOptions2.snippet(searchAddress);
                            mOptions2.position(point);
                            mMap.addMarker(mOptions2);
                            // 해당 좌표로 화면 줌
                            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(point, 15));
                        }
                    }
                } catch (Exception e) {

                }
            }
        });

        LatLng Seoul = new LatLng(37.5642135, 127.0016985);
        mMap.addMarker(new MarkerOptions().position(Seoul).title("초기 설정 서울"));
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(Seoul, 15));

        UiSettings mapUiSettings = mMap.getUiSettings();
        mapUiSettings.setZoomControlsEnabled(true);  // 줌버튼
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            try {
                // 선택한 이미지에서 비트맵 생성
                if ( data.getData() != null ) {
                    InputStream in = requireActivity().getContentResolver().openInputStream(data.getData());
                    temp = data.getData();
                    Bitmap img = BitmapFactory.decodeStream(in);
                    in.close();
                    danger_image.setImageBitmap(img);
                    change_Name = getImageNameToUri(data.getData());
                    image_edit.setText(change_Name);
                }
                // 이미지 표시
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public String getImageNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = requireActivity().getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        return imgName;
    }
}