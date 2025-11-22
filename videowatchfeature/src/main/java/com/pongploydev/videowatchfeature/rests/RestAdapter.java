package com.pongploydev.videowatchfeature.rests;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

@SuppressWarnings("WriteOnlyObject")
public class RestAdapter {

    public static ApiInterface createAPI(String api_url) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

//        Gson gson = new GsonBuilder()
//                .registerTypeAdapter(CallbackListVideo.class, new CallbackListVideoCreator())
//                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(5, TimeUnit.SECONDS)
                .writeTimeout(10, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .cache(null)
                .build();



        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(api_url + "/")
                .addConverterFactory(
                        GsonConverterFactory.create()
                ) // ใช้ gson ที่คุณกำหนด
                .client(okHttpClient)
                .build();

        return retrofit.create(ApiInterface.class);

    }

}
