package com.bike.trailgo.net;

import com.bike.trailgo.BuildConfig;

import java.io.File;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class Server{
    /** Single source of truth for every TrailGo server request. */
    public static final String BASE_URL = normalizeBaseUrl(BuildConfig.TRAILGO_SERVER_BASE_URL);
    private static Server instance;
    private ServerApi api;


    private Server(){
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        api = retrofit.create(ServerApi.class);
    }

    public static Server getInstance(){
        if ( instance == null ) instance = new Server();
        return instance;
    }

    public ServerApi getApi() { return api; }

    public static String getUrl(String path) {
        if (path == null || path.length() == 0) {
            return BASE_URL;
        }
        return BASE_URL + (path.startsWith("/") ? path.substring(1) : path);
    }

    public static String getRiderImageUrl(String imageName) {
        return getUrl("app/getGPX/rider/" + imageName);
    }

    private static String normalizeBaseUrl(String baseUrl) {
        if (baseUrl == null || baseUrl.trim().length() == 0) {
            return "http://192.168.0.5:8000/";
        }
        String normalized = baseUrl.trim();
        return normalized.endsWith("/") ? normalized : normalized + "/";
    }

    public static MultipartBody.Part getFilePart(String path, String fileName){
        MultipartBody.Part part = null;
        File file = new File(path);
        if(file.exists()){
            RequestBody body = RequestBody.create(MediaType.parse("multipart/form-data"), file);
            part = MultipartBody.Part.createFormData("file1", fileName, body);
        }
        return part;
    }
}
