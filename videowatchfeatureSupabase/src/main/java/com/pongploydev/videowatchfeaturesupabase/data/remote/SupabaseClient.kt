package com.pongploydev.videowatchfeaturesupabase.data.remote

import android.util.Log
import com.google.gson.GsonBuilder
import com.pongploydev.videowatchfeaturesupabase.config.AppConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object SupabaseClient {

    private var supabaseApiService: SupabaseApiService? = null

    fun getService(): SupabaseApiService {
        if (supabaseApiService == null) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }

            val okHttpClient = OkHttpClient.Builder()
                .connectTimeout(15, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(20, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build()

            val gson = GsonBuilder()
                .setLenient()
                .create()

            val baseUrl = if (AppConfig.SUPABASE_URL.endsWith("/")) {
                AppConfig.SUPABASE_URL
            } else {
                "${AppConfig.SUPABASE_URL}/"
            }

            val retrofit = Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()

            supabaseApiService = retrofit.create(SupabaseApiService::class.java)
        }
        return supabaseApiService!!
    }
}
