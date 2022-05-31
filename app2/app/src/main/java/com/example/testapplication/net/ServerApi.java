package com.example.testapplication.net;


import com.example.testapplication.dto.AnLogin;
import com.example.testapplication.dto.Badge;
import com.example.testapplication.dto.CompClub;
import com.example.testapplication.dto.CompScore;
import com.example.testapplication.dto.Competition;
import com.example.testapplication.dto.CourseReview;
import com.example.testapplication.dto.DangerousArea;
import com.example.testapplication.dto.LoginInfo;

import com.example.testapplication.dto.Mission;
import com.example.testapplication.dto.MissionRanking;
import com.example.testapplication.dto.RidingRecord;

import com.example.testapplication.dto.ScrapStatus;
import com.example.testapplication.dto.Tag_Status;
import com.example.testapplication.dto.User;
import com.example.testapplication.ui.riding.EndActivity;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;

public interface ServerApi {
    @GET("app/users")
    Call<List<User>> getUsers();

    @GET("app/users/{id}")
    Call<User> getUser(@Path("id") int id);

    @POST("app/login")
    Call<AnLogin> Login(@Body AnLogin login);

    @GET("app/getLoginInfo/{LoginId}")
    Call<LoginInfo> getLoginInfo(@Path("LoginId") String LoginId);

    @GET("app/getGPX/{directory}/{url}")
    Call<ResponseBody> getFile(@Path("directory") String directory, @Path("url") String url);

    @GET("app/get/{rr_rider}")
    Call<List<RidingRecord>> getMyRecord(@Path("rr_rider") String rr_rider);

    //라이딩 기록 저장
    @FormUrlEncoded
    @POST("app/riding/upload")
    Call<RidingRecord> InsertRecord(@FieldMap HashMap<String, Object> param);

    //라이딩 기록 저장 후 해당 기록 가져오기
    @GET("app/riding/getrecord")
    Call<List<RidingRecord>> getInsertSel();

    //태그삽입
    @FormUrlEncoded
    @POST("app/riding/taginsert")
    Call<Tag_Status> tagInsert(@FieldMap Map<String, String> param);

    //위험지역가져오기
    @GET("app/riding/danger")
    Call<List<DangerousArea>> getDangerArea();

    //라이딩 기록
    @GET("app/get/{loginId}")
    Call<ArrayList<RidingRecord>> getRecord(@Path("loginId") String loginId);

    //라이딩 상세보기
    @GET("app/get/detail/{rr_num}")
    Call<List<RidingRecord>> getRecordDetail(@Path("rr_num") String rr_num);

    //라이딩 공개설정
    @FormUrlEncoded
    @POST("app/recordset/open")
    Call<RidingRecord> setOpen(@FieldMap HashMap<String, Object> param);

    //라이딩 기록
    @GET("app/riding/scrap/{rr_rider}")
    Call<ArrayList<ScrapStatus>> getScrap(@Path("rr_rider") String rr_rider);

    //태그 기록
    @GET("app/record/gettag/{rr_num}")
    Call<List<Tag_Status>> getTag(@Path("rr_num") String rr_num);

    //위험지역 정보
    @GET("app/danger/{id}")
    Call<List<DangerousArea>> getDanger(@Path("id") String id);

    //파일 업로드
    @Multipart
    @POST("app/fileUpload/{dir}/{fileName}")
    Call<Void> upload(
            @Part MultipartBody.Part file1,
            @Path("dir") String dir,
            @Path("fileName") String fileName
    );

    //위험지역 등록
    @POST("app/insertDanger")
    Call<Void> insertDanger(@Body DangerousArea d_area);

    @GET("app/riding/course")
    Call<List<RidingRecord>> getCourseList();

    @GET("app/getCourseReview/{c_num}")
    Call<List<CourseReview>> getReviewList(@Path("c_num") String c_num);

    @FormUrlEncoded
    @POST("app/updateCourseReview")
    Call<Void> reviewUpdate(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("app/deleteCourseReview")
    Call<Void> deleteReview(@FieldMap HashMap<String, Object> param);

    @FormUrlEncoded
    @POST("app/insertCourseReview")
    Call<Void> insertReview(@FieldMap HashMap<String, Object> param);
}
