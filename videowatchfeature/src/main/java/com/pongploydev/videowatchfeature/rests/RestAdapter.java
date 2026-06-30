package com.pongploydev.videowatchfeature.rests;

import android.content.Context;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import android.util.Log;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

import com.pongploydev.videowatchfeature.data.remote.ApiInterfaceV1;

@SuppressWarnings("WriteOnlyObject")
public class RestAdapter {

    private static ApiInterface apiInterface;
    private static ApiInterfaceV1 apiInterfaceV1;
    private static Retrofit retrofitInstance;

    private static synchronized Retrofit getRetrofit(Context context, String api_url) {
        if (retrofitInstance != null) {
            return retrofitInstance;
        }

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient.Builder okHttpClientBuilder = new OkHttpClient.Builder()
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
                        if (response.code() == 403) {
                            // Cookie might be expired/invalid, clear cookies to force re-solve challenge
                            Log.w("CookieChallenge", "HTTP 403 detected. Clearing WebView cookies.");
                            android.webkit.CookieManager.getInstance().removeAllCookies(null);
                        }
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
                });

        // Configure OkHttp Cache if context is provided
        if (context != null) {
            try {
                File cacheDir = new File(context.getCacheDir(), "http_cache");
                Cache cache = new Cache(cacheDir, 10 * 1024 * 1024); // 10 MB
                okHttpClientBuilder.cache(cache);
            } catch (Exception e) {
                Log.e("RestAdapter", "Could not create OkHttp Cache", e);
            }
        }

        // Add a network interceptor to override server Cache-Control headers for InfinityFree
        okHttpClientBuilder.addNetworkInterceptor(new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Response originalResponse = chain.proceed(chain.request());
                String url = chain.request().url().toString();
                
                // Do not cache views count or specific analytic endpoints
                if (url.contains("get_total_views")) {
                    return originalResponse;
                }

                String cacheControl = originalResponse.header("Cache-Control");
                if (cacheControl == null || cacheControl.contains("no-store") || cacheControl.contains("no-cache") || 
                    cacheControl.contains("must-revalidate") || cacheControl.contains("max-age=0")) {
                    // Rewrite cache control header to store response for 5 minutes (300 seconds)
                    return originalResponse.newBuilder()
                            .header("Cache-Control", "public, max-age=300")
                            .removeHeader("Pragma")
                            .build();
                }
                return originalResponse;
            }
        });

        OkHttpClient okHttpClient = okHttpClientBuilder.build();

        Gson gson = new GsonBuilder()
                .setLenient()
                .create();

        retrofitInstance = new Retrofit.Builder()
                .baseUrl(api_url.endsWith("/") ? api_url : api_url + "/")
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(
                        GsonConverterFactory.create(gson)
                ) // Use lenient Gson
                .client(okHttpClient)
                .build();

        return retrofitInstance;
    }

    public static ApiInterface createAPI(String api_url) {
        return createAPI(null, api_url);
    }

    public static ApiInterface createAPI(Context context, String api_url) {
        if (apiInterface != null) {
            return apiInterface;
        }
        Retrofit retrofit = getRetrofit(context, api_url);
        apiInterface = retrofit.create(ApiInterface.class);
        return apiInterface;
    }

    public static ApiInterfaceV1 createAPIV1(Context context, String api_url) {
        if (apiInterfaceV1 != null) {
            return apiInterfaceV1;
        }
        Retrofit retrofit = getRetrofit(context, api_url);
        apiInterfaceV1 = retrofit.create(ApiInterfaceV1.class);
        return apiInterfaceV1;
    }
}
