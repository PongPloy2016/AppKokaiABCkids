package com.pongploydev.priyankvex.paintapp.activity.learning;

import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.priyankvex.paintapp.model.LearningCategory;
import com.pongploydev.priyankvex.paintapp.R;
import com.pongploydev.priyankvex.paintapp.utils.CategoryDataHelper;
import com.pongploydev.priyankvex.paintapp.adapter.LearningCategoriesAdapter;

import java.util.List;

public class LeaningActvity extends AppCompatActivity {

    private Context context;
    private int activityType = 1;
    private TextView txtExamTitle;
    private RecyclerView rvHomeCategories;
    // [NEW] Random Pick UI
    private com.google.android.material.floatingactionbutton.FloatingActionButton fabRandom;
    private android.media.SoundPool soundPool;
    private int soundClickId;

    private MediaPlayer mediaPlayer;
    private boolean isSoundLoaded = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        context = this;

        // 1. Get Intent Data
        Intent intent = getIntent();
        activityType = intent.getIntExtra("Type", 1);

        // [NEW] Init Sounds
        initSounds();

        initViews();
        setupDataAndAdapter();

        // 3. Load Ads (สมมติว่า Utils.loadBannerAd มีอยู่)
        // Utils.loadBannerAd(this, llAdView, llAdViewFacebook);

        // [NEW] Initialize MediaPlayer
        // Music handled by MusicManager in onResume
    }

    private void initSounds() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new android.media.SoundPool.Builder()
                    .setMaxStreams(5)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new android.media.SoundPool(5, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        try {
            soundClickId = soundPool.load(this, R.raw.button, 1);
            soundPool.setOnLoadCompleteListener(new android.media.SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(android.media.SoundPool soundPool, int sampleId, int status) {
                    if (status == 0)
                        isSoundLoaded = true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playClickSound() {
        if (isSoundLoaded && soundPool != null) {
            soundPool.play(soundClickId, 1f, 1f, 0, 0, 1f);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
        }
        com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().startMusic(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
        com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().pauseMusic();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        if (soundPool != null) {
            soundPool.release();
            soundPool = null;
        }
        // MusicManager manages its own lifecycle
    }

    private void initViews() {
        // Find Views
        txtExamTitle = findViewById(R.id.txtTitleSubHome);
        rvHomeCategories = findViewById(R.id.rvHomeCategories);
//        llAdView = findViewById(R.id.llAdView);
//        llAdViewFacebook = findViewById(R.id.llAdViewFacebook);
        fabRandom = findViewById(R.id.fabRandom);

        // Set Title based on Type
        txtExamTitle.setText(CategoryDataHelper.getTitleForType(activityType));

        // Note: The original XML had txtTitleSubHome and txtExamTitle
        // I use txtTitleSubHome as the main title as per the logic.

        // [NEW] FAB Listener
        fabRandom.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playClickSound();
                pickRandomCategory();
            }
        });
    }

    private void pickRandomCategory() {
        List<LearningCategory> categories = CategoryDataHelper.getAllCategories();
        if (categories != null && !categories.isEmpty()) {
            int randomIndex = new java.util.Random().nextInt(categories.size());
            // Scroll to the item
            rvHomeCategories.smoothScrollToPosition(randomIndex);

            // Highlight or simulate click after a short delay
            new android.os.Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    RecyclerView.ViewHolder viewHolder = rvHomeCategories.findViewHolderForAdapterPosition(randomIndex);
                    if (viewHolder != null) {
                        viewHolder.itemView.performClick();
                    } else {
                        // Fallback if view holder is not ready (e.g. not visible)
                        // Launch activity directly
                        LearningCategory category = categories.get(randomIndex);
                        launchSubActivity(category);
                    }
                }
            }, 500);
        }
    }

    // Helper to launch activity directly if View is not accessible
    private void launchSubActivity(LearningCategory category) {
        Intent intent;
        Class<?> targetActivity = com.pongploydev.priyankvex.paintapp.activity.learning.SubActivity.class;
        intent = new Intent(context, targetActivity);
        intent.putExtra("categoryPosition", category.getCategoryPosition());
        intent.putExtra("Category", category.getTitle());
        intent.putExtra("Type", activityType);
        startActivity(intent);
    }

    private void setupDataAndAdapter() {
        // ใช้ Data Model จาก Helper
        List<LearningCategory> categories = CategoryDataHelper.getAllCategories();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        rvHomeCategories.setLayoutManager(gridLayoutManager);

        // ส่ง List<LearningCategory> เข้า Adapter
        LearningCategoriesAdapter homeCategoriesAdapter = new LearningCategoriesAdapter(context, categories,
                activityType);
        rvHomeCategories.setAdapter(homeCategoriesAdapter);
    }

    public void onClickBack(View view) {
        finish();
    }
}