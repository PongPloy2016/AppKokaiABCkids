package com.pongploydev.priyankvex.paintapp.utils;

import android.app.Application;
import android.content.Context;
import android.speech.tts.TextToSpeech;


import java.util.Locale;


public class AppControl extends Application {
    Context context;
    public static TextToSpeech textToSpeech;
    @Override
    public void onCreate() {
        super.onCreate();
        context=this;
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
