package com.example.testapplication.ui.riding;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.SystemClock;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import com.example.testapplication.R;
import com.example.testapplication.SoundManager;
import com.example.testapplication.databinding.ActivityRidingStartBinding;
import com.example.testapplication.dto.DangerousArea;
import com.example.testapplication.gpx.GPXParser;
import com.example.testapplication.gpx.Gpx;
import com.example.testapplication.gpx.Track;
import com.example.testapplication.gpx.TrackPoint;
import com.example.testapplication.gpx.TrackSegment;
import com.example.testapplication.net.Server;
import com.example.testapplication.net.ServerApi;
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
        implements LocationListener, MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener, MapView.MapViewEventListener, MapView.POIItemEventListener, TextToSpeech.OnInitListener {
    private SoundPool soundPool;
    private SoundManager soundManager;
    private boolean play;
    private ActivityRidingStartBinding binding;
    private MapView mMapView;
    private MapPOIItem mCustomMarker;
    private LocationManager locationManager;
    private Location mLastlocation = null;
    private TextToSpeech textToSpeech;
    private String value;
    private float distance;
    private int cnt=0;

    private String getDalat, getDalon, getDacon, address_dong;
    //각종 변수
    private double latitude, lonngitude, getSpeed = 0, hap = 0, avg = 0, maX = 0, maxLat = 0, maxLon = 0, minLat = 1000, minLon = 1000;
    private int getgodo, getgodoval = 0;
    private int  total_time=0, rest=0;
    private ArrayList<Double> witch_lat = new ArrayList<>();
    private ArrayList<Double> witch_lon = new ArrayList<>();
    private ArrayList<Double> ele = new ArrayList<>();
    private ArrayList<Float> godoArray = new ArrayList<>();
    private String a;
    //형변환용변수
    private String cha_dis = "0", cha_max = "0", cha_avg = "0", adress_value = "";

    private List<Address> addr = null;
    private Geocoder gCoder;

    //이동시간 핸들러
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

    //gpx관련
    Gpx parsedGpx = null;
    GPXParser mParser = new GPXParser();
    //레트로핏
    private ServerApi serverApi = Server.getInstance().getApi();
    private Call<List<DangerousArea>> DangerGet;
    private List<DangerousArea> getDanger;

    public StartActivity() {
    }

    // CalloutBalloonAdapter 인터페이스 구현
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

        SharedPreferences phone = getSharedPreferences("auto", MODE_PRIVATE);
        number = phone.getString("sosphone", "");

        smsManager = SmsManager.getDefault();
        //음성인식
        intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(RecognizerIntent.EXTRA_CALLING_PACKAGE, getPackageName());
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "ko-KR");

        mRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        mRecognizer.setRecognitionListener(listener);
        mRecognizer.startListening(intent);
        //id refernece for wizet
        spinner = (Spinner) findViewById(R.id.help);

        if (a == "전화") {
            try {
                SmsManager smsManager = SmsManager.getDefault();
                String sms = "" +
                        "산에서 다쳤어요! 도와주세요! 제 위치는 " + latitude + ", " + lonngitude + "이고 주소는 " + address_dong + "에요. http://13.209.229.237:8080/app/dangergps/+"+latitude+"/"+lonngitude;
                smsManager.sendTextMessage(number, null, sms, null, null);
                Toast.makeText(getApplicationContext(), "긴급문자를 전송하였습니다.", Toast.LENGTH_LONG).show();
            } catch (Exception e) {
                Toast.makeText(getApplicationContext(), "전송에 실패하였습니다. 내장전화앱으로 전환합니다.", Toast.LENGTH_LONG).show();
                e.printStackTrace();
            }
        }

        //input array data
        list = new ArrayList<>();
        list.add("");
        list.add("119에 전화");
        list.add("119에 위치전송");
        list.add("지정번호에 전화" + "(" + number + ")");
        list.add("지정번호에 위치전송" + "(" + number + ")");


        //using ArrayAdapter
        ArrayAdapter spinnerAdapter;
        spinnerAdapter = new ArrayAdapter(this, R.layout.support_simple_spinner_dropdown_item, list);
        spinner.setAdapter(spinnerAdapter);

        //event listener
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                // Toast.makeText(getApplicationContext(),"선택된 아이템 : "+position+spinner.getItemAtPosition(position),Toast.LENGTH_SHORT).show();
                tel = "tel:01047527613";
                tel2 = "tel:" + number;
                Log.d("전화번호", tel2);
                switch (position) {
                    case 1:
                        startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                        break;
                    case 2:
                        try {
                            SmsManager smsManager = SmsManager.getDefault();
                            String sms = "" +
                                    "산에서 다쳤어요! 도와주세요! 제 위치는 " + latitude + ", " + lonngitude + "이고 주소는 " + address_dong + "에요. http://13.209.229.237:8080/app/dangergps/"+latitude+"/"+lonngitude;
                            smsManager.sendTextMessage("010-4752-7613", null, sms, null, null);
                            Toast.makeText(getApplicationContext(), "긴급문자를 전송하였습니다.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "전송에 실패하였습니다. 내장전화앱으로 전환합니다.", Toast.LENGTH_LONG).show();
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
                                    "산에서 다쳤어요! 도와주세요! 제 위치는 " + latitude + ", " + lonngitude + "이고 주소는 " + address_dong + "에요. http://13.209.229.237:8080/app/dangergps/"+latitude+"/"+lonngitude+"";
                            smsManager.sendTextMessage(number, null, sms, null, null);
                            Toast.makeText(getApplicationContext(), "긴급문자를 전송하였습니다.", Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "전송에 실패하였습니다. 내장전화앱으로 전환합니다.", Toast.LENGTH_LONG).show();
                            e.printStackTrace();
                        }
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        soundPool = new SoundPool.Builder().build();
        soundManager = new SoundManager(this, soundPool);
        soundManager.addSound(0, R.raw.sos);

        //긴급알림
        Button sns_sound = (Button) findViewById(R.id.sos_sound);
        sns_sound.setOnClickListener(v -> {
            if (!play) {
                play = true;
            } else {
                soundManager.playSound(0);
                play = false;
            }
        });

        timeThread = new Thread(new timeThread());
        timeThread.start();

        isRunning = !isRunning;
        mMapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mMapView);
        mMapView.setCurrentLocationEventListener(this);
        mMapView.setShowCurrentLocationMarker(true);
        mMapView.setMapViewEventListener(this);
        mMapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading);

        // 구현한 CalloutBalloonAdapter 등록
        mMapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());

        //코스 따라가기 값 가져오기
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
                        polyline.setLineColor(Color.argb(255, 0, 74, 196)); // Polyline 컬러 지정.
                        //서버에 올려둔 이미지 URL
                        URL url = new URL("http://13.209.229.237:8080/app/getGPX/gpx/"+gpx_name);
                        //Web에서 이미지 가져온 후 ImageView에 지정할 Bitmap 만들기
                    /* URLConnection 생성자가 protected로 선언되어 있으므로
                     개발자가 직접 HttpURLConnection 객체 생성 불가 */
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                    /* openConnection()메서드가 리턴하는 urlConnection 객체는
                    HttpURLConnection의 인스턴스가 될 수 있으므로 캐스팅해서 사용한다*/

                        conn.setDoInput(true); //Server 통신에서 입력 가능한 상태로 만듦
                        conn.connect(); //연결된 곳에 접속할 때 (connect() 호출해야 실제 통신 가능함)


                        InputStream is = conn.getInputStream(); //inputStream 값 가져오기
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
                            // Polyline 지도에 올리기.
                            mMapView.addPolyline(polyline);

                            // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
                            MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                            int padding = 100; // px
                            mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));
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


        //이동시간 핸들러
        BaseTime = SystemClock.elapsedRealtime();
        RidingTimer.sendEmptyMessage(0);

        //send_sms = (Button) findViewById(R.id.help);
        cur_status = Run; //현재상태를 런상태로 변경
        binding.mapLayout.setVisibility(View.VISIBLE); //지도
        binding.speedPre.setVisibility(View.VISIBLE); //지도탭에서 보여지는거
        binding.speedTap.setVisibility(View.GONE); //속도계
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


        binding.endriding.setOnClickListener(v -> {
            //형변환한거
            if (hap == 0) {
                /*이동거리가 0이면 라이딩 기록 종료시키면 액티비티 종료 */
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(this);
                alert_confirm.setMessage("수집 데이터가 너무 적어 저장하지 않습니다");
                alert_confirm.setPositiveButton("예", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                alert_confirm.setNegativeButton("아니오", null);
                AlertDialog alert = alert_confirm.create();
                alert.setTitle("라이딩 기록 실패");
                alert.show();

            } else {

                Intent intent = new Intent(StartActivity.this, EndActivity.class);
                intent.putExtra("ingtime", total_time); //경과시간
                intent.putExtra("distence", (int) hap); //이동거리
                intent.putExtra("maxspeed", (int) maX); //최대속도
                intent.putExtra("avgspeed", (int) avg); //평균속도
                intent.putExtra("getgodo", getgodo); //획득고도
                intent.putExtra("resttime", rest); //휴식시간
                intent.putExtra("addr", adress_value);
                intent.putExtra("witch_lat", witch_lat); //위도
                intent.putExtra("witch_lon", witch_lon); //경도
                intent.putExtra("address_dong", address_dong);
                intent.putExtra("godoarray", godoArray);
                intent.putExtra("ele", ele); //고도
                intent.putExtra("maxLat", maxLat); //최대위도
                intent.putExtra("minLat", minLat); //최소위도
                intent.putExtra("maxLon", maxLon); //최대경도
                intent.putExtra("minLon", minLon); //최소경도
                intent.putExtra("wido", witch_lat); //위도(지도보여줄거
                intent.putExtra("kyun", witch_lon); //경도(지도보여줄거)
                intent.putExtra("rr_comp", "null");

                Log.d("로그랑", total_time + " " + hap + " " + maX + " " + avg + " " + getgodo + " " + rest + " " + adress_value + " ");
                startActivity(intent);
                finish();
                mapViewContainer.removeAllViews();
                locationManager.removeUpdates(this);
            }
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


                            mCustomMarker = new MapPOIItem();
                            String name = "Custom Marker";
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

                @Override
                public void onFailure(Call<List<DangerousArea>> call, Throwable t) {
                    t.printStackTrace();
                }
            });

    }
                private void changeView(int index) {
                    switch (index) {
                        case 0:
                            binding.mapLayout.setVisibility(View.VISIBLE);
                            binding.speedPre.setVisibility(View.VISIBLE);
                            binding.speedTap.setVisibility(View.GONE);
                            binding.defaultLayout.setBackgroundColor(0xdddddd);
                            break;
                        case 1:
                            binding.mapLayout.setVisibility(View.GONE);
                            binding.speedPre.setVisibility(View.GONE);
                            binding.speedTap.setVisibility(View.VISIBLE);
                            binding.defaultLayout.setBackgroundColor(0xf0f0f0);
                            break;
                    }

                    locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
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
                    Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                    if (lastKnownLocation != null) {
                    }
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
                }

                //재개, 정지 버튼

                public void myOnClick(View v) {


                    Log.d("현재상태", String.valueOf(cur_status));

                    switch (v.getId()) {
                        case R.id.pausebt: //정지버튼 활성


                        case R.id.resume_bt:
                            //재개 활성


                            switch (cur_status) {
                                case Init:
                                    BaseTime = SystemClock.elapsedRealtime();
                                    cur_status = Run; //현재상태를 런상태로 변경
                                    Log.d("cur_status", "1");

                                    break;

                                case Run:
                                    PauseTime = SystemClock.elapsedRealtime();
                                    RidingTimer.removeMessages(0); //라이딩시간

                                    isRunning = !isRunning;

                                    cur_status = Pause;
                                    binding.resumeBt.setVisibility(View.VISIBLE);
                                    binding.pausebt.setVisibility(View.GONE);
                                    Log.d("cur_status", "실행");
                                    break;
                                case Pause:
                                    long now2 = SystemClock.elapsedRealtime();
                                    RidingTimer.sendEmptyMessage(0);
                                    BaseTime += (now2 - PauseTime);
                                    cur_status = Run;
                                    binding.resumeBt.setVisibility(View.GONE);
                                    binding.pausebt.setVisibility(View.VISIBLE);
                                    Log.d("cur_status", "중지");
                                    isRunning = false;
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

                        //sendEmptyMessage는 비어있는 메세제를 핸들러에게 전송함
                        RidingTimer.sendEmptyMessage(0);
                    }


                    //현재시간을 계속구해서 출력하는 메소드
                    int getTimeout2() {
                        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간(??)^^;
                        long outTime = now - BaseTime;

                        int easy_outTime2 = (int) outTime / 1000;
                        // Log.d("로그", String.valueOf(easy_outTime2));
                        return easy_outTime2;
                    }

                    //현재시간을 계속구해서 출력하는 메소드
                    String getTimeout() {
                        long now = SystemClock.elapsedRealtime(); //애플리케이션이 실행되고나서 실제로 경과된 시간(??)^^;
                        long outTime = now - BaseTime;

                        int sec = (int) ((outTime / 1000) % 60);
                        int min = (int) ((outTime / 1000) / 60);
                        int hour = (int) ((outTime / 1000) / 3600);


                        String easy_outTime = String.format("%02d:%02d:%02d", hour, min, sec);
                        //Log.d("로그", String.valueOf(outTime / 1000));
                        return easy_outTime;
                    }
                };


                @SuppressLint("HandlerLeak")
                Handler handler = new Handler() {
                    @Override
                    public void handleMessage(Message msg) {
                        int mSec = msg.arg1 % 100;
                        int sec = (msg.arg1 / 100) % 60;
                        int min = (msg.arg1 / 100) / 60;
                        int hour = (msg.arg1 / 100) / 360;
                        //1000이 1초 1000*60 은 1분 1000*60*10은 10분 1000*60*60은 한시간

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
                            while (isRunning) { //일시정지를 누르면 멈춤
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
                                    return; // 인터럽트 받을 경우 return
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

                    Location A = new Location("pointA");
                    A.setLatitude(latitude);
                    A.setLongitude(lonngitude);


                    try {
                        for (DangerousArea danger : getDanger) {
                            getDalat = danger.getDa_latitude();
                            getDalon = danger.getDa_longitude();
                            getDacon = danger.getDa_content();
                            //Log.d("위험지역", test + " " + test2 + test3);

                            Location B = new Location("pointA");
                            B.setLatitude(Double.parseDouble(getDalat));
                            B.setLongitude(Double.parseDouble(getDalon));
                            distance = A.distanceTo(B);
                            // Log.d(test3 + "거리는", String.valueOf(distance));
                            //float를 정수로
                            int a = (int) distance;
                            //정수로바꾼거를 문자열로
                            String cut_format = String.valueOf(a);
                            //문자열의 길이를 구한다
                            int leget = cut_format.length();
                            //

                            if (leget == 2) {
                                String get = cut_format.substring(cut_format.length() - 2);
                                String get2 = get.substring(0, 1);
                                if (get2.equals("7")) {
                                    value = getDacon + "위험구간이 " + get + "미터 앞입니다. 주의하세요";
                                    textToSpeech = new TextToSpeech(this, this);
                                    // Log.d("십의자리숫자", "70m 전방에 주의구간입니동");
                                }
                            }
                            if (leget == 3) {
                                String get = cut_format.substring(cut_format.length() - 3);
                                String get2 = get.substring(0, 1);
                                if (get2.equals("3")) {
                                    value = "300m앞에 위험구간입니다";
                                    textToSpeech = new TextToSpeech(this, this);
                                    // Log.d("십의자리숫자", "300m 전방에 주의구간입니동");
                                }
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    // 위도 경도 정보 배열 저장
                    witch_lat.add(latitude);
                    witch_lon.add(lonngitude);
                    godoArray.add((float) location.getAltitude());

                    // 최대 최소 위도 경도 계산
                    if (maxLat < latitude) maxLat = latitude;
                    if (minLat > latitude) minLat = latitude;
                    if (maxLon < lonngitude) maxLon = lonngitude;
                    if (minLon > lonngitude) minLon = lonngitude;

                    // 고도 정보 저장
                    ele.add(location.getAltitude());

                    //현재속도
                    getSpeed = Double.parseDouble(String.format("%.1f", location.getSpeed() * 3600 / 1000));
                    binding.nowspeed.setText(String.format("%.1f", getSpeed));  //Get Speed
                    binding.mSpeed.setText(String.format("%.1f", getSpeed) + "km/h");

                    MapPolyline polyline = new MapPolyline();
                    polyline.setLineColor(Color.argb(128, 255, 51, 0));

                    polyline.addPoint(MapPoint.mapPointWithGeoCoord(latitude, lonngitude));
                    mMapView.addPolyline(polyline);
                    mMapView.isShowingCurrentLocationMarker();

                    // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
                    MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
                    int padding = 50; // px
                    mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding, 2, 3));

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
                        /*폴리라인 그리기 */
                        MapPolyline polyline2 = new MapPolyline();
                        polyline.setLineColor(Color.argb(128, 255, 51, 0));
                        polyline.addPoint(MapPoint.mapPointWithGeoCoord(mLastlocation.getLatitude(), mLastlocation.getLongitude()));
                        mMapView.addPolyline(polyline);

                        // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
                        MapPointBounds mapPointBounds2 = new MapPointBounds(polyline.getMapPoints());
                        mMapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds2, padding, 2, 3));

                        if (getSpeed > maX) {
                            maX = getSpeed;
                            binding.maxspeed.setText(String.format("%.1f", maX));
                            cha_max = String.format("%.0f", maX);
                        }
                        //이동거리
                        hap = hap + mLastlocation.distanceTo(location);
                        cha_dis = String.format("%.0f", hap);

                        binding.dis.setText(String.format("%.1f", hap));
                        binding.mDistance.setText(String.format("%.1f", hap) + "m");

                        double killlo;
                        if (hap >= 1000) {
                            killlo = hap / 0;
                            binding.dis.setText(String.format("%.1f", killlo));
                            binding.mDistance.setText(String.format("%.1f", killlo) + "km");
                        }

                        //평균속도
                        avg = hap / total_time;
                        binding.avgspeed.setText(String.format("%.1f", Double.parseDouble(String.valueOf(avg))));
                        cha_avg = String.format("%.0f", (avg));

                        //획득고도
                        //획득고도
                        getgodoval = (int) (location.getAltitude() - mLastlocation.getAltitude());
                        if (getgodoval > 0) {
                            getgodo += getgodoval;
                            binding.getgodo.setText(String.valueOf(getgodo));
                        }

                        //고도
                        binding.Nowgodo.setText(String.format("%.0f", location.getAltitude()) + "m");
                    }
                    // 현재위치를 지난 위치로 변경
                    mLastlocation = location;
                }


                @Override
                public void onInit(int i) {
                    if (i == TextToSpeech.SUCCESS) {
                        textToSpeech.setLanguage(Locale.KOREAN);
                        textToSpeech.setPitch(0.6f);
                        textToSpeech.setSpeechRate(1f);
                        textToSpeech.speak(value, TextToSpeech.QUEUE_FLUSH, null);
                    }
                }


                @Override
                public void onStatusChanged(String provider, int status, Bundle extras) {

                }

                @Override
                public void onProviderEnabled(String provider) {
                // 위치정보 업데이트
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
                    locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 5, this);
                }

                @Override
                public void onProviderDisabled(String provider) {

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
                protected void onDestroy() {
                    super.onDestroy();
                    Log.d("onDestroy", "onDestroy");

                }

                @Override
                protected void onPause() {
                    super.onPause();
                    Log.d("onPause", "onPause");
                }

                @Override
                protected void onResume() {
                    super.onResume();
                    Log.d("onResume", "OnResume");
                }

                @Override
                protected void onStart() {
                    super.onStart();
                }

                @Override
                protected void onStop() {
                    super.onStop();
                    if (textToSpeech != null) {
                        textToSpeech.stop();
                        textToSpeech.shutdown();
                    }
                    Log.d("다른화면임", "다른화면임");
                }

                private RecognitionListener listener = new RecognitionListener() {
                    @Override
                    public void onReadyForSpeech(Bundle params) {
                        //Toast.makeText(getApplicationContext(), "음성인식을 시작합니다.", Toast.LENGTH_SHORT).show();
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
                                message = "오디오 에러";
                                break;
                            case SpeechRecognizer.ERROR_CLIENT:
                                message = "클라이언트 에러";
                                break;
                            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                                message = "퍼미션 없음";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK:
                                message = "네트워크 에러";
                                break;
                            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                                message = "네트웍 타임아웃";
                                break;
                            case SpeechRecognizer.ERROR_NO_MATCH:
                                message = "찾을 수 없음";
                                break;
                            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                                message = "RECOGNIZER가 바쁨";
                                break;
                            case SpeechRecognizer.ERROR_SERVER:
                                message = "서버가 이상함";
                                break;
                            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                                message = "말하는 시간초과";
                                break;
                            default:
                                message = "알 수 없는 오류임";
                                break;
                        }

                       // Toast.makeText(getApplicationContext(), "에러가 발생하였습니다. : " + message, Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onResults(Bundle results) {
                        // 말을 하면 ArrayList에 단어를 넣고 textView에 단어를 이어줍니다.
                        ArrayList<String> matches =
                                results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);

                        for (int i = 0; i < matches.size(); i++) {
                            a = matches.get(i);
                            Log.d("a의값은", a);
                        }
                        switch (a) {
                            case "위치전송":
                                String sms = "" +
                                        "산에서 다쳤어요! 도와주세요! 제 위치는 " + latitude + ", " + lonngitude + "이고 주소는 " + address_dong + "에요. http://13.209.229.237:8080/app/dangergps/35.896599/128.621009";
                                smsManager.sendTextMessage(number, null, sms, null, null);
                                Toast.makeText(getApplicationContext(), "긴급문자를 전송하였습니다.", Toast.LENGTH_LONG).show();
                                break;
                            case "119에 전화":
                                startActivity(new Intent("android.intent.action.CALL", Uri.parse(tel)));
                                break;
                            case "지인에게 문자":
                                String sms2 = "" +
                                        "산에서 다쳤어요! 도와주세요! 제 위치는 " + latitude + ", " + lonngitude + "이고 주소는 " + address_dong + "에요. http://13.209.229.237:8080/app/dangergps/35.896599/128.621009";
                                smsManager.sendTextMessage(number, null, sms2, null, null);
                                break;
                            case "지인에게 통화":
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
