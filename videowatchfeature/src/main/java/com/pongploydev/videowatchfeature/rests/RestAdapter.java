package com.pongploydev.videowatchfeature.rests;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.util.Log;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

@SuppressWarnings("WriteOnlyObject")
public class RestAdapter {

    public static ApiInterface createAPI(String api_url) {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

//        Gson gson = new GsonBuilder()
//                .registerTypeAdapter(CallbackListVideo.class, new CallbackListVideoCreator())
//                .create();

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .cookieJar(new okhttp3.CookieJar() {
                    private final android.webkit.CookieManager cookieManager = android.webkit.CookieManager.getInstance();

                    @Override
                    public void saveFromResponse(okhttp3.HttpUrl url, java.util.List<okhttp3.Cookie> cookies) {
                        String urlString = url.toString();
                        for (okhttp3.Cookie cookie : cookies) {
                            cookieManager.setCookie(urlString, cookie.toString());
                        }
                    }

                    @Override
                    public java.util.List<okhttp3.Cookie> loadForRequest(okhttp3.HttpUrl url) {
                        String cookieHeader = cookieManager.getCookie(url.toString());
                        if (cookieHeader == null || cookieHeader.isEmpty()) {
                            return java.util.Collections.emptyList();
                        }
                        java.util.List<okhttp3.Cookie> cookies = new java.util.ArrayList<>();
                        String[] cookiePairs = cookieHeader.split(";");
                        for (String pair : cookiePairs) {
                            okhttp3.Cookie cookie = okhttp3.Cookie.parse(url, pair.trim());
                            if (cookie != null) {
                                cookies.add(cookie);
                            }
                        }
                        return cookies;
                    }
                })
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        okhttp3.Request request = chain.request();
                        String url = request.url().toString();
                        String cookie = android.webkit.CookieManager.getInstance().getCookie(url);
                        if (cookie != null && !cookie.isEmpty()) {
                            request = request.newBuilder()
                                    .addHeader("Cookie", cookie)
                                    .build();
                        }
                        return chain.proceed(request);
                    }
                })
                .addInterceptor(new Interceptor() { // Custom interceptor to log raw body string
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response response = chain.proceed(chain.request());
                        if (response.body() != null) {
                            try {
                                String bodyString = response.peekBody(1024 * 1024).string();
                                Log.d("API_RAW_RESPONSE", bodyString);
                            } catch (Exception e) {
                                Log.e("API_DEBUG", "Error peeking body: " + e.getMessage());
                            }
                        }
                        return response;
                    }
                })
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        // Allow both pongploydev.x10.mx and pongploydev.infinityfree.me
                        if (hostname.equalsIgnoreCase("pongploydev.x10.mx") || 
                            hostname.equalsIgnoreCase("www.pongploydev.x10.mx") ||
                            hostname.equalsIgnoreCase("pongploydev.infinityfree.me")) {
                            return true;
                        }
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
                    }
                })
                .build();



        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(api_url.endsWith("/") ? api_url : api_url + "/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(
                        GsonConverterFactory.create(gson)
                ) // Use lenient Gson
                .client(okHttpClient)
                .build();

        return retrofit.create(ApiInterface.class);

    }

}
