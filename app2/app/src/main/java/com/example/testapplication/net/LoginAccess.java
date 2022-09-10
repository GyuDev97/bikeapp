package com.example.testapplication.net;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapplication.MainActivity;
import com.example.testapplication.dto.AnLogin;
import com.example.testapplication.dto.LoginInfo;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginAccess extends AppCompatActivity {

    String userid="", userpw="";

    String Save_Path, rider;

    protected ServerApi serverApi;

    private Call<AnLogin> request;
    private Call<LoginInfo> loginInfo;
    private Call<ResponseBody> getFile;

    public String info(){
        return userid;
    }
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


            Intent intent = new Intent(this.getIntent());
            userid = intent.getStringExtra("UserId");
            userpw = intent.getStringExtra("UserPw");

            Save_Path = getFilesDir().getPath();
            serverApi = Server.getInstance().getApi();

            AnLogin login = new AnLogin();
            login.setR_id(userid);
            login.setR_pw(userpw);
            request = serverApi.Login(login);
            request.enqueue(new Callback<AnLogin>() {
                @Override
                public void onResponse(Call<AnLogin> call, Response<AnLogin> response) {
                        AnLogin body = response.body();
                        try {
                            String status = body.getStatus();
                            rider = body.getR_id();

                            if (status.equals("Ok")) {
                                //자동로그인을 위한 앱 내부 저장
                                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor autoLogin = auto.edit();
                                autoLogin.putString("LoginId", rider);
                                autoLogin.commit();

                                Toast toast = Toast.makeText(getApplicationContext(), rider + "님 로그인했습니다.", Toast.LENGTH_SHORT);
                                toast.show();
                                Intent intent = new Intent(LoginAccess.this, MainActivity.class);
                                startActivity(intent);
                                finish();
                                }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }

                @Override
                public void onFailure(Call<AnLogin> call, Throwable t) {

                }
            });
        }
    }