package com.example.testapplication.ui.records;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.gpx.GPXParser;
import com.example.testapplication.gpx.Gpx;
import com.example.testapplication.gpx.Track;
import com.example.testapplication.gpx.TrackPoint;
import com.example.testapplication.gpx.TrackSegment;
import com.example.testapplication.ui.BaseFragment;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDetailFragment extends BaseFragment implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;


    private String rr_num, LoginId;
    private int rr_open;
    private GPXParser mParser = new GPXParser();
    private Gpx parsedGpx = null;
    private MapView mapView;
    private MapPolyline polyline = new MapPolyline();
    private LinearLayout set_noopen, set_open;
    private SharedPreferences auto;
    private ImageView userImage;
    private Call<RidingRecord> record;
    List<Track> tracks;
    private TextView RidingTime, RidingRest, RidingDistance, RidingMax, RidingAvg, RidingGet, RidingAddr;

    public MyDetailFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyDetailFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyDetailFragment newInstance(String param1, String param2) {
        MyDetailFragment fragment = new MyDetailFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mapView = new MapView(requireActivity());
        ViewGroup mapViewContainer = (ViewGroup) view.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        //폴리라인 그리자~
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(255, 255, 51, 0)); // Polyline 컬러 지정.

        /* 로그인 정보 가져오기 */
        LoginId = model.r_Id.getValue();
        rr_num = model.r_num.getValue();

        RidingTime = (TextView) view.findViewById(R.id.Riding_time);
        RidingRest = (TextView) view.findViewById(R.id.Riding_rest);
        RidingDistance = (TextView) view.findViewById(R.id.Riding_distance);
        RidingMax = (TextView) view.findViewById(R.id.Riding_max);
        RidingAvg = (TextView) view.findViewById(R.id.Riding_avg);
        RidingGet = (TextView) view.findViewById(R.id.Riding_godo);
        RidingAddr = (TextView) view.findViewById(R.id.Riding_addr);
        RidingTime.setText(model.my_rec_time.getValue());
        RidingRest.setText(model.my_rec_rest.getValue());
        RidingDistance.setText(model.my_rec_dis.getValue());
        RidingMax.setText(model.my_rec_max.getValue());
        RidingAvg.setText(model.my_rec_avg.getValue());
        RidingGet.setText(model.my_rec_get.getValue());
        RidingAddr.setText(model.my_rec_adress.getValue());
        set_open = (LinearLayout) view.findViewById(R.id.set_open);
        set_noopen = (LinearLayout) view.findViewById(R.id.set_noopen);

        set_open.setVisibility(View.GONE);
        set_noopen.setVisibility(View.GONE);

        RidingRecord setRecord = new RidingRecord();
        setRecord.setRr_num(Integer.parseInt(rr_num));
        rr_open = model.my_rec_open.getValue();
        if(rr_open == 1){
            set_open.setVisibility(View.VISIBLE);
            set_noopen.setVisibility(View.GONE);
            setRecord.setRr_open(0);
            HashMap<String, Object> input = new HashMap<>();
            input.put("rr_num",rr_num);
            input.put("rr_open","0");
            set_open.setOnClickListener(v->{
                record = serverApi.setOpen(input);
                record.enqueue(new Callback<RidingRecord>() {
                    @Override
                    public void onResponse(Call<RidingRecord> call, Response<RidingRecord> response) {
                        if(response.code()==200){
                            set_open.setVisibility(View.GONE);
                            set_noopen.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onFailure(Call<RidingRecord> call, Throwable t) {

                    }
                });
            });

        }else{
            set_noopen.setVisibility(View.VISIBLE);
            set_open.setVisibility(View.GONE);

            HashMap<String, Object> input = new HashMap<>();
            input.put("rr_num",rr_num);
            input.put("rr_open","1");
            set_noopen.setOnClickListener(v->{
                record = serverApi.setOpen(input);
                record.enqueue(new Callback<RidingRecord>() {
                    @Override
                    public void onResponse(Call<RidingRecord> call, Response<RidingRecord> response) {
                        if(response.code()==200){
                            set_open.setVisibility(View.VISIBLE);
                            set_noopen.setVisibility(View.GONE);
                        }
                    }
                    @Override
                    public void onFailure(Call<RidingRecord> call, Throwable t) {
                        t.printStackTrace();
                    }
                });
            });
        }







        Thread uThread = new Thread() {
            @Override
            public void run() {
                try {
                    URL url = new URL("http://13.209.229.237:8080/app/getGPX/gpx/"+model.my_rec_gpx.getValue());
                    HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦
                    conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)
                    InputStream is = conn.getInputStream(); //inputStream 값 가져오기
                    // InputStream in = getAssets().open("and.gpx");
                    parsedGpx = mParser.parse(is);

                    if (parsedGpx != null) {
                        // log stuff
                        tracks = parsedGpx.getTracks();
                        for (int i = 0; i < tracks.size(); i++) {
                            Track track = tracks.get(i);
                            Log.d("track ", i + ":");
                            List<TrackSegment> segments = track.getTrackSegments();
                            for (int j = 0; j < segments.size(); j++) {
                                TrackSegment segment = segments.get(j);
                                for (TrackPoint trackPoint : segment.getTrackPoints()) {
                                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(trackPoint.getLatitude(), trackPoint.getLongitude()));
                                    Log.d("point: lat ", + trackPoint.getLatitude() + ", lon " + trackPoint.getLongitude());
                                }
                            }
                        }

                        // Polyline 지도에 올리기.
                        mapView.addPolyline(polyline);

                        // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
                        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                        int padding = 100; // px
                        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));

                    } else {
                        Log.e("error","Error parsing gpx track!");
                    }
                } catch (MalformedURLException e) {
                    e.printStackTrace();
                } catch (IOException | XmlPullParserException e) {
                    e.printStackTrace();
                }
            }
        };
        uThread.start(); // 작업 Thread 실행
    }
    @Override
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {

    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }

    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float v) {

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {

    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {

    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {

    }
}