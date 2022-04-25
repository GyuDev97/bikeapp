package com.example.testapplication.ui.member;

import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;


import com.example.testapplication.R;
import com.example.testapplication.net.HttpClient;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class registerActivity extends AppCompatActivity {
    EditText idEdit, address1Edit, pwEdit, pwCheckEdit, nameEdit, nicknameEdit, address2Edit, phone1Edit, phone2Edit;

    String id, fileName="", Save_Path;

    Uri temp;

    RadioGroup radioGroup;
    RadioButton radioButton;

    ImageView imageView2;

    DatePicker datePicker;

    boolean idCheck = false;

    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;

    private WebView webView;
    private TextView txt_address;
    private Handler handler;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        Toolbar toolbar = findViewById(R.id.toolbar2);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowTitleEnabled(false);

        ArrayAdapter<String> arrayAdapter;

        Button sameButton = findViewById(R.id.button2);
        Button adressButton = findViewById(R.id.button4);
        Button regButton = findViewById(R.id.button5);
        idEdit = findViewById(R.id.editText2);
        address1Edit = findViewById(R.id.editText11);
        pwEdit = findViewById(R.id.editText3);
        pwCheckEdit = findViewById(R.id.editText5);
        nameEdit = findViewById(R.id.editText8);
        nicknameEdit = findViewById(R.id.editText9);
        address2Edit = findViewById(R.id.editText12);
        phone1Edit = findViewById(R.id.editText14);
        phone2Edit = findViewById(R.id.editText15);
        radioGroup = findViewById(R.id.radioGroup);
        datePicker = findViewById(R.id.datePicker);
        imageView2 = findViewById(R.id.imageView8);

        Save_Path = getFilesDir().getPath();

/*        ImageView imageView = findViewById(R.id.imageView);
        imageView.setOnClickListener( v -> {
            finish();
        });*/

        Spinner spinner = findViewById(R.id.spinner);

        ArrayList arrayList = new ArrayList<>();
        arrayList.add("010");
        arrayList.add("011");
        arrayList.add("016");
        arrayList.add("017");
        arrayList.add("018");
        arrayList.add("019");
        arrayList.add("070");

        arrayAdapter = new ArrayAdapter<>(getApplicationContext(),
                android.R.layout.simple_spinner_dropdown_item,
                arrayList);

        spinner.setAdapter(arrayAdapter);

        imageView2.setOnClickListener( v -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(intent,1);
        });

        sameButton.setOnClickListener( v -> {
            try {
                SameCheckTask sameCheckTask = new SameCheckTask();
                id = String.valueOf(idEdit.getText());
                sameCheckTask.execute();
            }catch (Exception e){

            }
        });

        adressButton.setOnClickListener( v -> {
            Intent i = new Intent(registerActivity.this, AddressActivity.class);
            startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
        });

        regButton.setOnClickListener( v -> {

            radioButton = (RadioButton) findViewById(radioGroup.getCheckedRadioButtonId());

            id = String.valueOf(idEdit.getText());
            String pw = String.valueOf(pwEdit.getText());
            String pwCheck = String.valueOf(pwCheckEdit.getText());
            String name = String.valueOf(nameEdit.getText());
            String nickname = String.valueOf(nicknameEdit.getText());
            String address1 = String.valueOf(address1Edit.getText());
            String address2 = String.valueOf(address2Edit.getText());
            String phone1 = String.valueOf(phone1Edit.getText());
            String phone2 = String.valueOf(phone2Edit.getText());
            String phone = spinner.getSelectedItem().toString() + phone1 + phone2;
            String gender = "";
            if ( radioButton != null)
                gender = radioButton.getText().toString();
            if ( gender.equals("남성"))
                gender = "1";
            if ( gender.equals("여성"))
                gender = "2";
            String birth = String.valueOf(datePicker.getYear()) + "-" + String.valueOf(datePicker.getMonth()+1) +  "-" + String.valueOf(datePicker.getDayOfMonth());

            if ( id.equals(""))
                Toast.makeText(getApplicationContext(),"아이디를 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( pw.equals(""))
                Toast.makeText(getApplicationContext(),"비밀번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( pwCheck.equals("") || !pw.equals(pwCheck))
                Toast.makeText(getApplicationContext(),"비밀번호가 맞지 않습니다", Toast.LENGTH_SHORT).show();
            else if ( name.equals(""))
                Toast.makeText(getApplicationContext(),"이름을 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( nickname.equals(""))
                Toast.makeText(getApplicationContext(),"닉네임을 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( address1.equals(""))
                Toast.makeText(getApplicationContext(),"주소를 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( address2.equals(""))
                Toast.makeText(getApplicationContext(),"상세주소를 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( phone1.equals("") || phone2.equals(""))
                Toast.makeText(getApplicationContext(),"전화번호를 입력해주세요", Toast.LENGTH_SHORT).show();
            else if ( radioButton == null)
                Toast.makeText(getApplicationContext(),"성별을 선택해주세요", Toast.LENGTH_SHORT).show();
            else if ( idCheck == false ){
                Toast.makeText(getApplicationContext(),"아이디 중복확인을 해주세요 !", Toast.LENGTH_SHORT).show();
            }
            else {
                InsertRegTask insertRegTask = new InsertRegTask();
                Map<String, String> params = new HashMap<String, String>();
                params.put("id", id);
                params.put("pw", pw);
                params.put("name", name);
                params.put("nickname", nickname);
                params.put("addr1", address1);
                params.put("addr2", address2);
                params.put("phone", phone);
                params.put("gender", gender);
                params.put("birth", birth);
                params.put("type", "1");
                if ( !fileName.equals("")) {
                    params.put("image", fileName);
                    UploadTask uploadTask = new UploadTask();
                    uploadTask.execute();
                }

                insertRegTask.execute(params);

            }
        });
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){
            case android.R.id.home:{ //toolbar의 back키 눌렀을 때 동작
                finish();
                return true;
            }
        }
        return super.onOptionsItemSelected(item);
    }

    public class InsertRegTask extends AsyncTask<Map<String, String>, Integer, String> {

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("POST", "/app/insertreg");
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
            String tempData = s;

            Toast.makeText(getApplicationContext(),"등록 완료", Toast.LENGTH_SHORT).show();
            finish();

        }
    }

    public class SameCheckTask extends AsyncTask<Map<String, String>, Integer, String> {

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            // Http 요청 준비 작업
            //URL은 현재 자기 아이피번호를 입력해야합니다.
            HttpClient.Builder http = new HttpClient.Builder("GET", "/app/sameCheck/" + id);
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
            String tempData = s;

            if ( s.equals("ok") ){
                Toast.makeText(getApplicationContext(),"사용 가능한 아이디입니다",
                        Toast.LENGTH_SHORT).show();
                idCheck = true;
            }
            else
                Toast.makeText(getApplicationContext(),"사용 불가능한 아이디입니다",
                        Toast.LENGTH_SHORT).show();
        }
    }

    public class UploadTask extends AsyncTask<Map<String, String>, Integer, String> {
        String filepath = "";
        ImageView imageView;

        @Override
        protected String doInBackground(Map<String, String>... maps) {

            final String url_address = "http://13.209.229.237:8080/android/fileUpload/rider/" + fileName;

            String str; //결과를 저장할 변수

            try {
                InputStream tempIs = getContentResolver().openInputStream(temp);
                File tempFile = new File(Save_Path + "/" + fileName);

                // 복사
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                int read2;
                byte[] bytes2 = new byte[1024];

                while ((read2 = tempIs.read(bytes2)) != -1) {
                    outputStream.write(bytes2, 0, read2);
                }

                FileInputStream fis = new FileInputStream(tempFile);
                URL url = new URL(url_address);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                //웹서버를 통해 입출력 가능하도록 설정
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);//캐쉬 사용하지 않음
                conn.setRequestMethod("POST");
                //정해진 시간 내에 재접속할 경우 소켓을 새로 생성하지 않고 기존연결 사용
                conn.setRequestProperty("Connection", "Keep-Alive");
                //첨부파일에 대한 정보
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=files");


                DataOutputStream dos = new DataOutputStream(conn.getOutputStream());
                //form-data; name=파일변수명; filename="첨부파일이름"
                dos.writeBytes("--files\r\n"); // --은 파일 시작 알림 표시
                dos.writeBytes("Content-Disposition: form-data;"
                        + "name=\"file1\";"
                        + "filename=\"" + fileName
                        + "\"" + "\r\n");

                dos.writeBytes("\r\n");//줄바꿈 문자
                int bytes = fis.available();
                int maxBufferSize = 1024;
                //Math.min(A, B)둘중 작은값;
                int bufferSize = Math.min(bytes, maxBufferSize);
                byte[] buffer = new byte[bufferSize];
                int read = fis.read(buffer, 0, bufferSize);
                Log.d("Test", "image byte is " + read);
                while (read > 0) {
                    //서버에 업로드
                    dos.write(buffer, 0, bufferSize);
                    bytes = fis.available();
                    bufferSize = Math.min(bytes, maxBufferSize);
                    //읽은 바이트 수
                    read = fis.read(buffer, 0, bufferSize);
                }
                dos.writeBytes("\r\n");//줄바꿈 문자

                dos.writeBytes("--files--\r\n");
                fis.close();
                dos.flush();
                dos.close();

                //서버 응답 처리
                int ch;

                int status = conn.getResponseCode();
                Log.e("status", String.valueOf(status));
                Log.e("method", conn.getRequestMethod());
                InputStream is = conn.getInputStream();
                StringBuffer sb = new StringBuffer();
                while ((ch = is.read()) != -1) { // 내용이 없을 때까지 반복
                    sb.append((char) ch); // 문자 읽어서 저장
                }
                // 스트링.trim() 스트링의 좌우 공백 제거
                str = sb.toString().trim();
                if (str.equals("success")) {
                    str = "파일이 업로드되었습니다.";
                } else if (str.equals("fail")) {
                    str = "파일 업로드 실패...";
                }

                is.close();
                conn.disconnect();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
        }
    }

    public String getImageNameToUri(Uri data) {
        String[] proj = {MediaStore.Images.Media.DISPLAY_NAME};
        Cursor cursor = getContentResolver().query(data, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME);
        cursor.moveToFirst();
        String imgPath = cursor.getString(column_index);
        String imgName = imgPath.substring(imgPath.lastIndexOf("/") + 1);

        return imgName;
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent){
        super.onActivityResult(requestCode, resultCode, intent);
        switch(requestCode){

            case SEARCH_ADDRESS_ACTIVITY:
                if(resultCode == RESULT_OK){
                    String data = intent.getExtras().getString("data");
                    if (data != null)
                        address1Edit.setText(data);
                }
                break;

            case 1:
                if (resultCode == RESULT_OK) {
                    try {
                        // 선택한 이미지에서 비트맵 생성
                        if ( intent.getData() != null ) {
                            InputStream in = getContentResolver().openInputStream(intent.getData());
                            temp = intent.getData();
                            Bitmap img = BitmapFactory.decodeStream(in);
                            in.close();
                            imageView2.setImageBitmap(img);
                            fileName = getImageNameToUri(intent.getData());
                        }
                            // 이미지 표시
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                }
                break;
        }
    }


}
