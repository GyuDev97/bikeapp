package com.example.testapplication.ui.riding;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapplication.R;
import com.example.testapplication.SendGPXFile;
import com.example.testapplication.databinding.FragmentRidingEndBinding;
import com.example.testapplication.dto.Competition_Status;
import com.example.testapplication.dto.Mission;
import com.example.testapplication.dto.Mission_Status;
import com.example.testapplication.gpx.GpxInfo;
import com.example.testapplication.net.HttpClient;
import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.google.gson.Gson;

import net.daum.mf.map.api.CameraUpdateFactory;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPointBounds;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONObject;
import org.simpleframework.xml.Serializer;
import org.simpleframework.xml.core.Persister;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FollowEndActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{
    private FragmentRidingEndBinding binding;

    //스타트 액티비티에서 가져오는 String형변수
    int MaxSpeed=0;
    int AvgSpeed=0;
    int Getgodo=0;
    int RestTime=0;
    int IngTime=0;
    int Distence=0;
    String rr_num = "";
    String rr_rider="";
    String rr_comp="";
    String address_dong="";
    String course_name="";
    String comp_point="";
    //스타트 액티비티에서 가져온 값들을 텍스트로 설정


    int check, clearCount=0, riding_point=0;

    MapView mapView;
    //DB로 전송하기 위한 변수
    String open;

    //로그인정보 받아오기
    SharedPreferences auto;
    String LoginId;
    int[] typeScore;

    int r_club;
    int dis;

    //형 변환
    String comp_name, adress_value;

    Intent intent;

    private LineChart lineChart;
    private Object endActivity;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = FragmentRidingEndBinding.inflate(getLayoutInflater());
        View view = binding.getRoot();
        setContentView(view);

        //로그인정보 받아오기
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        LoginId  = auto.getString("LoginId","");
        r_club = auto.getInt("r_club", 0);

        endActivity = this;

        //넘어온 intent값 넣기
        intent = new Intent(this.getIntent());

        Distence = intent.getIntExtra("distence",0); //이동거리
        MaxSpeed = intent.getIntExtra("maxspeed",0); //최대속도
        AvgSpeed = intent.getIntExtra("avgspeed",0); //평균속도
        Getgodo = intent.getIntExtra("getgodo",0); //획득고도
        RestTime = intent.getIntExtra("resttime",0); //휴식시간
        IngTime = intent.getIntExtra("ingtime",0); //라이딩시간(시분초)
        check = intent.getIntExtra("check", 0);
        rr_comp = intent.getStringExtra("rr_comp");
        comp_name = intent.getStringExtra("comp_name");
        adress_value = intent.getStringExtra("addr");
        address_dong = intent.getStringExtra("address_dong");
        course_name = intent.getStringExtra("course_name");
        comp_point = intent.getStringExtra("comp_point");


        Log.d("End Activity", Distence + " " + MaxSpeed + " " + AvgSpeed + " " + Getgodo + " " + RestTime + " " + IngTime +" " +check + " " + rr_comp + " "+ comp_name+ " " + adress_value      );

        ArrayList<Double> witch_lat = (ArrayList<Double>)intent.getSerializableExtra("witch_lat");
        ArrayList<Double> witch_lon = (ArrayList<Double>)intent.getSerializableExtra("witch_lon");
        ArrayList<Double> ele = (ArrayList<Double>)intent.getSerializableExtra("ele");

        //그래프 그리기 위한 변수
        ArrayList<Float> godolist = (ArrayList<Float>)intent.getSerializableExtra("godoarray");
        double maxLat = intent.getExtras().getDouble("maxLat",0);
        double maxLon = intent.getExtras().getDouble("maxLon", 0);
        double minLat = intent.getExtras().getDouble("minLat", 0);
        double minLon = intent.getExtras().getDouble("minLon", 0);


        //선 그래프
        ArrayList<Entry> entry_chart = new ArrayList<>();
        lineChart = (LineChart) findViewById(R.id.chart);//layout의 id
        LineData chartData = new LineData();
        for(int g=0; g<godolist.size(); g++){
            entry_chart.add(new Entry(g, godolist.get(g)));
        }

            /* 만약 (2, 3) add하고 (2, 5)한다고해서
            기존 (2, 3)이 사라지는게 아니라 x가 2인곳에 y가 3, 5의 점이 찍힘 */
        LineDataSet lineDataSet = new LineDataSet(entry_chart, "고도상승");
        chartData.addDataSet(lineDataSet);

        lineChart.setData(chartData);
        lineChart.invalidate();


        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        MapPolyline polyline = new MapPolyline();
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline 컬러 지정.

        // Polyline 좌표 지정.
        for(int i=0; i<witch_lat.size(); i++){
            polyline.addPoint(MapPoint.mapPointWithGeoCoord(witch_lat.get(i), witch_lon.get(i)));
        }

        // Polyline 지도에 올리기.
        mapView.addPolyline(polyline);

        // 지도뷰의 중심좌표와 줌레벨을 Polyline이 모두 나오도록 조정.
        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
        int padding = 100; // px
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));


        Button save = (Button) findViewById(R.id.saveriding);
        RadioGroup openselect = (RadioGroup)findViewById(R.id.selectgroup);

        //앞에서 받아온값들을 텍스트로 설정
        binding.endavg.setText(String.valueOf(AvgSpeed)+"km/h"); //평균속도
        binding.endmax.setText(String.valueOf(MaxSpeed)+"km/h"); //최대속도


        //주소 텍스트
        String text=adress_value.replace("대한민국","");
        binding.addr.setText(text);

        //휴식시간 및 지속시간
        int hour;
        int min;
        int sec = IngTime;

        min = sec/60;
        hour = min/60;
        sec = sec % 60;
        min = min % 60;
        if(hour == 0){
            hour=0;
        }
        if(min==0){
            min=0;
        }

        int r_sec = RestTime;
        int r_hour;
        int r_min;

        r_min = r_sec/60;
        r_hour = r_min/60;
        r_sec = r_sec%60;
        r_min = r_min % 60;
        if(r_hour == 0){
            r_hour=00;
        }
        if(r_min==0){
            r_min=00;
        }
        binding.ending.setText(String.valueOf(hour+"시간 "+min+"분 "+sec+"초")); //지속시간
        binding.endresttime.setText(String.valueOf(r_hour+"시간 "+r_min+"분 "+r_sec+"초")); //휴식시간

        binding.endget.setText(String.valueOf(Getgodo)+"m");
        double killlo = 0;

        if (dis >= 1000) {
            killlo = dis / 1000.0;

            binding.enddis.setText(String.format("%.1f", killlo) + "km");
        }else{
            binding.enddis.setText(Distence+"m");
        }


        // gpx파일 제목을 위해 현재 시간 구하기
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String nowTime = sdfNow.format(date);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        NetworkTask2 networkTask = new NetworkTask2();
        final String select;

        //공개여부 선택
        openselect.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) (group, checkedId) -> {
            RadioGroup rg = (RadioGroup)findViewById(R.id.selectgroup); // 라디오그룹 객체 맵핑
            RadioButton selectedRdo = (RadioButton)findViewById(rg.getCheckedRadioButtonId()); // rg 라디오그룹의 체크된(getCheckedRadioButtonId) 라디오버튼 객체 맵핑
            String selectedValue = selectedRdo.getText().toString();
            String value="";
            switch (selectedValue){
                case "비공개":
                    value="0";
                case "전체 공개":
                    value="1";
            }
            open = value;
        });


        // gpx 파일 생성
        GpxInfo gpxInfoObj = new GpxInfo();
        gpxInfoObj.setCreator("MTBcraft");

        GpxInfo.metadata metaObj = new GpxInfo.metadata();
        gpxInfoObj.setMetadata(metaObj);

        metaObj.setDesc("MTBcraft");

        GpxInfo.bounds boundsObj = new GpxInfo.bounds();
        metaObj.setBounds(boundsObj);

        boundsObj.setMaxLat(maxLat);
        boundsObj.setMaxLon(maxLon);
        boundsObj.setMinLat(minLat);
        boundsObj.setMinLon(minLon);

        GpxInfo.trk trkObj = new GpxInfo.trk();
        gpxInfoObj.setTrk(trkObj);

        GpxInfo.trkseg trksegObj = new GpxInfo.trkseg();
        trkObj.setTrkseg(trksegObj);

        List<GpxInfo.trkpt> trkptArray = new ArrayList<GpxInfo.trkpt>();
        ArrayList<Entry> entries = new ArrayList<>();
        for ( int i = 0; i < witch_lat.size() ; i++){
            GpxInfo.trkpt trkptObj = new GpxInfo.trkpt(witch_lat.get(i), witch_lon.get(i), ele.get(i));
            trkptArray.add(trkptObj);
        }
        trksegObj.setTrkptList(trkptArray);
        // Create a file to save to and make sure to use the path provided from
        // getFilesDir().getPath().
        File xmlFile = new File(getFilesDir().getPath() + "/gpx_" + nowTime + ".gpx");
        // Serialize the Person
        try
        {
            Serializer serializer = new Persister();
            serializer.write(gpxInfoObj, xmlFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        // Create a second person object
        GpxInfo gpxInfoObj2 = null;

        // Deserialize the Person
        if (xmlFile.exists())
        {
            try
            {
                Serializer serializer = new Persister();
                gpxInfoObj2 = serializer.read(GpxInfo.class, xmlFile);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }
        }

        boolean b = gpxInfoObj.equals(gpxInfoObj2);
        // 여기까지 gpx파일 생성


        save.setOnClickListener(v -> {
            try{
                NetworkTask2 networkTask1 = new NetworkTask2();
                Map<String, String> params = new HashMap<String, String>();
                Log.d("로그임","아이디"+LoginId+"거리"+Distence+"최대속도"+MaxSpeed+"평균속도"+AvgSpeed+"고도"+Getgodo+"공개"+open+"휴식"+RestTime+"시간"+IngTime+"이름"+binding.ridingNameinput.getText());
                params.put("rr_rider", LoginId);
                params.put("rr_distance", String.valueOf(Distence));
                params.put("rr_topspeed", String.valueOf(MaxSpeed));
                params.put("rr_avgspeed", String.valueOf(AvgSpeed));
                params.put("rr_high", String.valueOf(Getgodo));
                params.put("rr_gpx", "gpx_" + nowTime + ".gpx");
                params.put("rr_open", open);
                params.put("rr_breaktime", String.valueOf(RestTime));
                params.put("rr_time", String.valueOf(IngTime));
                params.put("rr_area", adress_value);
                params.put("rr_name", binding.ridingNameinput.getText().toString());
                params.put("rr_comp", rr_comp);
                params.put("rr_fonum", course_name);
                networkTask1.execute(params);

                riding_point+= Distence;

                GetRecordTask getRecordTask = new GetRecordTask();
                getRecordTask.execute();



                new getMissionStatus().execute();



                //경쟁전 관련 업데이트
                if (!rr_comp.equals("null")) {
                    new getCompAllScore().execute();


                    UpdateCompScore updateCompScore = new UpdateCompScore();
                    Map<String, String> params2 = new HashMap<>();

                    params2.put("avg_speed", String.valueOf(AvgSpeed));
                    params2.put("rr_comp", rr_comp);
                    params2.put("r_club", String.valueOf(r_club));
                    params2.put("LoginId", LoginId);
                    params2.put("m_point", comp_point);

                    riding_point += Integer.parseInt(comp_point);
                    updateCompScore.execute(params2);
                }

                // gpx 파일 보냄
                SendGPXFile sendObj = new SendGPXFile(getFilesDir().getPath() + "/", "gpx_" + nowTime + ".gpx");
                sendObj.upload();
            }catch(Exception e){
                e.printStackTrace();
            }
        });
    }

    public class GetRecordTask extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/riding/getrecord");

            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;
        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Log.d("GetRecord", s);
                String tempData = s;

                JSONArray jarray2 = new JSONArray(tempData);
                for (int i = 0; i < jarray2.length(); i++) {
                    JSONObject jObject2 = jarray2.getJSONObject(i);
                    rr_num = jObject2.getString("rr_num");
                }
                Log.d("GetRecord2", rr_num);

                TagInsert tgagInsert = new TagInsert();
                Map<String, String> par = new HashMap<String, String>();
                par.put("rr_num", rr_num);
                par.put("rr_rider",LoginId);
                par.put("address_dong","#"+address_dong);
                tgagInsert.execute(par);


            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public class TagInsert extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("POST", "app/riding/taginsert");
            http.addAllParameters(maps[0]);
            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;

        }

        @Override
        protected void onPostExecute(String s) {
            try {
                Log.d("JSON_RESULT", s);
                String tempData = s;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
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
    public void onReverseGeoCoderFoundAddress(MapReverseGeoCoder mapReverseGeoCoder, String s) {

    }

    @Override
    public void onReverseGeoCoderFailedToFindAddress(MapReverseGeoCoder mapReverseGeoCoder) {

    }


    public class NetworkTask2 extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("POST", "app/riding/foup");
            // Parameter 를 전송한다.
            http.addAllParameters(maps[0]);
            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {

        }
    }

    // 경쟁전 현황 점수, 횟수 업데이트
    public class UpdateCompScore extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/updateCompScore");
            // Parameter 를 전송한다.
            http.addAllParameters(maps[0]);
            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {
        }
    }

    //경쟁전 모든 점수현황 가져오기
    public class getCompAllScore extends AsyncTask<Map<String, String>, Integer, String> {

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/getCompAllScore/" + rr_comp);
            // Parameter 를 전송한다.

            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();

            return body;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                String tempData = s;

                Gson gson = new Gson();
                ArrayList<Competition_Status> itemList = new ArrayList<>();
                Competition_Status[] items = gson.fromJson(tempData, Competition_Status[].class);

                for ( int i = 0; i<items.length; i++){
                    if ( items[i].getCs_club() == r_club)
                        items[i].setCs_score( items[i].getCs_score() + Integer.parseInt(String.valueOf(AvgSpeed)));
                    itemList.add(items[i]);
                }

                ArrayList<Integer> tempInt = new ArrayList<>();

                for ( int j = 0; j < itemList.size(); j++) {
                    tempInt.add(itemList.get(j).getCs_score());
                }

                // 크기순 정렬
                Collections.sort(tempInt);
                Collections.reverse(tempInt);

                ArrayList<Competition_Status> NewItemList = new ArrayList<>();

                for ( int l = 0; l < itemList.size(); l ++){
                    for ( int k = 0; k < itemList.size(); k++){
                        if ( tempInt.get(l) == itemList.get(k).getCs_score() )
                            NewItemList.add(itemList.get(k));
                    }
                }

                int count = NewItemList.size();

                updateRank[] UpdateRank = new updateRank[count];
                Map<String, String>[] params3 = new HashMap[count];

                for ( int k = 0; k < count; k++) {
                    UpdateRank[k] = new updateRank();
                    params3[k] = new HashMap<>();
                    params3[k].put("rr_comp", rr_comp);
                    params3[k].put("r_club", String.valueOf(NewItemList.get(k).getCs_club()));
                    params3[k].put("score", String.valueOf(NewItemList.get(k).getCs_score()));
                    params3[k].put("rank", String.valueOf(k+1));
                    UpdateRank[k].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params3[k]);
                }

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    // 순위 업데이트
    public class updateRank extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected synchronized String doInBackground( Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/updateRank/");
            // Parameter 를 전송한다.
            http.addAllParameters(maps[0]);
            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {
        }
    }

    public class getMissionStatus extends AsyncTask<Map<String, String>, Integer, String> {

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/getMissionStatus/" + LoginId);
            // Parameter 를 전송한다.

            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();

            return body;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                String tempData = s;

                Gson gson = new Gson();
                ArrayList<Mission_Status> itemList = new ArrayList<>();
                Mission_Status[] items = gson.fromJson(tempData, Mission_Status[].class);

                for ( int i = 0; i<items.length; i++){
                    itemList.add(items[i]);
                }

                typeScore = new int[itemList.size()];

                for ( int j = 0; j<itemList.size(); j++) {
                    if ( itemList.get(j).getMs_type() == 1)
                        typeScore[0] = itemList.get(0).getMs_score() + Integer.parseInt(String.valueOf(Distence)); // 총 거리 계산
                    else if ( itemList.get(j).getMs_type() == 2) {
                        typeScore[1] = itemList.get(1).getMs_score(); // 경쟁전 참여 횟수 계산
                        if (!rr_comp.equals("null"))
                            typeScore[1] += 1;
                    }
                    else if ( itemList.get(j).getMs_type() == 3) {
                        typeScore[2] = itemList.get(j).getMs_score();
                    }

                }

                new getNoClearMission().execute();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public class getNoClearMission extends AsyncTask<Map<String, String>, Integer, String> {

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/getNoClearMission/" + LoginId);
            // Parameter 를 전송한다.

            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();

            return body;
        }

        @Override
        protected void onPostExecute(String s) {
            try{
                String tempData = s;

                Gson gson = new Gson();
                ArrayList<Mission> itemList = new ArrayList<>();
                ArrayList<Mission> clearList = new ArrayList<>();
                Mission[] items = gson.fromJson(tempData, Mission[].class);

                for ( int i = 0; i<items.length; i++){
                    itemList.add(items[i]);
                }

                for ( int j = 0; j<items.length; j++) {
                    if (itemList.get(j).getM_type() == 1) {
                        String tempStr = itemList.get(j).getM_content();
                        String changeStr = tempStr.replaceAll("[^0-9]", "");
                        if (typeScore[0] >= Integer.parseInt(changeStr)) {
                            clearList.add(itemList.get(j));
                            typeScore[2]++;
                            clearCount++;
                        }
                    }
                    else if ( itemList.get(j).getM_type() == 2){
                        String tempStr = itemList.get(j).getM_content();
                        String changeStr = tempStr.replaceAll("[^0-9]", "");
                        if ( typeScore[1] >= Integer.parseInt(changeStr)){
                            clearList.add(itemList.get(j));
                            typeScore[2]++;
                            clearCount++;
                        }
                    }

                    if ( itemList.get(j).getM_type() == 3){
                        String tempStr = itemList.get(j).getM_content();
                        String changeStr = tempStr.replaceAll("[^0-9]", "");
                        if ( typeScore[2] >= Integer.parseInt(changeStr)){
                            clearList.add(itemList.get(j));
                            typeScore[2]++;
                            clearCount++;
                        }
                    }

                    if ( itemList.get(j).getM_type() == 4){
                        String tempStr = itemList.get(j).getM_content();
                        String changeStr = tempStr.replaceAll("[^0-9]", "");
                        if ( Integer.parseInt(String.valueOf(AvgSpeed)) >= Integer.parseInt(changeStr)){
                            clearList.add(itemList.get(j));
                            typeScore[2]++;
                            clearCount++;
                        }
                    }
                }

                updateMissionStatus UMS = new updateMissionStatus();

                Map<String, String> params4 = new HashMap<>();

                params4.put("LoginId", LoginId);
                params4.put("typeScore1", String.valueOf(typeScore[0]));
                params4.put("typeScore2", String.valueOf(typeScore[2]));
                UMS.execute(params4);


                int count = clearList.size();
                if ( count >= 1 ){
                    SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    Date time = new Date();
                    String time2 = format.format(time);
                    insertMissionCom[] IMC = new insertMissionCom[count];
                    Map<String, String>[] params5 = new HashMap[count];
                    for ( int k = 0; k < clearList.size(); k++){
                        IMC[k] = new insertMissionCom();
                        params5[k] = new HashMap<>();
                        params5[k].put("LoginId", LoginId);
                        params5[k].put("mc_mission", String.valueOf(clearList.get(k).getM_num()));
                        params5[k].put("mc_time", time2);
                        params5[k].put("m_point", String.valueOf(clearList.get(k).getM_point()));

                        riding_point += clearList.get(k).getM_point();
                        IMC[k].executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, params5[k]);
                    }
                }

                // 다이얼로그 바디
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder((Context) endActivity);
                // 메세지
                String text = "라이딩 기록이 저장되었습니다";
                String text1 = "";
                String text2 = "";
                String text3 = riding_point + " 라이딩 포인트 획득 !";

                alert_confirm.setMessage(text + "\n" + text3);

                // 경쟁전 완료
                if (!rr_comp.equals("null") && clearList.size() < 1) {
                    text1 = comp_name;
                    alert_confirm.setMessage( text +"\n" + text1+"에 참가하셨습니다\n" + text3);
                }

                // 경쟁전X, 미션 완료
                if ( rr_comp.equals("null") && clearList.size() >= 1 ){
                    for ( int l = 0; l < clearList.size(); l++){
                        text2 += clearList.get(l).getM_name() + " 미션 성공\n";
                    }
                    alert_confirm.setMessage( text + "\n" + text2 + "\n" + text3);
                }

                // 미션, 경쟁전 완료
                if ( !rr_comp.equals("null") || clearList.size() >= 1){
                    text1 = comp_name;
                    for ( int l = 0; l < clearList.size(); l++){
                        text2 += clearList.get(l).getM_name() + " 미션 성공\n";
                    }
                    alert_confirm.setMessage( text +"\n" + text1+"에 참가하셨습니다\n" + text2 + "\n" + text3);
                }
                // 확인 버튼 리스너
               /* alert_confirm.setPositiveButton("확인", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Intent intent2 = new Intent(FollowEndActivity.this, SubActivity.class);
                        startActivity(intent2);
                        finish();
                    }
                });*/
                // 다이얼로그 생성
                AlertDialog alert = alert_confirm.create();
                // 다이얼로그 타이틀
                alert.setTitle("업데이트 로그");
                // 다이얼로그 보기
                alert.show();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public class updateMissionStatus extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected synchronized String doInBackground( Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/updateMissionStatus/");
            // Parameter 를 전송한다.
            http.addAllParameters(maps[0]);
            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {
        }
    }

    public class insertMissionCom extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected synchronized String doInBackground( Map<String, String>... maps) {
            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/insertMissionCom/");
            // Parameter 를 전송한다.
            http.addAllParameters(maps[0]);
            //Http 요청 전송
            HttpClient post = http.create();
            post.request();

            // 응답 상태코드 가져오기
            int statusCode = post.getHttpStatusCode();

            // 응답 본문 가져오기
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {

        }
    }

}