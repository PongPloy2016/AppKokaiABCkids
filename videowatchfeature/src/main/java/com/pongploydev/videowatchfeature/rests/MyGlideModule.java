package com.pongploydev.videowatchfeature.rests;

import android.content.Context;
import androidx.annotation.NonNull;
import com.bumptech.glide.Glide;
import com.bumptech.glide.Registry;
import com.bumptech.glide.annotation.GlideModule;
import com.bumptech.glide.integration.okhttp3.OkHttpUrlLoader;
import com.bumptech.glide.load.model.GlideUrl;
import com.bumptech.glide.module.AppGlideModule;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;
import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLSession;
import okhttp3.OkHttpClient;

@GlideModule
public class MyGlideModule extends AppGlideModule {
    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(20, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(chain -> {
                    okhttp3.Request request = chain.request();
                    String url = request.url().toString();
                    String cookie = android.webkit.CookieManager.getInstance().getCookie(url);
                    
                    okhttp3.Request.Builder builder = request.newBuilder()
                            .header("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36");
                    
                    if (cookie != null && !cookie.isEmpty()) {
                        builder.addHeader("Cookie", cookie);
                    }
                    return chain.proceed(builder.build());
                })
                .hostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String hostname, SSLSession session) {
                        if (hostname.equalsIgnoreCase("php-laravel-app-hslg.onrender.com") ||
                            hostname.equalsIgnoreCase("pongploydev.infinityfree.me") || 
                            hostname.equalsIgnoreCase("www.pongploydev.x10.mx") ||
                            hostname.equalsIgnoreCase("pongploydev.x10.mx")) {
                            return true;
                        }
                        return HttpsURLConnection.getDefaultHostnameVerifier().verify(hostname, session);
                    }
                })
                .build();
        registry.replace(GlideUrl.class, InputStream.class, new OkHttpUrlLoader.Factory(client));
    }
}
