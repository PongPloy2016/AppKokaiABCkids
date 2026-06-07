package com.pongploydev.priyankvex.paintapp.utils;

import android.app.Application;
import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.util.Log;


import com.google.android.gms.ads.MobileAds;
import com.onesignal.Continue;
import com.onesignal.OneSignal;
import com.onesignal.debug.LogLevel;

import java.util.Locale;


public class AppControl extends Application {
    Context context;
    public static TextToSpeech textToSpeech;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;

        MobileAds.initialize(this);

        Log.d("App", "MobileAds initialized");

        // Enable verbose logging for debugging (remove in production)
        OneSignal.getDebug().setLogLevel(LogLevel.VERBOSE);
        // Initialize with your OneSignal App ID
        OneSignal.initWithContext(this, "3836f6b5-1484-496d-b452-d7ab16a5988e");
        // Use this method to prompt for push notifications.
        // We recommend removing this method after testing and instead use In-App Messages to prompt for notification permission.
        OneSignal.getNotifications().requestPermission(false, Continue.none());


        new Utils(context);
//        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
//                .setDefaultFontPath("Fon/OhWhale.ttf")
//                .setFontAttrId(R.attr.fontPath)
//                .build()
//        );
        textToSpeech = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (status != TextToSpeech.ERROR) {
 //                   textToSpeech.setLanguage(Locale.UK);
                   textToSpeech.setLanguage(new Locale("th","TR"));
                }
            }
        });
        textToSpeech.setSpeechRate(0.3f);
    }


}
