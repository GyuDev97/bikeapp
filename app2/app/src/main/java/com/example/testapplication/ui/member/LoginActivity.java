package com.example.testapplication.ui.member;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.testapplication.MainActivity;
import com.example.testapplication.R;
import com.example.testapplication.dto.AnLogin;
import com.example.testapplication.dto.LoginInfo;
import com.example.testapplication.net.Server;
import com.example.testapplication.net.ServerApi;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    // 멀티 퍼미션 지정
    private static final int MULTIPLE_PERMISSIONS = 101;

    //권한 체크
    private String[] permissions = {
            Manifest.permission.INTERNET, //인터넷
            Manifest.permission.ACCESS_NETWORK_STATE, //네트워크
            Manifest.permission.ACCESS_COARSE_LOCATION, // GPS
            Manifest.permission.ACCESS_BACKGROUND_LOCATION, // GPS백그라운드
            Manifest.permission.ACCESS_FINE_LOCATION, // 위치
            Manifest.permission.RECORD_AUDIO, //마이크
            Manifest.permission.WRITE_EXTERNAL_STORAGE, //내장메모리
            Manifest.permission.SEND_SMS, //내장메모리
            Manifest.permission.CALL_PHONE
    };
    
    protected ServerApi serverApi;//서버 호출
    private Call<AnLogin> request; //서버요청 콜
    private Call<LoginInfo> profileRequest;
    String Save_Path, rider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 23) { // 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions();
        }

        //현재 로그인상태인지 체크
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        String LoginId = auto.getString("LoginId",null);
        
        //로그인이 되어있지 않다면 로그인 진행
        if(LoginId==null){
            Button loginbt = (Button)findViewById(R.id.login_bt);
            TextView userid = (TextView)findViewById(R.id.userid);
            TextView userpw = (TextView)findViewById(R.id.userpw);
            Button registerbt = (Button)findViewById(R.id.join);


            loginbt.setOnClickListener(v -> {

                //저장경로
                Save_Path = getFilesDir().getPath();
                //레트로핏
                serverApi = Server.getInstance().getApi();

                AnLogin login = new AnLogin();
                login.setR_id(userid.getText().toString());
                login.setR_pw(userpw.getText().toString());


                request = serverApi.Login(login);
                request.enqueue(new Callback<AnLogin>() {
                    @Override
                    public void onResponse(Call<AnLogin> call, Response<AnLogin> response) {
                        //JSON BODY가져옴
                        AnLogin body = response.body();
                        //로그인 데이터 체크
                        String status = body.getStatus();
                        try {
                            if (status.equals("true")) {
                                completeLogin(userid.getText().toString().trim());
                            }else{
                                //로그인 실패시 toast값 표시
                                Toast toast = Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT);
                                toast.show();
                                return;
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onFailure(Call<AnLogin> call, Throwable t) {
                        Toast toast = Toast.makeText(getApplicationContext(), "서버와 통신에 실패하였습니다.", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                });
            });

            registerbt.setOnClickListener( v -> {
                Intent intent = new Intent ( getApplicationContext(), registerActivity.class);
                startActivity(intent);
            });
        }else{
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void completeLogin(String loginId) {
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        auto.edit().putString("LoginId", loginId).apply();

        profileRequest = serverApi.getLoginInfo(loginId);
        profileRequest.enqueue(new Callback<LoginInfo>() {
            @Override
            public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
                LoginInfo info = response.body();
                if (response.isSuccessful() && info != null) {
                    SharedPreferences.Editor editor = auto.edit();
                    if (info.getR_nickname() != null && !info.getR_nickname().trim().isEmpty()) {
                        editor.putString("r_nickname", info.getR_nickname());
                    }
                    if (info.getR_image() != null && !info.getR_image().trim().isEmpty()) {
                        editor.putString("r_image", info.getR_image());
                    }
                    editor.putString("r_clubname", info.getCb_name() == null ? "" : info.getCb_name());
                    editor.apply();
                }
                openMainScreen();
            }

            @Override
            public void onFailure(Call<LoginInfo> call, Throwable error) {
                openMainScreen();
            }
        });
    }

    private void openMainScreen() {
        if (isFinishing() || isDestroyed()) {
            return;
        }
        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
        startActivity(intent);
        finish();
    }

    private boolean checkPermissions() {
        int result;
        List<String> permissionList = new ArrayList<>();
        for (String pm : permissions) {
            result = ContextCompat.checkSelfPermission(this, pm);
            if (result != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(pm);
            }
        }
        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), MULTIPLE_PERMISSIONS);
            return false;
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MULTIPLE_PERMISSIONS: {
                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++) {
                        if (permissions[i].equals(this.permissions[i])) {
                            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                                showToast_PermissionDeny();
                            }
                        }
                    }
                } else {
                    showToast_PermissionDeny();
                }
                return;
            }
        }

    }

    private void showToast_PermissionDeny() {
        Toast.makeText(this, "권한 요청에 동의 해주셔야 이용 가능합니다. 설정에서 권한 허용 하시기 바랍니다.", Toast.LENGTH_SHORT).show();
        finish();
    }

    @Override
    protected void onDestroy() {
        if (request != null) {
            request.cancel();
        }
        if (profileRequest != null) {
            profileRequest.cancel();
        }
        super.onDestroy();
    }


}
