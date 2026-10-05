package com.bike.trailgo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;


public class SplashActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.splash_activity);

        Handler hd = new Handler();
        hd.postDelayed(new SplashHandler(), 1000); // 1초 후에 hd handler 실행  3000ms = 3초

    }

    private class SplashHandler implements Runnable{
        public void run(){
            // Store MVP starts locally.  Account-backed sync remains deliberately unreachable
            // until its authentication and ownership model ships.
            startActivity(new Intent(getApplication(), MainActivity.class));
            finish(); // 로딩페이지 Activity stack에서 제거
        }
    }

    @Override
    public void onBackPressed() {
        //초반 플래시 화면에서 넘어갈때 뒤로가기 버튼 못누르게 함
    }

}
