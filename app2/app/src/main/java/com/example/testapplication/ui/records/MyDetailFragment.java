package com.example.testapplication.ui.records;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.testapplication.R;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.gpx.GPXParser;
import com.example.testapplication.gpx.Gpx;
import com.example.testapplication.gpx.Track;
import com.example.testapplication.gpx.TrackPoint;
import com.example.testapplication.gpx.TrackSegment;
import com.example.testapplication.ui.BaseFragment;
import com.kakao.vectormap.GestureType;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.kakao.vectormap.route.RouteLine;
import com.kakao.vectormap.route.RouteLineLayer;
import com.kakao.vectormap.route.RouteLineOptions;
import com.kakao.vectormap.route.RouteLineSegment;
import com.kakao.vectormap.route.RouteLineStyle;
import com.kakao.vectormap.route.RouteLineStyles;
import com.kakao.vectormap.route.RouteLineStylesSet;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyDetailFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyDetailFragment extends BaseFragment {

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
    private com.kakao.vectormap.MapView mapView;
    private KakaoMap kakaoMap;
    private RouteLineLayer routeLineLayer;
    private RouteLineStyles routeLineStyles;
    private RouteLine routeLine;
    private LabelLayer markerLayer;
    private LabelStyles startMarkerStyles;
    private LabelStyles finishMarkerStyles;
    private Label startMarker;
    private Label finishMarker;
    private final ArrayList<LatLng> routePoints = new ArrayList<>();
    private boolean waitingForGpx;
    private TextView mapStatus;
    private ImageView mapFallback;
    private LinearLayout set_noopen, set_open;
    private ImageView userImage;
    private Call<RidingRecord> record;
    List<Track> tracks;
    private TextView RidingTime, RidingRest, RidingDistance, RidingMax, RidingAvg, RidingGet, RidingAddr;
    private TextView detailTitle, detailDate, detailTime, detailGraphHigh, detailAnalysisHeight, detailAnalysisAvg;

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
        LoginId = model.r_Id.getValue();
        rr_num = firstNotEmpty(model.r_num.getValue(), "-1");
        String gpxFile = firstNotEmpty(model.my_rec_gpx.getValue(), "");

        waitingForGpx = gpxFile.length() > 0 && !"null".equalsIgnoreCase(gpxFile);
        startNativeMap(view);
        if (waitingForGpx) {
            loadGpxRoute(gpxFile);
        }

        RidingTime = (TextView) view.findViewById(R.id.Riding_time);
        RidingRest = (TextView) view.findViewById(R.id.Riding_rest);
        RidingDistance = (TextView) view.findViewById(R.id.Riding_distance);
        RidingMax = (TextView) view.findViewById(R.id.Riding_max);
        RidingAvg = (TextView) view.findViewById(R.id.Riding_avg);
        RidingGet = (TextView) view.findViewById(R.id.Riding_godo);
        RidingAddr = (TextView) view.findViewById(R.id.Riding_addr);
        detailTitle = view.findViewById(R.id.detail_title);
        detailDate = view.findViewById(R.id.detail_date);
        detailTime = view.findViewById(R.id.detail_time);
        detailGraphHigh = view.findViewById(R.id.detail_graph_high);
        detailAnalysisHeight = view.findViewById(R.id.detail_analysis_height);
        detailAnalysisAvg = view.findViewById(R.id.detail_analysis_avg_text);

        View back = view.findViewById(R.id.detail_back);
        if (back != null) {
            back.setOnClickListener(v -> controller.popBackStack());
        }

        View shareButton = view.findViewById(R.id.detail_share_button);
        View shareTop = view.findViewById(R.id.detail_share_top);
        View.OnClickListener shareClick = v -> shareRecord();
        shareButton.setOnClickListener(shareClick);
        shareTop.setOnClickListener(shareClick);

        view.findViewById(R.id.detail_more).setOnClickListener(v ->
                Toast.makeText(requireContext(), "기록 메뉴", Toast.LENGTH_SHORT).show());
        view.findViewById(R.id.detail_delete_button).setOnClickListener(v ->
                new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                        .setTitle("기록 삭제")
                        .setMessage("삭제 기능은 서버 연결 후 사용할 수 있습니다.")
                        .setPositiveButton("확인", null)
                        .show());

        setTextIfPresent(detailTitle, firstNotEmpty(model.my_rec_name.getValue(), model.my_rec_adress.getValue(), "라이딩 기록"));
        setTextIfPresent(detailDate, formatDate(model.my_rec_date.getValue()));
        setTextIfPresent(detailTime, formatClock(model.my_rec_date.getValue()));
        setTextIfPresent(RidingTime, firstNotEmpty(model.my_rec_time.getValue(), "-"));
        setTextIfPresent(RidingRest, estimateCalories(model.my_rec_dis.getValue()));
        setTextIfPresent(RidingDistance, firstNotEmpty(model.my_rec_dis.getValue(), "-"));
        setTextIfPresent(RidingMax, firstNotEmpty(model.my_rec_max.getValue(), "-"));
        setTextIfPresent(RidingAvg, firstNotEmpty(model.my_rec_avg.getValue(), "-"));
        setTextIfPresent(RidingGet, firstNotEmpty(model.my_rec_get.getValue(), "-"));
        setTextIfPresent(RidingAddr, firstNotEmpty(model.my_rec_adress.getValue(), "-"));
        String height = firstNotEmpty(model.my_rec_get.getValue(), "0 m");
        String average = firstNotEmpty(model.my_rec_avg.getValue(), "0 km/h");
        setTextIfPresent(detailGraphHigh, "▲ 최고 " + height);
        setTextIfPresent(detailAnalysisHeight, "해발 " + height);
        setTextIfPresent(detailAnalysisAvg, "평균 " + average);
        set_open = (LinearLayout) view.findViewById(R.id.set_open);
        set_noopen = (LinearLayout) view.findViewById(R.id.set_noopen);

        set_open.setVisibility(View.GONE);
        set_noopen.setVisibility(View.GONE);

        Integer openValue = model.my_rec_open.getValue();
        rr_open = openValue == null ? 0 : openValue;
        if(rr_open == 1){
            set_open.setVisibility(View.VISIBLE);
            set_noopen.setVisibility(View.GONE);
            HashMap<String, Object> input = new HashMap<>();
            input.put("rr_num",rr_num);
            input.put("rr_open","0");
            set_open.setOnClickListener(v->{
                if (isPreviewRecord()) {
                    set_open.setVisibility(View.GONE);
                    set_noopen.setVisibility(View.VISIBLE);
                    return;
                }
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
                if (isPreviewRecord()) {
                    set_noopen.setVisibility(View.GONE);
                    set_open.setVisibility(View.VISIBLE);
                    return;
                }
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
    }

    private void startNativeMap(View view) {
        mapView = view.findViewById(R.id.map_view);
        mapStatus = view.findViewById(R.id.detail_map_status);
        mapFallback = view.findViewById(R.id.detail_map_fallback);
        mapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                Log.d("RecordDetailMap", "Kakao map destroyed");
            }

            @Override
            public void onMapError(Exception error) {
                Log.e("RecordDetailMap", "Kakao map error", error);
                if (!isAdded()) {
                    return;
                }
                mapFallback.setVisibility(View.VISIBLE);
                mapStatus.setVisibility(View.VISIBLE);
                mapStatus.setText("지도를 불러오지 못했습니다");
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(KakaoMap readyMap) {
                kakaoMap = readyMap;
                kakaoMap.setGestureEnable(GestureType.Pan, true);
                kakaoMap.setGestureEnable(GestureType.Zoom, true);
                routeLineLayer = kakaoMap.getRouteLineManager().getLayer();
                routeLineStyles = RouteLineStyles.from(
                        RouteLineStyle.from(10, Color.rgb(30, 157, 70), 3, Color.WHITE)
                );
                markerLayer = kakaoMap.getLabelManager().getLayer();
                startMarkerStyles = kakaoMap.getLabelManager().addLabelStyles(
                        LabelStyles.from(LabelStyle.from(drawableToBitmap(R.drawable.riding_ic_play, 28)))
                );
                finishMarkerStyles = kakaoMap.getLabelManager().addLabelStyles(
                        LabelStyles.from(LabelStyle.from(drawableToBitmap(R.drawable.riding_ic_flag, 28)))
                );
                mapFallback.setVisibility(View.GONE);
                mapStatus.setVisibility(View.GONE);
                if (!waitingForGpx) {
                    renderRoute();
                }
            }

            @Override
            public LatLng getPosition() {
                return LatLng.from(35.8329, 128.5650);
            }

            @Override
            public int getZoomLevel() {
                return 13;
            }
        });
    }

    private void loadGpxRoute(String gpxFile) {
        new Thread(() -> {
            ArrayList<LatLng> loadedPoints = new ArrayList<>();
            HttpURLConnection connection = null;
            try {
                URL url = new URL("http://13.209.229.237:8080/app/getGPX/gpx/" + gpxFile);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(7000);
                connection.setReadTimeout(7000);
                connection.setDoInput(true);
                try (InputStream input = connection.getInputStream()) {
                    parsedGpx = mParser.parse(input);
                }
                if (parsedGpx != null) {
                    tracks = parsedGpx.getTracks();
                    for (Track track : tracks) {
                        for (TrackSegment segment : track.getTrackSegments()) {
                            for (TrackPoint point : segment.getTrackPoints()) {
                                loadedPoints.add(LatLng.from(point.getLatitude(), point.getLongitude()));
                            }
                        }
                    }
                }
            } catch (IOException | XmlPullParserException error) {
                Log.e("RecordDetailMap", "Unable to load GPX route", error);
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }

            if (!isAdded()) {
                return;
            }
            requireActivity().runOnUiThread(() -> {
                routePoints.clear();
                routePoints.addAll(loadedPoints);
                waitingForGpx = false;
                renderRoute();
            });
        }).start();
    }

    private void renderRoute() {
        if (kakaoMap == null || routeLineLayer == null || routeLineStyles == null) {
            return;
        }
        if (routePoints.size() < 2) {
            routePoints.clear();
            routePoints.addAll(createPreviewRoute());
        }

        RouteLineSegment segment = RouteLineSegment.from(new ArrayList<>(routePoints), routeLineStyles);
        if (routeLine == null) {
            routeLine = routeLineLayer.addRouteLine(
                    RouteLineOptions.from(segment).setStylesSet(RouteLineStylesSet.from(routeLineStyles))
            );
        } else {
            routeLine.changeSegments(segment);
        }

        LatLng start = routePoints.get(0);
        LatLng finish = routePoints.get(routePoints.size() - 1);
        if (startMarker == null) {
            startMarker = markerLayer.addLabel(
                    LabelOptions.from("record-start", start).setStyles(startMarkerStyles)
            );
            finishMarker = markerLayer.addLabel(
                    LabelOptions.from("record-finish", finish).setStyles(finishMarkerStyles)
            );
        } else {
            startMarker.moveTo(start);
            finishMarker.moveTo(finish);
        }
        kakaoMap.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory.fitMapPoints(
                        routePoints.toArray(new LatLng[0]), 80
                )
        );
    }

    private ArrayList<LatLng> createPreviewRoute() {
        ArrayList<LatLng> points = new ArrayList<>();
        points.add(LatLng.from(35.8387, 128.5484));
        points.add(LatLng.from(35.8358, 128.5529));
        points.add(LatLng.from(35.8371, 128.5588));
        points.add(LatLng.from(35.8327, 128.5631));
        points.add(LatLng.from(35.8342, 128.5694));
        points.add(LatLng.from(35.8300, 128.5748));
        points.add(LatLng.from(35.8334, 128.5809));
        points.add(LatLng.from(35.8370, 128.5862));
        return points;
    }

    private Bitmap drawableToBitmap(int drawableId, int sizeDp) {
        int sizePx = Math.round(sizeDp * getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = AppCompatResources.getDrawable(requireContext(), drawableId);
        if (drawable != null) {
            drawable.setBounds(0, 0, sizePx, sizePx);
            drawable.draw(canvas);
        }
        return bitmap;
    }

    private void shareRecord() {
        String title = firstNotEmpty(model.my_rec_name.getValue(), "라이딩 기록");
        String summary = title + "\n"
                + firstNotEmpty(model.my_rec_dis.getValue(), "-") + " · "
                + firstNotEmpty(model.my_rec_time.getValue(), "-") + " · "
                + firstNotEmpty(model.my_rec_avg.getValue(), "-");
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_SUBJECT, title);
        intent.putExtra(Intent.EXTRA_TEXT, summary);
        startActivity(Intent.createChooser(intent, "라이딩 기록 공유"));
    }

    private boolean isPreviewRecord() {
        try {
            return Integer.parseInt(rr_num) < 0;
        } catch (NumberFormatException e) {
            return true;
        }
    }
    private String firstNotEmpty(String... values) {
        for (String value : values) {
            if (value != null && value.trim().length() > 0) {
                return value.trim();
            }
        }
        return "";
    }

    private void setTextIfPresent(TextView textView, String value) {
        if (textView != null) {
            textView.setText(value);
        }
    }

    private String formatDate(String value) {
        Date date = parseDate(value);
        if (date != null) {
            return new SimpleDateFormat("yyyy. MM. dd (E)", Locale.KOREA).format(date);
        }
        return value == null ? "-" : value;
    }

    private String formatClock(String value) {
        Date date = parseDate(value);
        if (date != null) {
            return new SimpleDateFormat("a h:mm", Locale.KOREA).format(date);
        }
        return "-";
    }

    private Date parseDate(String value) {
        if (value == null || value.length() < 10) {
            return null;
        }

        String normalized = value.length() >= 19 ? value.substring(0, 19) : value;
        String[] patterns = {"yyyy-MM-dd HH:mm:ss", "yyyy-MM-dd HH:mm", "yyyy-MM-dd"};
        for (String pattern : patterns) {
            try {
                return new SimpleDateFormat(pattern, Locale.KOREA).parse(normalized);
            } catch (ParseException ignored) {
            }
        }
        return null;
    }

    private String estimateCalories(String distanceText) {
        if (distanceText == null) {
            return "0 kcal";
        }
        try {
            String number = distanceText.replace("km", "").trim();
            double km = Double.parseDouble(number);
            return Math.max(1, (int) Math.round(km * 50.0)) + " kcal";
        } catch (NumberFormatException e) {
            return "0 kcal";
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mapView != null) {
            mapView.resume();
        }
        setAppBarVisible(false);
    }

    @Override
    public void onPause() {
        if (mapView != null) {
            mapView.pause();
        }
        super.onPause();
        setAppBarVisible(true);
    }

    @Override
    public void onDestroyView() {
        if (mapView != null) {
            mapView.finish();
            mapView = null;
        }
        kakaoMap = null;
        routeLine = null;
        routeLineLayer = null;
        markerLayer = null;
        startMarker = null;
        finishMarker = null;
        routePoints.clear();
        super.onDestroyView();
    }

    private void setAppBarVisible(boolean visible) {
        View appBar = requireActivity().findViewById(R.id.app_bar_layout);
        if (appBar != null) {
            appBar.setVisibility(visible ? View.VISIBLE : View.GONE);
        }
    }

}
