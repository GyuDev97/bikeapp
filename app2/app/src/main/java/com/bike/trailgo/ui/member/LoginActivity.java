package com.bike.trailgo.ui.member;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bike.trailgo.MainActivity;
import com.bike.trailgo.R;
import com.bike.trailgo.data.LocalRideStore;
import com.bike.trailgo.dto.AnLogin;
import com.bike.trailgo.dto.LoginInfo;
import com.bike.trailgo.net.Server;
import com.bike.trailgo.net.ServerApi;
import com.bike.trailgo.setting.AccountSession;


import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    protected ServerApi serverApi;//서버 호출
    private Call<AnLogin> request; //서버요청 콜
    private Call<LoginInfo> profileRequest;
    String Save_Path, rider;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //현재 로그인상태인지 체크
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        String LoginId = auto.getString("LoginId",null);
        
        //로그인이 되어있지 않다면 로그인 진행
        if(LoginId == null || LoginId.trim().isEmpty()){
            Button loginbt = (Button)findViewById(R.id.login_bt);
            TextView userid = (TextView)findViewById(R.id.userid);
            TextView userpw = (TextView)findViewById(R.id.userpw);
            Button registerbt = (Button)findViewById(R.id.join);
            Button guestStart = findViewById(R.id.guest_start);


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
                        AnLogin body = response.body();
                        if (response.isSuccessful() && body != null && "true".equals(body.getStatus())) {
                            completeLogin(userid.getText().toString().trim());
                        } else {
                            Toast.makeText(getApplicationContext(), "아이디와 비밀번호를 확인해주세요", Toast.LENGTH_SHORT).show();
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
            guestStart.setOnClickListener(v -> {
                AccountSession.beginGuest(this);
                openMainScreen();
            });
        }else{
            Intent intent=new Intent(getApplicationContext(), MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    private void completeLogin(String loginId) {
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        AccountSession.completeMemberLogin(this, loginId);

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
        if (AccountSession.isMember(this) && LocalRideStore.hasRecords(this)) {
            Toast.makeText(this, "이 기기에 게스트 주행 기록이 있습니다. 서버 동기화는 아직 자동으로 진행되지 않습니다.", Toast.LENGTH_LONG).show();
        }
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
