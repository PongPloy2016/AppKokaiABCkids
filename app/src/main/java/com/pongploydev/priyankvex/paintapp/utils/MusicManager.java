package com.pongploydev.priyankvex.paintapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import com.pongploydev.priyankvex.paintapp.R;

public class MusicManager {
    private static MusicManager instance;
    private MediaPlayer mediaPlayer;
    private boolean isMusicEnabled = true;
    private static final String PREF_NAME = "AppConfig";
    private static final String KEY_MUSIC_ENABLED = "music_enabled";

    private MusicManager() {
        // Private constructor
    }

    public static synchronized MusicManager getInstance() {
        if (instance == null) {
            instance = new MusicManager();
        }
        return instance;
    }

    public void startMusic(Context context) {
        updateSettings(context);
        if (!isMusicEnabled) {
            stopMusic();
            return;
        }

        if (mediaPlayer == null) {
            mediaPlayer = MediaPlayer.create(context, R.raw.is_sound_cute_music);
            if (mediaPlayer != null) {
                mediaPlayer.setLooping(true);
                mediaPlayer.setVolume(0.5f, 0.5f);
            }
        }

        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
    }

    public void pauseMusic() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    public void stopMusic() {
        if (mediaPlayer != null) {
            if (mediaPlayer.isPlaying()) {
                mediaPlayer.stop();
            }
            mediaPlayer.release();
            mediaPlayer = null;
        }
    }

    public void resumeMusic(Context context) {
        startMusic(context);
    }

    public void setMusicEnabled(Context context, boolean enabled) {
        isMusicEnabled = enabled;
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putBoolean(KEY_MUSIC_ENABLED, enabled).apply();

        if (enabled) {
            startMusic(context);
        } else {
            pauseMusic(); // Or stopMusic() if you prefer releasing resources
        }
    }

    private void updateSettings(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        isMusicEnabled = prefs.getBoolean(KEY_MUSIC_ENABLED, true);
    }

    public boolean isMusicEnabled(Context context) {
        updateSettings(context);
        return isMusicEnabled;
    }
}
