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

import com.example.testapplication.dto.Competition_Status;
import com.example.testapplication.dto.Mission;
import com.example.testapplication.dto.Mission_Status;
import com.example.testapplication.gpx.GpxInfo;
import com.example.testapplication.R;
import com.example.testapplication.SendGPXFile;
import com.example.testapplication.databinding.FragmentRidingEndBinding;
import com.example.testapplication.dto.RidingRecord;
import com.example.testapplication.dto.Tag_Status;
import com.example.testapplication.net.HttpClient;
import com.example.testapplication.net.Server;
import com.example.testapplication.net.ServerApi;
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

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EndActivity extends AppCompatActivity implements MapView.CurrentLocationEventListener, MapReverseGeoCoder.ReverseGeoCodingResultListener{
    private FragmentRidingEndBinding binding;

    //????낆쉐?棺??짆??눀?
    private ServerApi serverApi = Server.getInstance().getApi();

    //?????????ロ룂???????????좊읈??嶺뚮ㅎ?닸쾮??String?嶺???
    int MaxSpeed=0;
    int AvgSpeed=0;
    int Getgodo=0;
    int RestTime=0;
    int IngTime=0;
    int Distence=0;
    int rr_num;
    String rr_comp="";
    String address_dong="";
    //?????????ロ룂???????????좊읈??嶺뚮ㅎ?닸쾮???좊즴????????몄릇?嶺뚮ㅎ?붷ㅇ????源놁젳

    int check, clearCount=0;
    MapView mapView;
    //DB????ш끽維뽬땻????꾨탿 ??ш낄援η뵳??怨뚮뼚???
    String open;

    //?棺??짆??嶺뚮ㅎ???륁녃??袁⑸즵?룸돁????됰씚逾?
    SharedPreferences auto;
    String LoginId;
    int[] typeScore;

    int r_club;
    int dis;
    int riding_point=0;

    //???怨뚮뼚???
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

        //?棺??짆??嶺뚮ㅎ???륁녃??袁⑸즵?룸돁????됰씚逾?
        auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        LoginId  = auto.getString("LoginId","");
        r_club = auto.getInt("r_club", 0);

        endActivity = this;

        //??筌뚯슦苑??intent???壤굿??몃탿
        intent = new Intent(this.getIntent());

        Distence = intent.getIntExtra("distence",0); //????猷뱀돥?얠쉪??ㅽ떜??
        MaxSpeed = intent.getIntExtra("maxspeed",0); //癲ル슔?됭짆?????뽦뵣
        AvgSpeed = intent.getIntExtra("avgspeed",0); //????????뽦뵣
        Getgodo = intent.getIntExtra("getgodo",0); //????볥뜪??關履??
        RestTime = intent.getIntExtra("resttime",0); //???⑤８六??癰???
        IngTime = intent.getIntExtra("ingtime",0); //??繹먮끏???獄쏅챶六????筌먐뼘??
        check = intent.getIntExtra("check", 0);
        rr_comp = intent.getStringExtra("rr_comp");
        comp_name = intent.getStringExtra("comp_name");
        adress_value = intent.getStringExtra("addr");
        address_dong = intent.getStringExtra("address_dong");

        Log.d("End Activity", Distence + " " + MaxSpeed + " " + AvgSpeed + " " + Getgodo + " " + RestTime + " " + IngTime +" " +check + " " + rr_comp + " "+ comp_name+ " " + adress_value      );

        ArrayList<Double> witch_lat = (ArrayList<Double>)intent.getSerializableExtra("witch_lat");
        ArrayList<Double> witch_lon = (ArrayList<Double>)intent.getSerializableExtra("witch_lon");
        ArrayList<Double> ele = (ArrayList<Double>)intent.getSerializableExtra("ele");

        //??숆강筌?????숆강筌??ⓦ꺂糾???ш낄援η뵳??怨뚮뼚???
        ArrayList<Float> godolist = (ArrayList<Float>)intent.getSerializableExtra("godoarray");
        double maxLat = intent.getExtras().getDouble("maxLat",0);
        double maxLon = intent.getExtras().getDouble("maxLon", 0);
        double minLat = intent.getExtras().getDouble("minLat", 0);
        double minLon = intent.getExtras().getDouble("minLon", 0);


        //????숆강筌???
        ArrayList<Entry> entry_chart = new ArrayList<>();
        lineChart = (LineChart) findViewById(R.id.chart);//layout??id
        LineData chartData = new LineData();
        for(int g=0; g<godolist.size(); g++){
            entry_chart.add(new Entry(g, godolist.get(g)));
        }

            /* 癲ル슢??節됰쑏?(2, 3) add???寃뗏?(2, 5)??筌먲퐢???關履???
            ??れ삀???(2, 3)??????瑜????釉먮윪????ш끽維???x??좊읈? 2?嶺뚮∥梨뜻쾮??y??좊읈? 3, 5???????癲ル슔?蹂?엥??*/
        LineDataSet lineDataSet = new LineDataSet(entry_chart, "Elevation");
        chartData.addDataSet(lineDataSet);

        lineChart.setData(chartData);
        lineChart.invalidate();
        lineDataSet.setValueTextSize(0);


        mapView = new MapView(this);
        ViewGroup mapViewContainer = (ViewGroup) findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);
        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff);
        MapPolyline polyline = new MapPolyline();
        polyline.setTag(1000);
        polyline.setLineColor(Color.argb(128, 255, 51, 0)); // Polyline ??濡ル㎥??癲ル슣????

        // Polyline ???щ뮡嶺?癲ル슣????
        for(int i=0; i<witch_lat.size(); i++){
            polyline.addPoint(MapPoint.mapPointWithGeoCoord(witch_lat.get(i), witch_lon.get(i)));
        }

        // Polyline 癲ル슣????ш끽維??????ⓦ꺂糾?
        mapView.addPolyline(polyline);

        // 癲ル슣????ш끽維???濚욌꼬?댄꺍?筌?苑??⑤틳嫄?? 濚욌꼬?대뮛??怨룐뵛??쒑キ?Polyline??癲ル슢?꾤땟?嶺????쒓낫???ш끽維곩ㅇ??釉뚰???
        MapPointBounds mapPointBounds = new MapPointBounds(polyline.getMapPoints());
        int padding = 100; // px
        mapView.moveCamera(CameraUpdateFactory.newMapPointBounds(mapPointBounds, padding));


        Button save = (Button) findViewById(R.id.saveriding);
        RadioGroup openselect = (RadioGroup)findViewById(R.id.selectgroup);

        //??嚥▲꺆諭???袁⑸즵?룸돁???影??????源낃도 ????몄릇?嶺뚮ㅎ?붷ㅇ????源놁젳
        binding.endavg.setText(String.valueOf(AvgSpeed+"km/h")); //????????뽦뵣
        binding.endmax.setText(String.valueOf(MaxSpeed+"km/h")); //癲ル슔?됭짆?????뽦뵣


        //??낆뒩???????몄릇??
        String text = adress_value == null ? "" : adress_value.replace("\uB300\uD55C\uBBFC\uAD6D", "").trim();
        binding.addr.setText(text);

        //???⑤８六??癰?????癲ル슣??????몃빰??
        int sec = IngTime;
        int hour;
        int min;

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
        binding.ending.setText(hour + "\uC2DC\uAC04 " + min + "\uBD84 " + sec + "\uCD08");
        binding.endresttime.setText(r_hour + "\uC2DC\uAC04 " + r_min + "\uBD84 " + r_sec + "\uCD08");

        binding.endget.setText(Getgodo +"m");
        double killlo = 0;

        if (dis >= 1000) {
            killlo = dis / 1000.0;
            binding.enddis.setText(String.format("%.1f", killlo) + "km");
        }else{
            binding.enddis.setText(Distence+"m");
        }


        // gpx???????筌먯룄肄????ш낄援????ш끽維????癰???????엮??堉?
        long now = System.currentTimeMillis();
        Date date = new Date(now);
        SimpleDateFormat sdfNow = new SimpleDateFormat("yyyyMMdd_HHmmss");
        String nowTime = sdfNow.format(date);

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        final String select;

        //???????? ???ャ뀕??
        openselect.setOnCheckedChangeListener((RadioGroup.OnCheckedChangeListener) (group, checkedId) -> {
            RadioGroup rg = (RadioGroup)findViewById(R.id.selectgroup); // ???⑤벤爰???됰씮?????좊즵??꼯??癲ル슢??린琉삳눀?
            RadioButton selectedRdo = (RadioButton)findViewById(rg.getCheckedRadioButtonId()); // rg ???⑤벤爰???됰씮??????癲ル슪???띿물??getCheckedRadioButtonId) ???⑤벤爰????곷츉????좊즵??꼯??癲ル슢??린琉삳눀?
            String selectedValue = selectedRdo.getText().toString();
            String value="";
            switch (selectedValue){
                case "\uBE44\uACF5\uAC1C":
                    value="0";
                    break;
                case "\uACF5\uAC1C":
                    value="1";
                    break;
            }
            open = value;
        });


        // gpx ???????獄쏅똻??
        GpxInfo gpxInfoObj = new GpxInfo();
        gpxInfoObj.setCreator("TrailGO");

        GpxInfo.metadata metaObj = new GpxInfo.metadata();
        gpxInfoObj.setMetadata(metaObj);

        metaObj.setDesc("TrailGO");

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
        // ???源껉펾??? gpx???????獄쏅똻??


        save.setOnClickListener(v -> {
            try{
                    Call<RidingRecord> Insert;
                    RidingRecord record = new RidingRecord();
                    record.setRr_rider(LoginId);
                    record.setRr_distance(Distence);
                    record.setRr_topspeed(MaxSpeed);
                    record.setRr_avgspeed(AvgSpeed);
                    record.setRr_high(Getgodo);
                    record.setRr_gpx("gpx_" + nowTime + ".gpx");
                    record.setRr_open(Integer.parseInt(open));
                    record.setRr_breaktime(RestTime);
                    record.setRr_time(IngTime);
                    record.setRr_area(adress_value);
                    record.setRr_name(binding.ridingNameinput.getText().toString());
                    //   record.setRr_comp(rr_comp);
                    riding_point += Distence;

                    HashMap<String, Object> insert = new HashMap<>();
                    insert.put("rr_rider", LoginId);
                    insert.put("rr_distance", String.valueOf(Distence));
                    insert.put("rr_topspeed", String.valueOf(MaxSpeed));
                    insert.put("rr_avgspeed", String.valueOf(AvgSpeed));
                    insert.put("rr_high", String.valueOf(Getgodo));
                    insert.put("rr_gpx", "gpx_" + nowTime + ".gpx");
                    insert.put("rr_open", open);
                    insert.put("rr_breaktime", String.valueOf(RestTime));
                    insert.put("rr_time", String.valueOf(IngTime));
                    insert.put("rr_area", adress_value);
                    insert.put("rr_name", binding.ridingNameinput.getText().toString());
                    insert.put("rr_comp", rr_comp);

                    Insert = serverApi.InsertRecord(insert);
                    Insert.enqueue(new Callback<RidingRecord>() {
                        @Override
                        public void onResponse(Call<RidingRecord> call, Response<RidingRecord> response) {
                            if(response.code() == 200){
                            }
                        }

                        @Override
                        public void onFailure(Call<RidingRecord> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });

                    Call<List<RidingRecord>> getRecord;
                    getRecord = serverApi.getInsertSel();
                    getRecord.enqueue(new Callback<List<RidingRecord>>() {
                        @Override
                        public void onResponse(Call<List<RidingRecord>> call, Response<List<RidingRecord>> response) {
                            List<RidingRecord> record = response.body();
                            for(RidingRecord record_num : record){
                                rr_num = record_num.getRr_num();
                            }

                            Call<Tag_Status> tagInsert;
                            Map<String, String> par = new HashMap<>();
                            par.put("rr_num", String.valueOf(rr_num));
                            par.put("rr_rider",LoginId);
                            par.put("address_dong","#"+address_dong);


                            tagInsert = serverApi.tagInsert(par);
                            tagInsert.enqueue(new Callback<Tag_Status>() {
                                @Override
                                public void onResponse(Call<Tag_Status> call, Response<Tag_Status> response) {
                                    if(response.code()==200){

                                    }
                                }

                                @Override
                                public void onFailure(Call<Tag_Status> call, Throwable t) {
                                    t.printStackTrace();
                                }
                            });
                        }

                        @Override
                        public void onFailure(Call<List<RidingRecord>> call, Throwable t) {
                            t.printStackTrace();
                        }
                    });

                new getMissionStatus().execute();
                //?濡ろ뜑???????굿??????녿ぅ??熬곣뫀肄?
                if (!rr_comp.equals("null")) {
                    new getCompAllScore().execute();


                    UpdateCompScore updateCompScore = new UpdateCompScore();
                    Map<String, String> params2 = new HashMap<>();

                    params2.put("avg_speed", String.valueOf(AvgSpeed));
                    params2.put("rr_comp", rr_comp);
                    params2.put("r_club", String.valueOf(r_club));
                    params2.put("LoginId", LoginId);
                    updateCompScore.execute(params2);
                }

                // gpx ??????怨뚮옖?濚?
                SendGPXFile sendObj = new SendGPXFile(getFilesDir().getPath() + "/", "gpx_" + nowTime + ".gpx");
                sendObj.upload();
            }catch(Exception e){
                e.printStackTrace();
            }
        });
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


    // ?濡ろ뜑??????ш낄援??????? ????낅묄 ????녿ぅ??熬곣뫀肄?
    public class UpdateCompScore extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected String doInBackground(Map<String, String>... maps) {
            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/riding/updateCompScore");
            // Parameter ????ш끽維뽬땻??筌먲퐢??
            http.addAllParameters(maps[0]);
            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {
        }
    }

    //?濡ろ뜑????癲ル슢?꾤땟?????????ш낄援????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
    public class getCompAllScore extends AsyncTask<Map<String, String>, Integer, String> {

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/getCompAllScore/" + rr_comp);
            // Parameter ????ш끽維뽬땻??筌먲퐢??

            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
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

                // ??????嶺뚮㉡?ｈ??
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

    // ??筌믨퀡彛?????녿ぅ??熬곣뫀肄?
    public class updateRank extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected synchronized String doInBackground( Map<String, String>... maps) {
            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/updateRank/");
            // Parameter ????ш끽維뽬땻??筌먲퐢??
            http.addAllParameters(maps[0]);
            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
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

            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/getMissionStatus/" + LoginId);
            // Parameter ????ш끽維뽬땻??筌먲퐢??

            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
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
                        typeScore[0] = itemList.get(0).getMs_score() + Integer.parseInt(String.valueOf(Distence)); // ??癲꾧퀗??????節뚮쳮雅?
                    else if ( itemList.get(j).getMs_type() == 2) {
                        typeScore[1] = itemList.get(1).getMs_score(); // ?濡ろ뜑????癲ル슔?蹂?덫??????낅묄 ??節뚮쳮雅?
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

            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("GET", "app/getNoClearMission/" + LoginId);
            // Parameter ????ш끽維뽬땻??筌먲퐢??

            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
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

                // ???源낇꼧???⑥????袁⑸즴????
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder((Context) endActivity);
                // 癲ル슢??袁λ빝?壤?
                String text = "??繹먮끏?????れ삀??쎈뭄?????潁뺛깾逾???怨?????덊렡";
                String text2 = "";
                String text3 = riding_point + " ??繹먮끏????????????볥뜪 !";

                alert_confirm.setMessage(text + "\n" + text3);

                if ( clearList.size() >= 1){
                    for ( int l = 0; l < clearList.size(); l++){
                        text2 += clearList.get(l).getM_name() + " \uC644\uB8CC\n";
                    }
                    alert_confirm.setMessage( text + "\n" + text2 + "\n" + text3);
                }
                // ?嶺뚮Ĳ?됮??類?????域밸Ŧ遊얕짆??
                alert_confirm.setPositiveButton("\uD655\uC778", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                    }
                });
                // ???源낇꼧???⑥?????獄쏅똻??
                AlertDialog alert = alert_confirm.create();
                // ???源낇꼧???⑥????????
                alert.setTitle("\uB77C\uC774\uB529 \uC800\uC7A5 \uC644\uB8CC");
                // ???源낇꼧???⑥????怨뚮옖??逾?
                alert.show();

            }catch(Exception e){
                e.printStackTrace();
            }
        }
    }

    public class updateMissionStatus extends AsyncTask<Map<String, String>, Integer, String> {
        @Override
        protected synchronized String doInBackground( Map<String, String>... maps) {
            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/updateMissionStatus/");
            // Parameter ????ш끽維뽬땻??筌먲퐢??
            http.addAllParameters(maps[0]);
            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
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
            // Http ??釉먯뒜??濚욌꼬裕뼘????????
            //URL?? ??ш끽維?????????ш끽維쀩???⑥щ쾺?嶺? ????곸죷???⑤；???筌뤾퍓???
            HttpClient.Builder http = new HttpClient.Builder("PUT", "app/insertMissionCom/");
            // Parameter ????ш끽維뽬땻??筌먲퐢??
            http.addAllParameters(maps[0]);
            //Http ??釉먯뒜????ш끽維뽬땻?
            HttpClient post = http.create();
            post.request();

            // ???쑩?젆????ㅺ컼??熬곣뫀?????좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            int statusCode = post.getHttpStatusCode();

            // ???쑩?젆??怨뚮옖筌?쑜猷???좊읈??嶺뚮ㅎ?닸쾮濡㏓섀?
            String body = post.getBody();
            return body;
        }
        @Override
        protected void onPostExecute(String s) {

        }
    }

}
