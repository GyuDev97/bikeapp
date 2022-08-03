package com.example.testapplication.net;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.testapplication.MainActivity;
import com.example.testapplication.dto.AnLogin;
import com.example.testapplication.dto.LoginInfo;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

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
    protected NetworkAddr networkAddr;
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try{
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
                    if ( response.code() == 200) {
                        AnLogin body = response.body();
                        try {
                            String status = body.getStatus();
                            rider = body.getR_id();

                            if(status.equals("Ok") ){
                                //자동로그인을 위한 앱 내부 저장
                                SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                                SharedPreferences.Editor autoLogin = auto.edit();
                                autoLogin.putString("LoginId", rider);
                                autoLogin.commit();

                                loginInfo = serverApi.getLoginInfo(userid);
                                loginInfo.enqueue(new Callback<LoginInfo>() {
                                    @Override
                                    public void onResponse(Call<LoginInfo> call, Response<LoginInfo> response) {
                                        if ( response.code() == 200) {
                                            SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
                                            SharedPreferences.Editor autoLogin = auto.edit();
                                            autoLogin.putString("r_clubname", response.body().getCb_name());
                                            autoLogin.putInt("r_club", response.body().getCj_club());
                                            if ( response.body().getR_image() == null)
                                                response.body().setR_image("noImage.jpg");
                                            autoLogin.putString("r_image", response.body().getR_image());
                                            autoLogin.putString("r_nickname", response.body().getR_nickname());

                                            String r_image = response.body().getR_image();

                                            autoLogin.commit();

                                            getFile = serverApi.getFile("rider", response.body().getR_image());
                                            getFile.enqueue(new Callback<ResponseBody>() {
                                                @Override
                                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                                                    if ( response.code() == 200 ){
                                                        writeResponseBodyToDisk(response.body(), r_image);

                                                        Toast toast = Toast.makeText(getApplicationContext(), rider+"님 로그인했습니다.", Toast.LENGTH_SHORT); toast.show();
                                                        Intent intent = new Intent(LoginAccess.this, MainActivity.class);
                                                        startActivity(intent);
                                                        finish();
                                                    }
                                                }

                                                @Override
                                                public void onFailure(Call<ResponseBody> call, Throwable t) {

                                                }
                                            });
                                        }
                                    }

                                    @Override
                                    public void onFailure(Call<LoginInfo> call, Throwable t) {

                                    }
                                });

                            }else{
                                Toast toast = Toast.makeText(getApplicationContext(), "로그인에 실패했습니다.", Toast.LENGTH_SHORT); toast.show();
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }
                }

                @Override
                public void onFailure(Call<AnLogin> call, Throwable t) {
                    Log.e("되나?","안된다");
                }
            });

        }catch(Exception e){

        }
    }

    private void writeResponseBodyToDisk(ResponseBody body, String name) {
        try {
            // todo change the file location/name according to your needs
            File futureStudioIconFile = new File(Save_Path + "/" + name);

            InputStream inputStream = null;
            OutputStream outputStream = null;

            try {
                byte[] fileReader = new byte[4096];

                long fileSize = body.contentLength();
                long fileSizeDownloaded = 0;

                inputStream = body.byteStream();
                outputStream = new FileOutputStream(futureStudioIconFile);

                while (true) {
                    int read = inputStream.read(fileReader);

                    if (read == -1) {
                        break;
                    }

                    outputStream.write(fileReader, 0, read);

                    fileSizeDownloaded += read;
                }

                outputStream.flush();

            } catch (IOException e) {

            } finally {
                if (inputStream != null) {
                    inputStream.close();
                }

                if (outputStream != null) {
                    outputStream.close();
                }
            }
        } catch (IOException e) {

        }
    }

    public class DownloadClubTask extends AsyncTask<Map<String, String>, Integer, String> {
        String directory = "";
        String url = "";

        public DownloadClubTask(String directory, String url) {
            this.directory = directory;
            this.url = url;
        }

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            File dir = new File(Save_Path);
            //상위 디렉토리가 존재하지 않을 경우 생성

            if (!dir.exists()) {
                dir.mkdirs();
            }

            String fileURL = "http://192.168.162.13:8080/app/getGPX/" + directory + "/" + url;
            String LocalPath = Save_Path + "/" + url;

            if (new File(Save_Path + "/" + url).exists() == false) {
                URL imgUrl = null;
                try {
                    imgUrl = new URL(fileURL);

                    //서버와 접속하는 클라이언트 객체 생성
                    HttpURLConnection conn = (HttpURLConnection) imgUrl.openConnection();
                    int response = conn.getResponseCode();

                    File file = new File(LocalPath);

                    InputStream is = conn.getInputStream();
                    OutputStream outStream = new FileOutputStream(file);

                    byte[] buf = new byte[1024];
                    int len = 0;

                    while ((len = is.read(buf)) > 0) {
                        outStream.write(buf, 0, len);
                    }

                    outStream.close();
                    is.close();
                    conn.disconnect();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            SharedPreferences auto = getSharedPreferences("auto", Activity.MODE_PRIVATE);
            SharedPreferences.Editor autoLogin = auto.edit();
            autoLogin.putString("r_image", url);
            autoLogin.commit();

            Toast toast = Toast.makeText(getApplicationContext(), rider+"님 로그인했습니다.", Toast.LENGTH_SHORT); toast.show();
            Intent intent = new Intent(LoginAccess.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }
}
