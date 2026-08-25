package com.bike.trailgo.ui.riding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.app.ActivityCompat;
import com.bike.trailgo.R;
import com.bike.trailgo.SoundManager;
import com.bike.trailgo.databinding.ActivityRidingStartBinding;
import com.bike.trailgo.dto.DangerousArea;
import com.bike.trailgo.gpx.GPXParser;
import com.bike.trailgo.gpx.Gpx;
import com.bike.trailgo.gpx.Track;
import com.bike.trailgo.gpx.TrackPoint;
import com.bike.trailgo.gpx.TrackSegment;
import com.bike.trailgo.net.Server;
import com.bike.trailgo.net.ServerApi;
import com.kakao.vectormap.KakaoMap;
import com.kakao.vectormap.KakaoMapReadyCallback;
import com.kakao.vectormap.LatLng;
import com.kakao.vectormap.MapLifeCycleCallback;
import com.kakao.vectormap.GestureType;
import com.kakao.vectormap.route.RouteLine;
import com.kakao.vectormap.route.RouteLineLayer;
import com.kakao.vectormap.route.RouteLineOptions;
import com.kakao.vectormap.route.RouteLineSegment;
import com.kakao.vectormap.route.RouteLineStyle;
import com.kakao.vectormap.route.RouteLineStyles;
import com.kakao.vectormap.route.RouteLineStylesSet;
import com.kakao.vectormap.label.Label;
import com.kakao.vectormap.label.LabelLayer;
import com.kakao.vectormap.label.LabelOptions;
import com.kakao.vectormap.label.LabelStyle;
import com.kakao.vectormap.label.LabelStyles;
import com.google.android.material.tabs.TabLayout;
import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;
import org.json.JSONArray;
import org.json.JSONObject;
import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

@SuppressLint("HandlerLeak")
public class StartActivity extends AppCompatActivity
        implements LocationListener, MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener, MapView.POIItemEventListener {
    private static final int LOCATION_PERMISSION_REQUEST = 3101;
    private static final int NOTIFICATION_PERMISSION_REQUEST = 3102;
    private SoundPool soundPool;
    private SoundManager soundManager;
    private boolean play;
    private ActivityRidingStartBinding binding;
    private MapView mMapView;
    private com.kakao.vectormap.MapView kakaoV2MapView;
    private KakaoMap kakaoMap;
    private RouteLineLayer ridingRouteLayer;
    private RouteLine ridingRouteLine;
    private RouteLineStyles ridingRouteStyles;
    private final List<LatLng> ridingRoutePoints = new ArrayList<>();
    private LabelLayer currentLocationLayer;
    private Label currentLocationLabel;
    private LabelStyles currentLocationStyles;
    private boolean locationUpdatesStarted;
    private boolean rideLocationReceiverRegistered;
    private final BroadcastReceiver rideLocationReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(RideTrackingService.EXTRA_LOCATION);
            if (location != null) {
                onLocationChanged(location);
            }
        }
    };
    private MapPOIItem mCustomMarker;
    private LocationManager locationManager;
    private Location mLastlocation = null;
    private int cnt=0;

    private String getDalat, getDalon, getDacon, address_dong;
    //??醫딆┣??????⑤슢堉???
    private double latitude, lonngitude, getSpeed = 0, hap = 0, avg = 0, maX = 0, maxLat = 0, maxLon = 0, minLat = 1000, minLon = 1000;
    private static final double MIN_ELEVATION_GAIN_METERS = 3.0;
    private static final double MAX_REASONABLE_RIDE_SPEED_KMH = 100.0;
    private int getgodo = 0;
    private int totalDescentMeters = 0;
    private Double elevationBaselineMeters = null;
    private Location gradeBaselineLocation = null;
    private long lastSpeedLocationReceivedAtMillis = 0L;
    private int  total_time=0, rest=0;
    private ArrayList<Double> witch_lat = new ArrayList<>();
    private ArrayList<Double> witch_lon = new ArrayList<>();
    private ArrayList<Double> ele = new ArrayList<>();
    private ArrayList<Float> godoArray = new ArrayList<>();
    private String a;
    //?癲?????볥궙獒??⑤슢堉???
    private String cha_dis = "0", cha_max = "0", cha_avg = "0", adress_value = "";

    private List<Address> addr = null;
    private Geocoder gCoder;

    //???????????癲ル슢??蹂좊쨨??
    long BaseTime;
    final static int Init = 0;
    final static int Run = 1;
    final static int Pause = 2;
    int cur_status = Init;
    long PauseTime;
    private Thread timeThread = null;
    private Boolean isRunning = true;
    String tel2="";
    private Spinner spinner;
    private ArrayList<String> list;
    private String number;
    Intent intent;
    SpeechRecognizer mRecognizer;
    private SmsManager smsManager;
    private String tel="";
    private String gpx_name;
    private String c_name;

    //gpx???援온??
    Gpx parsedGpx = null;
    GPXParser mParser = new GPXParser();
    //?????놁뎽?汝??吏????
    private ServerApi serverApi = Server.getInstance().getApi();
    private Call<List<DangerousArea>> DangerGet;
    private List<DangerousArea> getDanger;

    public StartActivity() {
    }

    // CalloutBalloonAdapter ?癲ル슢?뤸뤃?????볥궙????????닻뇦?
        class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
            private final View mCalloutBalloon;

            public CustomCalloutBalloonAdapter() {
                mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
            }

            @Override
            public View getCalloutBalloon(MapPOIItem poiItem) {
                ((ImageView) mCalloutBalloon.findViewById(R.id.badge)).setImageResource(R.drawable.danger_icon);
                ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
                return mCalloutBalloon;
            }

            @Override
            public View getPressedCalloutBalloon(MapPOIItem poiItem) {
                return null;
            }
        }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRidingStartBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);
        registerRideLocationReceiver();
        binding.ridingSettingsButton.setOnClickListener(v ->
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        );

        SharedPreferences phone = getSharedPreferences("auto", MODE_PRIVATE);
        number = phone.getString("sosphone", "");

        smsManager = SmsManager.getDefault();
        //?????癲ル슢???彛?
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(intent);
        //id refernece for wizet
        spinner = (Spinner) findViewById(R.id.help);

        if ("\uC804\uD654".equals(a)) {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String sms = "" +
                        "\uC704\uAE09\uD569\uB2C8\uB2E4. \uD604\uC7AC \uC704\uCE58: " + latitude + ", " + lonngitude + " \uC8FC\uC18C: " + address_dong + " http://13.209.229.237:8080/app/dangergps/" + latitude + "/" + lonngitude;
                smsManager.sendTextMessage(number, null, sms, null, null);
                Toast.makeText(getApplicationContext(), "\uAE34\uAE09\uBB38\uC790\uB97C \uC804\uC1A1\uD588\uC2B5\uB2C8\uB2E4.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "\uC804\uC1A1\uC5D0 \uC2E4\uD328\uD588\uC2B5\uB2C8\uB2E4.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        //input array data
        list = new ArrayList<>();
        list.add("");
        list.add("119\uC5D0 \uC804\uD654");
        list.add("119\uC5D0 \uC704\uCE58\uC804\uC1A1");
        list.add("\uC9C0\uC815\uBC88\uD638\uC5D0 \uC804\uD654" + "(" + number + ")");
        list.add("\uC9C0\uC815\uBC88\uD638\uC5D0 \uC704\uCE58\uC804\uC1A1" + "(" + number + ")");


        //using ArrayAdapter
        ArrayAdapter spinnerAdapter;
        spinnerAdapter = new ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, list);
        spinner.setAdapter(spinnerAdapter);

        //event listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getApplicationContext(),"????ｋ???????썹땟?㈑??: "+position+spinner.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                tel = "tel:01047527613";
                tel2 = "tel:" + number;
                Log.d("PhoneNumber", tel2);
                switch (position) {
                    case 1:
                        startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                        break;
                    case 2:
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            String sms = "" +
                                    "???????援??????????ㅼ굡?? ??????녿뮝???ル튉?? ??????썹땟???" + latitude + ", " + lonngitude + "????댁삩?????녿뮝????" + address_dong + "????? http://13.209.229.237:8080/app/dangergps/"+latitude+"/"+lonngitude;
                            smsManager.sendTextMessage("010-4752-7613", null, sms, null, null);
                            Toast.makeText(getApplicationContext(), "???궰???亦??湲븐땡筌뚯뼚維???????썹땟戮щ빝?????????", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "????썹땟戮щ빝???????곌숯????????? ????ㅼ굣亦????꾣뤃??嚥싲갭큔?源띿춻??뽯뮋?????꾣뤃???嶺뚮ㅎ????", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }

                        break;
                    case 3:
                        startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel2)));
                        break;
                    case 4:
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            String sms = "" +
                                    "???????援??????????ㅼ굡?? ??????녿뮝???ル튉?? ??????썹땟???" + latitude + ", " + lonngitude + "????댁삩?????녿뮝????" + address_dong + "????? http://13.209.229.237:8080/app/dangergps/"+latitude+"/"+lonngitude+"";
                            smsManager.sendTextMessage(number, null, sms, null, null);
                            Toast.makeText(getApplicationContext(), "???궰???亦??湲븐땡筌뚯뼚維???????썹땟戮щ빝?????????", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "????썹땟戮щ빝???????곌숯????????? ????ㅼ굣亦????꾣뤃??嚥싲갭큔?源띿춻??뽯뮋?????꾣뤃???嶺뚮ㅎ????", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });


        startKakaoV2Map();

        //??ш끽維驪??????산뭐??쀪쑬夷???쎛??????醫딆쓧??癲ル슢???몄쒜嚥▲룗??
        Intent course_get = new Intent(this.getIntent());
        c_name = course_get.getStringExtra("c_name");
        gpx_name = course_get.getStringExtra("gpx");

        if(gpx_name == null && c_name == null){
            binding.flowInfo.setVisibility(View.GONE);
        }else{
            binding.flowInfo.setVisibility(View.VISIBLE);
            binding.couseInfo.setText(c_name);
            Thread uThread = new Thread() {
                @Override
                public void run() {

                    try {
                        MapPolyline polyline = new MapPolyline();
                        polyline.setTag(1000);
                        polyline.setLineColor(Color.argb(255, 0, 74, 196)); // Polyline ??嚥▲꺂????꿔꺂?????
                        //??嶺뚮Ĳ?됭짆????????????꿔꺂??? URL
                        URL url = new URL("http://gyudrive.ipdisk.co.kr:8080/app/getGPX/gpx/"+gpx_name);
                        //Web??????????꿔꺂??? ??醫딆쓧??癲ル슢???몄쒜???ImageView???꿔꺂????癲ル슢怡??뱀떫 Bitmap ?꿔꺂??????筌뚯솃?
                    // Open the GPX file from the server.

                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    // Configure and open the HTTP connection.


                        conn.setDoInput(true); //Server ???嶺?????????怨몄７ ??醫딆쓧??嚥싳쇎紐??????븐뻤??쒖몱???꿔꺂??????
                        conn.connect(); //????쇰뮚???????곗숯???????????(connect() ?癲ル슢???????ㅿ폑?????繹먮냱議????嶺???醫딆쓧??嚥싳쇎紐??


                        InputStream is = conn.getInputStream(); //inputStream ????醫딆쓧??癲ル슢???몄쒜嚥▲룗??
                        // InputStream in = getAssets().open("and.gpx");
                        parsedGpx = mParser.parse(is);

                        if (parsedGpx != null) {
                            // log stuff
                            List<Track> tracks = parsedGpx.getTracks();
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
                            // Polyline ?꿔꺂???????썹땟????????틓楹?
                            if (mMapView != null) {
                                mMapView.addPolyline(polyline);
                            }

                            // ?꿔꺂???????썹땟????嚥싳쉶瑗??꾧틡?嶺?????ㅽ떝椰?? 嚥싳쉶瑗??裕???⑤즾逾???묆궘?Polyline???꿔꺂??袁ㅻ븶?癲?????볥궖?????썹땟怨⒲뀋???됰슦????
                            MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                            int padding = 100; // px
                            if (mMapView != null) {
                                mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
                            }
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
            uThread.start();
        }


        //???????????癲ル슢??蹂좊쨨??
        BaseTime = SystemClock.elapsedRealtime();
        RidingTimer.sendEmptyMessage(0);

        //send_sms = (Button) findViewById(R.id.help);
        cur_status = Run; //????썹땟?????븐뻤??????????????嶺뚮Ĳ?됲걫 ??⑤슢堉???
        binding.mapLayout.setVisibility(View.VISIBLE); //?꿔꺂?????
        binding.speedPre.setVisibility(View.VISIBLE); //?꿔꺂???????꾣뤃????????⑤슢??????鶯????됰Ŧ???
        binding.speedTap.setVisibility(View.GONE); //????戮?덫??
        binding.statusLayout.setVisibility(View.GONE);
        binding.resumeBt.setVisibility(View.GONE);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                // TODO : process tab selection event.
                int pos = tab.getPosition();
                changeView(pos);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                // do nothing
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // do nothing
            }
        });
        startLocationTracking();


        binding.endriding.setOnClickListener(v -> {
                Intent intent = new Intent(StartActivity.this, EndActivity.class);
                intent.putExtra("ingtime", total_time); //?嚥▲굧?????????
                intent.putExtra("distence", (int) hap); //?????룸????좎돦???쎈뼔??
                intent.putExtra("maxspeed", (int) maX); //?꿔꺂????쭍?????戮?덫
                intent.putExtra("avgspeed", (int) avg); //????????戮?덫
                intent.putExtra("getgodo", getgodo); //????蹂λ쑋???쒙쭫??
                intent.putExtra("resttime", rest); //????ㅿ폍筌??????
                intent.putExtra("addr", adress_value);
                intent.putExtra("address_dong", address_dong);
                intent.putExtra("rr_comp", "null");

                try {
                    intent.putExtra(
                            RideRouteCache.EXTRA_CACHE_FILE,
                            RideRouteCache.save(this, witch_lat, witch_lon, ele, godoArray,
                                    maxLat, maxLon, minLat, minLon)
                    );
                } catch (IOException exception) {
                    Log.e("RideEnd", "Unable to prepare route data", exception);
                    Toast.makeText(this, "주행 데이터를 준비하지 못했습니다. 다시 시도해 주세요.", Toast.LENGTH_LONG).show();
                    return;
                }

                Log.d("RideEnd", "time=" + total_time + ", distance=" + hap + ", max=" + maX + ", avg=" + avg + ", high=" + getgodo + ", rest=" + rest + ", addr=" + adress_value);
                stopRideTracking();
                startActivity(intent);
                finish();
        });

            DangerGet = serverApi.getDangerArea();
            DangerGet.enqueue(new Callback<List<DangerousArea>>() {
                @Override
                public void onResponse(Call<List<DangerousArea>> call, Response<List<DangerousArea>> response) {
                    if(response.code()==200) {
                        getDanger= response.body();

                        for (DangerousArea danger : getDanger) {
                            getDalat = danger.getDa_latitude();
                            getDalon = danger.getDa_longitude();
                            getDacon = danger.getDa_content();


                            if (mMapView != null) {
                                mCustomMarker = new MapPOIItem();
                                mCustomMarker.setItemName(getDacon);
                                mCustomMarker.setTag(1);
                                mCustomMarker.setMapPoint(MapPoint.mapPointWithGeoCoord(Double.parseDouble(getDalat), Double.parseDouble(getDalon)));

                                mCustomMarker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

                                mCustomMarker.setCustomImageResourceId(R.drawable.custom_marker_red);
                                mCustomMarker.setCustomImageAutoscale(false);
                                mCustomMarker.setCustomImageAnchor(0.5f, 1.0f);

                                mMapView.addPOIItem(mCustomMarker);
                            }
                        }
                    }
                }

                @Override
                public void onFailure(Call<List<DangerousArea>> call, Throwable t) {
                    t.printStackTrace();
                }
            });

    }

    private void startKakaoV2Map() {
        kakaoV2MapView = binding.mapView;
        binding.mapStatus.setVisibility(View.VISIBLE);
        binding.mapStatus.setText("\uCE74\uCE74\uC624\uB9F5\uC744 \uBD88\uB7EC\uC624\uB294 \uC911\uC785\uB2C8\uB2E4");
        kakaoV2MapView.start(new MapLifeCycleCallback() {
            @Override
            public void onMapDestroy() {
                Log.d("KakaoMapV2", "Map destroyed");
            }

            @Override
            public void onMapError(Exception error) {
                Log.e("KakaoMapV2", "Map error", error);
                runOnUiThread(() -> {
                    binding.mapStatus.setVisibility(View.VISIBLE);
                    binding.mapStatus.setText(
                            "\uC9C0\uB3C4\uB97C \uBD88\uB7EC\uC624\uC9C0 \uBABB\uD588\uC2B5\uB2C8\uB2E4\n"
                                    + "\uCE74\uCE74\uC624 \uC571 \uD0A4\uC640 \uD0A4 \uD574\uC2DC\uB97C \uD655\uC778\uD574\uC8FC\uC138\uC694"
                    );
                });
            }
        }, new KakaoMapReadyCallback() {
            @Override
            public void onMapReady(KakaoMap readyMap) {
                kakaoMap = readyMap;
                kakaoMap.setGestureEnable(GestureType.Pan, true);
                kakaoMap.setGestureEnable(GestureType.Zoom, true);
                kakaoMap.setGestureEnable(GestureType.RotateZoom, true);
                kakaoMap.setGestureEnable(GestureType.OneFingerZoom, true);
                ridingRouteLayer = kakaoMap.getRouteLineManager().getLayer();
                ridingRouteStyles = RouteLineStyles.from(
                        RouteLineStyle.from(12, Color.rgb(30, 116, 52), 3, Color.WHITE)
                );
                currentLocationLayer = kakaoMap.getLabelManager().getLayer();
                currentLocationStyles = kakaoMap.getLabelManager().addLabelStyles(
                        LabelStyles.from(
                                LabelStyle.from(drawableToBitmap(R.drawable.riding_current_marker, 34))
                                        .setAnchorPoint(0.5f, 0.5f)
                        )
                );
                binding.mapStatus.setVisibility(View.GONE);
                Log.d("KakaoMapV2", "Map ready");
            }

            @Override
            public LatLng getPosition() {
                return LatLng.from(35.89617906303501, 128.62171907592318);
            }

            @Override
            public int getZoomLevel() {
                return 15;
            }
        });
    }

    private void updateCurrentPosition(double latitude, double longitude, boolean recordRoute) {
        if (kakaoMap == null) {
            return;
        }

        LatLng currentPosition = LatLng.from(latitude, longitude);
        if (currentLocationLabel == null && currentLocationLayer != null && currentLocationStyles != null) {
            currentLocationLabel = currentLocationLayer.addLabel(
                    LabelOptions.from("current-rider", currentPosition)
                            .setStyles(currentLocationStyles)
                            .setRank(10000)
            );
        } else if (currentLocationLabel != null) {
            currentLocationLabel.moveTo(currentPosition, 350);
        }
        kakaoMap.moveCamera(
                com.kakao.vectormap.camera.CameraUpdateFactory
                        .newCenterPosition(currentPosition, 17)
        );

        if (!recordRoute || ridingRouteLayer == null || ridingRouteStyles == null) {
            Log.d("RideTracking", "current position updated; route recording=" + recordRoute);
            return;
        }

        ridingRoutePoints.add(currentPosition);
        Log.d("RideTracking", "route point recorded; count=" + ridingRoutePoints.size()
                + ", lat=" + latitude + ", lon=" + longitude);
        if (ridingRoutePoints.size() < 2) {
            return;
        }

        RouteLineSegment segment = RouteLineSegment.from(
                new ArrayList<>(ridingRoutePoints),
                ridingRouteStyles
        );
        if (ridingRouteLine == null) {
            RouteLineStylesSet stylesSet = RouteLineStylesSet.from(ridingRouteStyles);
            RouteLineOptions options = RouteLineOptions.from(segment).setStylesSet(stylesSet);
            ridingRouteLine = ridingRouteLayer.addRouteLine(options);
        } else {
            ridingRouteLine.changeSegments(segment);
        }
    }

    private Bitmap drawableToBitmap(int drawableId, int sizeDp) {
        int sizePx = Math.round(sizeDp * getResources().getDisplayMetrics().density);
        Bitmap bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        Drawable drawable = AppCompatResources.getDrawable(this, drawableId);
        if (drawable != null) {
            drawable.setBounds(0, 0, sizePx, sizePx);
            drawable.draw(canvas);
        }
        return bitmap;
    }
                private void changeView(int index) {
                    switch (index) {
                        case 0:
                            binding.mapLayout.setVisibility(View.VISIBLE);
                            binding.speedPre.setVisibility(View.VISIBLE);
                            binding.speedTap.setVisibility(View.GONE);
                            binding.defaultLayout.setBackgroundColor(0xFFF7F8F7);
                            break;
                        case 1:
                            binding.mapLayout.setVisibility(View.GONE);
                            binding.speedPre.setVisibility(View.GONE);
                            binding.speedTap.setVisibility(View.VISIBLE);
                            binding.defaultLayout.setBackgroundColor(0xFFF7F8F7);
                            break;
                    }
                }

                private void startLocationTracking() {
                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                            != PackageManager.PERMISSION_GRANTED) {
                        binding.gpsStatus.setText("GPS 권한 필요");
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                LOCATION_PERMISSION_REQUEST
                        );
                        return;
                    }

                    RideTrackingService.start(this);
                    locationUpdatesStarted = true;
                    requestNotificationPermissionIfNeeded();
                }

                private void registerRideLocationReceiver() {
                    if (rideLocationReceiverRegistered) {
                        return;
                    }
                    IntentFilter filter = new IntentFilter(RideTrackingService.ACTION_LOCATION_UPDATE);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                        registerReceiver(rideLocationReceiver, filter, Context.RECEIVER_NOT_EXPORTED);
                    } else {
                        registerReceiver(rideLocationReceiver, filter);
                    }
                    rideLocationReceiverRegistered = true;
                }

                private void requestNotificationPermissionIfNeeded() {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                            && ActivityCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                            != PackageManager.PERMISSION_GRANTED) {
                        ActivityCompat.requestPermissions(
                                this,
                                new String[]{Manifest.permission.POST_NOTIFICATIONS},
                                NOTIFICATION_PERMISSION_REQUEST
                        );
                    }
                }

                private void stopRideTracking() {
                    RideTrackingService.stop(this);
                    locationUpdatesStarted = false;
                }

                @Override
                public void onRequestPermissionsResult(
                        int requestCode,
                        @NonNull String[] permissions,
                        @NonNull int[] grantResults
                ) {
                    super.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    if (requestCode == LOCATION_PERMISSION_REQUEST
                            && grantResults.length > 0
                            && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        startLocationTracking();
                    } else if (requestCode == LOCATION_PERMISSION_REQUEST) {
                        binding.gpsStatus.setText("GPS 권한 필요");
                        binding.mapStatus.setVisibility(View.VISIBLE);
                        binding.mapStatus.setText("현재 위치 기록을 위해 위치 권한이 필요합니다");
                    }
                }

                //???? ?癲? ?筌?????

                public void myOnClick(View v) {
                    Log.d("RideStatus", String.valueOf(cur_status));

                    switch (v.getId()) {
                        case R.id.pausebt: //?癲??筌???????嶺???


                        case R.id.resume_bt:
                            //??????嶺???


                            switch (cur_status) {
                                case Init:
                                    BaseTime = SystemClock.elapsedRealtime();
                                    cur_status = Run; //????썹땟?????븐뻤??????????????嶺뚮Ĳ?됲걫 ??⑤슢堉???
                                    Log.d("cur_status", "1");

                                    break;

                                case Run:
                                    PauseTime = SystemClock.elapsedRealtime();
                                    RidingTimer.removeMessages(0); //??濚밸Ŧ?????꾩룆梨띰쭛??

                                    RideTrackingService.pauseAssistant(this);

                                    cur_status = Pause;
                                    binding.resumeBt.setVisibility(View.VISIBLE);
                                    binding.pausebt.setVisibility(View.GONE);
                                    Log.d("cur_status", "?????덊떀");
                                    break;
                                case Pause:
                                    long now2 = SystemClock.elapsedRealtime();
                                    rest += (int) ((now2 - PauseTime) / 1000L);
                                    binding.resttime.setText(formatDuration(rest));
                                    RidingTimer.sendEmptyMessage(0);
                                    BaseTime += (now2 - PauseTime);
                                    RideTrackingService.resumeAssistant(this);
                                    cur_status = Run;
                                    binding.resumeBt.setVisibility(View.GONE);
                                    binding.pausebt.setVisibility(View.VISIBLE);
                                    Log.d("cur_status", "嚥싳쉶瑗??꾧틡?");
                                    break;
                            }
                            break;
                    }
                }

                Handler RidingTimer = new Handler() {
                    public void handleMessage(Message msg) {
                        binding.ingtime.setText(getTimeout());
                        binding.mTime.setText(getTimeout());
                        total_time = getTimeout2();

                        //sendEmptyMessage??????猷뱀쟼?????됲닓 ?꿔꺂???熬곊삳튉??? ?癲ル슢??蹂좊쨨????怨뺤췅??????썹땟戮щ빝??
                        RidingTimer.sendEmptyMessageDelayed(0, 1000L);
                    }


                    //????썹땟??????????影??낟??筌뤾퍔커???????Β?ы닍????β뼯爰귨㎘??꿔꺂??????
                    int getTimeout2() {
                        long now = SystemClock.elapsedRealtime(); //????ｋ????????ㅼ젾????????덊떀???野껊뿈???嶺뚮슣堉?쭕????繹먮냱議???嚥▲굧?????????????)^^;
                        long outTime = now - BaseTime;

                        int easy_outTime2 = (int) outTime / 1000;
                        // Log.d("?汝??吏??, String.valueOf(easy_outTime2));
                        return easy_outTime2;
                    }

                    //????썹땟??????????影??낟??筌뤾퍔커???????Β?ы닍????β뼯爰귨㎘??꿔꺂??????
                    String getTimeout() {
                        long now = SystemClock.elapsedRealtime(); //????ｋ????????ㅼ젾????????덊떀???野껊뿈???嶺뚮슣堉?쭕????繹먮냱議???嚥▲굧?????????????)^^;
                        long outTime = now - BaseTime;

                        int sec = (int) ((outTime / 1000) % 60);
                        int min = (int) ((outTime / 1000 / 60) % 60);
                        int hour = (int) ((outTime / 1000) / 3600);


                        String easy_outTime = String.format("%02d:%02d:%02d", hour, min, sec);
                        //Log.d("?汝??吏??, String.valueOf(outTime / 1000));
                        return easy_outTime;
                    }
                };


                private String formatDuration(int seconds) {
                    int hours = seconds / 3600;
                    int minutes = (seconds % 3600) / 60;
                    int secs = seconds % 60;
                    return String.format(Locale.KOREA, "%02d:%02d:%02d", hours, minutes, secs);
                }

                @SuppressLint("HandlerLeak")
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int mSec = msg.arg1 % 100;
                        int sec = (msg.arg1 / 100) % 60;
                        int min = (msg.arg1 / 100) / 60;
                        int hour = (msg.arg1 / 100) / 360;
                        //1000??1??1000*60 ?? 1??1000*60*10?? 10??1000*60*60?? ??嶺?筌??

                        @SuppressLint("DefaultLocale") String result = String.format("%02d:%02d:%02d", hour, min, sec);

                        rest = msg.arg1 / 100;
                        binding.resttime.setText(result);
                    }
                };

                public class timeThread implements Runnable {
                    @Override
                    public void run() {
                        int i = 0;

                        while (true) {
                            while (isRunning) { //??濚밸Ŧ援앾쭛?癲???????썼キ?????꿔꺂?????
                                Message msg = new Message();
                                msg.arg1 = i++;
                                handler.sendMessage(msg);

                                try {
                                    Thread.sleep(10);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                    runOnUiThread(() -> {
                                        binding.resttime.setText("");
                                        binding.resttime.setText("00:00:00");
                                    });
                                    return; // ?癲ル슢?뤸뤃???????熬곣뫖利????嚥▲굧????return
                                }
                            }
                        }
                    }
                }

                @Override
                public void onMapViewInitialized(MapView mapView) {

                }


                @Override
                public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onMapViewZoomLevelChanged(MapView mapView, int i) {

                }

                @Override
                public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

                }

                @Override
                public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

                }

                @Override
                public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {
                    Toast.makeText(this, "Clicked " + mapPOIItem.getItemName() + " Callout Balloon", Toast.LENGTH_SHORT).show();
                }

                @Override
                public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

                }

                @Override
                public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {
                }


                @Override
                public void onLocationChanged(Location location) {
                    latitude = location.getLatitude();
                    lonngitude = location.getLongitude();
                    binding.gpsStatus.setText("GPS 연결됨");
                    boolean shouldRecord = cur_status == Run;
                    updateCurrentPosition(latitude, lonngitude, shouldRecord);

                    if (!shouldRecord) {
                        mLastlocation = null;
                        elevationBaselineMeters = null;
                        gradeBaselineLocation = null;
                        lastSpeedLocationReceivedAtMillis = 0L;
                        return;
                    }

                    // ????썹땟?嶺??嚥▲굧??嶸???癲ル슢???ъ쒜??熬곣뫖利?影?뉖뜦?????
                    witch_lat.add(latitude);
                    witch_lon.add(lonngitude);
                    godoArray.add((float) location.getAltitude());

                    // ?꿔꺂????쭍? ?꿔꺂????쭍??????썹땟?嶺??嚥▲굧??嶸????影??낟??
                    if (maxLat < latitude) maxLat = latitude;
                    if (minLat > latitude) minLat = latitude;
                    if (maxLon < lonngitude) maxLon = lonngitude;
                    if (minLon > lonngitude) minLon = lonngitude;

                    // ???쒙쭫???癲ル슢???ъ쒜?????
                    ele.add(location.getAltitude());
                    if (location.hasAltitude()) {
                        updateElevationStats(location);
                    }
                    updateGpsQuality(location);

                    //????썹땟?????戮?덫
                    getSpeed = resolveSpeedKmh(location);
                    binding.nowspeed.setText(String.format("%.1f", getSpeed));  //Get Speed
                    binding.mSpeed.setText(String.format("%.1f", getSpeed) + "km/h");
                    binding.speedGauge.setSpeedKmh((float) getSpeed);

                    MapPolyline polyline = new MapPolyline();
                    polyline.setLineColor(Color.argb(128, 255, 51, 0));

                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(latitude, lonngitude));
                    if (mMapView != null) {
                        mMapView.addPolyline(polyline);
                        mMapView.isShowingCurrentLocationMarker();
                    }

                    // ?꿔꺂???????썹땟????嚥싳쉶瑗??꾧틡?嶺?????ㅽ떝椰?? 嚥싳쉶瑗??裕???⑤즾逾???묆궘?Polyline???꿔꺂??袁ㅻ븶?癲?????볥궖?????썹땟怨⒲뀋???됰슦????
                    MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                    int padding = 50; // px
                    if (mMapView != null) {
                        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding, 2, 3));
                    }

                    gCoder = new Geocoder(getApplicationContext(), Locale.getDefault());
                    try {
                        addr = gCoder.getFromLocation(latitude, lonngitude, 1);
                        List<Address> addr2 = gCoder.getFromLocation(latitude, lonngitude, 2);
                        List<Address> addr3 = gCoder.getFromLocation(latitude, lonngitude, 3);

                        Address a = addr.get(0);
                        address_dong = a.getThoroughfare();
                        adress_value = a.getAddressLine(0);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    if (mLastlocation != null) {
                        /*??????濚밸Ŧ??????녾컯嶺????틓楹?*/
                        MapPolyline polyline2 = new MapPolyline();
                        polyline.setLineColor(Color.argb(128, 255, 51, 0));
                        polyline.addPoint(MapPoint.mapPointWithGeoCoord(mLastlocation.getLatitude(), mLastlocation.getLongitude()));
                        if (mMapView != null) {
                            mMapView.addPolyline(polyline);
                        }

                        // ?꿔꺂???????썹땟????嚥싳쉶瑗??꾧틡?嶺?????ㅽ떝椰?? 嚥싳쉶瑗??裕???⑤즾逾???묆궘?Polyline???꿔꺂??袁ㅻ븶?癲?????볥궖?????썹땟怨⒲뀋???됰슦????
                        MapPointBounds mapPointBounds2 = new MapPointBounds(polyline.getMapPoints());
                        if (mMapView != null) {
                            mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds2, padding, 2, 3));
                        }

                        if (getSpeed > maX) {
                            maX = getSpeed;
                            binding.maxspeed.setText(String.format(Locale.KOREA, "%.1f km/h", maX));
                            cha_max = String.format("%.0f", maX);
                        }
                        //?????룸????좎돦???쎈뼔??
                        hap = hap + mLastlocation.distanceTo(location);
                        cha_dis = String.format("%.0f", hap);

                        binding.dis.setText(String.format("%.1f m", hap));
                        binding.mDistance.setText(String.format("%.1f", hap) + "m");

                        double killlo = hap / 1000.0;
                        if (hap >= 1000) {
                            binding.dis.setText(String.format("%.1f km", killlo));
                            binding.mDistance.setText(String.format("%.1f", killlo) + "km");
                        }
                        binding.calories.setText(String.format(Locale.KOREA, "%d kcal", Math.round(killlo * 50.8)));

                        //????????戮?덫
                        avg = total_time > 0 ? (hap / total_time) * 3.6 : 0;
                        binding.avgspeed.setText(String.format(Locale.KOREA, "%.1f km/h", avg));
                        cha_avg = String.format(Locale.KOREA, "%.0f", avg);

                        updateCurrentGrade(location);

                    }
                    // ????썹땟?????썹땟????꿔꺂?????????썹땟?熬?????⑤슢堉???
                    mLastlocation = location;
                }

                private void updateElevationStats(Location location) {
                    double altitude = location.getAltitude();
                    binding.Nowgodo.setText(String.format(Locale.KOREA, "%.0f m", altitude));

                    if (elevationBaselineMeters == null) {
                        elevationBaselineMeters = altitude;
                        return;
                    }

                    double elevationChange = altitude - elevationBaselineMeters;
                    if (elevationChange >= MIN_ELEVATION_GAIN_METERS) {
                        getgodo += Math.round(elevationChange);
                        elevationBaselineMeters = altitude;
                        binding.getgodo.setText(String.format(Locale.KOREA, "%d m", getgodo));
                    } else if (elevationChange <= -MIN_ELEVATION_GAIN_METERS) {
                        totalDescentMeters += Math.round(Math.abs(elevationChange));
                        elevationBaselineMeters = altitude;
                        binding.totalDescent.setText(String.format(Locale.KOREA, "%d m", totalDescentMeters));
                    }
                }

                private void updateCurrentGrade(Location location) {
                    if (!location.hasAltitude()) {
                        binding.currentGrade.setText("-- %");
                        return;
                    }

                    if (gradeBaselineLocation == null) {
                        gradeBaselineLocation = new Location(location);
                        return;
                    }

                    float horizontalDistance = gradeBaselineLocation.distanceTo(location);
                    if (horizontalDistance < 10f) return;

                    double grade = ((location.getAltitude() - gradeBaselineLocation.getAltitude())
                            / horizontalDistance) * 100.0;
                    gradeBaselineLocation = new Location(location);
                    // Values outside this range are overwhelmingly likely to be a GPS altitude jump.
                    if (Math.abs(grade) > 35.0) {
                        binding.currentGrade.setText("-- %");
                        return;
                    }
                    binding.currentGrade.setText(String.format(Locale.KOREA, "%+.1f %%", grade));
                }

                private void updateGpsQuality(Location location) {
                    if (location.hasAccuracy()) {
                        binding.gpsAccuracy.setText(String.format(Locale.KOREA, "±%.0f m", location.getAccuracy()));
                    } else {
                        binding.gpsAccuracy.setText("확인 중");
                    }
                }

                private double resolveSpeedKmh(Location location) {
                    long receivedAtMillis = SystemClock.elapsedRealtime();
                    double reportedSpeedKmh = location.getSpeed() * 3.6;
                    if (location.hasSpeed() && reportedSpeedKmh > 0.1) {
                        lastSpeedLocationReceivedAtMillis = receivedAtMillis;
                        return reportedSpeedKmh;
                    }

                    if (mLastlocation == null) {
                        lastSpeedLocationReceivedAtMillis = receivedAtMillis;
                        return 0.0;
                    }

                    long elapsedMillis = location.getElapsedRealtimeNanos() > mLastlocation.getElapsedRealtimeNanos()
                            ? (location.getElapsedRealtimeNanos() - mLastlocation.getElapsedRealtimeNanos()) / 1_000_000L
                            : location.getTime() - mLastlocation.getTime();
                    if (elapsedMillis <= 0L && lastSpeedLocationReceivedAtMillis > 0L) {
                        elapsedMillis = receivedAtMillis - lastSpeedLocationReceivedAtMillis;
                    }
                    lastSpeedLocationReceivedAtMillis = receivedAtMillis;
                    if (elapsedMillis < 250L || elapsedMillis > 15_000L) return 0.0;

                    double inferredSpeedKmh = mLastlocation.distanceTo(location) * 3600.0 / elapsedMillis;
                    return inferredSpeedKmh <= MAX_REASONABLE_RIDE_SPEED_KMH ? inferredSpeedKmh : 0.0;
                }


                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                    binding.gpsStatus.setText("GPS 연결 중");
                // ????썹땟??癲ル슢???ъ쒜??????욍걛???ш끽維??
                    if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    RideTrackingService.start(this);
                }

                @Override
                public void onProviderDisabled(String provider) {
                    binding.gpsStatus.setText("GPS 꺼짐");
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

                @Override
                public void onBackPressed() {
                    new AlertDialog.Builder(this)
                            .setTitle("주행을 종료할까요?")
                            .setMessage("저장하지 않은 주행 기록은 사라집니다.")
                            .setNegativeButton("계속 주행", null)
                            .setNeutralButton("기록 없이 종료", (dialog, which) -> {
                                stopRideTracking();
                                finish();
                            })
                            .setPositiveButton("종료 후 저장", (dialog, which) -> binding.endriding.performClick())
                            .show();
                }

                @Override
                protected void onDestroy() {
                    if (isFinishing()) {
                        stopRideTracking();
                    }
                    if (locationManager != null && locationUpdatesStarted) {
                        locationManager.removeUpdates(this);
                        locationUpdatesStarted = false;
                    }
                    if (rideLocationReceiverRegistered) {
                        unregisterReceiver(rideLocationReceiver);
                        rideLocationReceiverRegistered = false;
                    }
                    RidingTimer.removeCallbacksAndMessages(null);
                    if (kakaoV2MapView != null) {
                        kakaoV2MapView.finish();
                        kakaoV2MapView = null;
                    }
                    kakaoMap = null;
                    ridingRouteLine = null;
                    ridingRouteLayer = null;
                    currentLocationLabel = null;
                    currentLocationLayer = null;
                    super.onDestroy();
                    Log.d("onDestroy", "onDestroy");

                }

                @Override
                protected void onPause() {
                    super.onPause();
                    if (kakaoV2MapView != null) {
                        kakaoV2MapView.pause();
                    }
                    Log.d("onPause", "onPause");
                }

                @Override
                protected void onResume() {
                    super.onResume();
                    if (kakaoV2MapView != null) {
                        kakaoV2MapView.resume();
                    }
                    Log.d("onResume", "OnResume");
                }

                @Override
                protected void onStart() {
                    super.onStart();
                }

                @Override
                protected void onStop() {
                    super.onStop();
                    Log.d("SpeechRecognizer", "stopped");
                }

                private RecognitionListener listener = new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        //Toast.makeText(getApplicationContext(), "?????癲ル슢???彛????嶺뚮??ｆ뤃??嶺뚮ㅎ????", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onBeginningOfSpeech() {
                    }

                    @Override
                    public void onRmsChanged(float rmsdB) {
                    }

                    @Override
                    public void onBufferReceived(byte[] buffer) {
                    }

                    @Override
                    public void onEndOfSpeech() {
                    }

                    @Override
                    public void onError(int error) {
                        String message;

                        switch (error) {
                            case SpeechRecognizer.ERROR_AUDIO:
                                message = "Audio error";
                                break;
                            case SpeechRecognizer.ERROR_CLIENT:
                                message = "Client error";
                                break;
                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                message = "Permission error";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK:
                                message = "Network error";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                message = "Network timeout";
                                break;
                            case SpeechRecognizer.ERROR_NO_MATCH:
                                message = "No match";
                                break;
                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                message = "Recognizer busy";
                                break;
                            case SpeechRecognizer.ERROR_SERVER:
                                message = "Server error";
                                break;
                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                message = "Speech timeout";
                                break;
                            default:
                                message = "Unknown error";
                                break;
                        }

                       // Toast.makeText(getApplicationContext(), "????嚥?夷???쎛 ?熬곣뫖利든뜏類ｋ렱?????????? : " + message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResults(Bundle results) {
                        // ?꿔꺂????????????ArrayList????潁????鶯ㅺ동????궰?textView????潁???????ㅿ폎?묐쵈?딂린?곌땀????
                        ArrayList<String> matches =
                                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                        for (int i = 0; i < matches.size(); i++) {
                            a = matches.get(i);
                            Log.d("a????????", a);
                        }
                        switch (a) {
                            case "\uC704\uCE58\uC804\uC1A1":
                                String sms = "" +
                                        "\uC704\uAE09\uD569\uB2C8\uB2E4. \uD604\uC7AC \uC704\uCE58: " + latitude + ", " + lonngitude + " \uC8FC\uC18C: " + address_dong + " http://gyudrive.ipdisk.co.kr:8080/app/dangergps/35.896599/128.621009";
                                smsManager.sendTextMessage(number, null, sms, null, null);
                                Toast.makeText(getApplicationContext(), "\uAE34\uAE09\uBB38\uC790\uB97C \uC804\uC1A1\uD588\uC2B5\uB2C8\uB2E4.", Toast.LENGTH_LONG).show();
                                break;
                            case "119\uC5D0 \uC804\uD654":
                                startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                                break;
                            case "\uC9C0\uC815\uBC88\uD638\uC5D0 \uC704\uCE58\uC804\uC1A1":
                                String sms2 = "" +
                                        "\uC704\uAE09\uD569\uB2C8\uB2E4. \uD604\uC7AC \uC704\uCE58: " + latitude + ", " + lonngitude + " \uC8FC\uC18C: " + address_dong + " http://gyudrive.ipdisk.co.kr:8080/app/dangergps/35.896599/128.621009";
                                smsManager.sendTextMessage(number, null, sms2, null, null);
                                break;
                            case "\uC9C0\uC815\uBC88\uD638\uC5D0 \uC804\uD654":
                                startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel2)));
                                break;
                            default:
                                break;
                        }

                    }

                    @Override
                    public void onPartialResults(Bundle partialResults) {
                    }

                    @Override
                    public void onEvent(int eventType, Bundle params) {
                    }
                };
            }
