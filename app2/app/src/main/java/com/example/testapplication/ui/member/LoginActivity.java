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
import com.example.testapplication.net.LoginAccess;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    // 멀티 퍼미션 지정
    private static final int MULTIPLE_PERMISSIONS = 101;

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

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        if (Build.VERSION.SDK_INT >= 23) { // 안드로이드 6.0 이상일 경우 퍼미션 체크
            checkPermissions();
        }

        //자동로그인을 위한 앱 내부 저장
        SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
        SharedPreferences.Editor autoLogin = auto.edit();
        String LoginId = auto.getString("LoginId",null);
        if(LoginId==null){
            Button loginbt = (Button)findViewById(R.id.login_bt);
            TextView userid = (TextView)findViewById(R.id.userid);
            TextView userpw = (TextView)findViewById(R.id.userpw);
            Button registerbt = (Button)findViewById(R.id.join);


            loginbt.setOnClickListener(v -> {
                Intent intent=new Intent(getApplicationContext(), LoginAccess.class);
                intent.putExtra("UserId",userid.getText().toString());
                intent.putExtra("UserPw", userpw.getText().toString());
                startActivity(intent);
                finish();
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


}