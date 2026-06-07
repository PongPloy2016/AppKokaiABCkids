package com.pongploydev.priyankvex.paintapp.activity;

import android.media.MediaPlayer;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.priyankvex.paintapp.adapter.AZRecyclerViewAdapter;
import com.pongploydev.priyankvex.paintapp.model.ItemObject;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MenuActivityAZ extends AppCompatActivity {

    private GridLayoutManager lLayout;
    private TextToSpeech textToSpeech;
    private MediaPlayer successSound;
    private MediaPlayer clickSound;
    private MediaPlayer backgroundMusic;

    // UI
    private Button btnStartAutoRead, btnStopAutoRead, btnToggleMusic;
    private ProgressBar progressBar;
    private TextView tvProgress, tvTitle;
    private LinearLayout layoutProgress;
    private RecyclerView recyclerView;

    // Auto read
    private boolean isAutoReading = false;
    private int currentAutoReadIndex = 0;
    private final Handler autoReadHandler = new Handler();
    private Runnable autoReadRunnable;
    private boolean isMusicPlaying = true;

    private List<ItemObject> rowListItem;
    private AZRecyclerViewAdapter rcAdapter;

    private static final long AUTO_READ_DELAY = 3000; // 3 วินาทีต่อตัวอักษร

    // ✅ เพิ่ม: ตัวแปรจัดการ memory
    private boolean isLowMemoryDevice = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ✅ เพิ่ม: ตรวจสอบ memory ก่อน
        checkDeviceMemory();

        // แก้ crash "Software rendering doesn't support drawRenderNode" ถาวร 100%
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().setDecorFitsSystemWindows(false);
        }

        setContentView(R.layout.activity_menu_az);

        // ดึง View
        setupViews();
        setupRecyclerView(); // ต้องเรียกก่อน setupAutoReadControls
        setupSounds();
        setupTextToSpeech();
        setupAutoReadControls();
        setupTitle();
    }

    // ✅ เพิ่ม: ตรวจสอบหน่วยความจำของอุปกรณ์
    private void checkDeviceMemory() {
        Runtime runtime = Runtime.getRuntime();
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();

        Log.d("MemoryInfo", "Max Memory: " + (maxMemory / 1024 / 1024) + "MB");
        Log.d("MemoryInfo", "Total Memory: " + (totalMemory / 1024 / 1024) + "MB");
        Log.d("MemoryInfo", "Free Memory: " + (freeMemory / 1024 / 1024) + "MB");

        // ถ้า device มี memory น้อยกว่า 512MB
        if (maxMemory < 512 * 1024 * 1024) {
            isLowMemoryDevice = true;
            Log.w("MemoryInfo", "Low memory device detected");
        }
    }

    private void setupViews() {
        btnStartAutoRead = findViewById(R.id.btn_start_auto_read);
        btnStopAutoRead = findViewById(R.id.btn_stop_auto_read);
        btnToggleMusic = findViewById(R.id.btn_toggle_music);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        layoutProgress = findViewById(R.id.layout_progress);
        tvTitle = findViewById(R.id.textViewTitle);
        recyclerView = findViewById(R.id.recycler_view);

        // ✅ แก้ไข: ปรับจำนวนคอลัมน์ตาม memory
        if (isLowMemoryDevice) {
            lLayout = new GridLayoutManager(this, 1); // 1 คอลัมน์สำหรับเครื่อง memory ต่ำ
        } else {
            lLayout = new GridLayoutManager(this, 2); // 2 คอลัมน์ปกติ
        }

        // ปิด overscroll glow/stretch
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            getWindow().getDecorView().setOverScrollMode(View.OVER_SCROLL_NEVER);
        }
    }

    private void setupTitle() {
        if (tvTitle != null) {
            Animation bounce = AnimationUtils.loadAnimation(this, R.anim.bounce);
            tvTitle.startAnimation(bounce);

            // อนิเมชันกระพริบซ้ำทุก 6 วินาที
            final Handler titleHandler = new Handler();
            titleHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    Animation blink = AnimationUtils.loadAnimation(MenuActivityAZ.this, R.anim.blink);
                    tvTitle.startAnimation(blink);
                    titleHandler.postDelayed(this, 6000);
                }
            }, 6000);
        }
    }

    private void setupSounds() {
        try {
            clickSound = MediaPlayer.create(this, R.raw.is_sound_buttonswitch);
            successSound = MediaPlayer.create(this, R.raw.is_sound_success_sound);
            // backgroundMusic handled by MusicManager in onResume but we need to ensure
            // MusicManager respects toggle
            // Actually, MenuActivityAZ has a toggle button that toggles music locally.
            // We should update toggle logic to use MusicManager.

            // Initial Start handled by onResume
        } catch (Exception e) {
            Log.e("Sound", "Sound file not found: " + e.getMessage());
        }
    }

    private void toggleBackgroundMusic() {
        boolean newState = !com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().isMusicEnabled(this);
        com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().setMusicEnabled(this, newState);

        if (newState) {
            btnToggleMusic.setText("� ปิดเพลง");
        } else {
            btnToggleMusic.setText("� เปิดเพลง");
        }
        isMusicPlaying = newState;
    }

    private void playClickSound() {
        if (clickSound != null) {
            clickSound.seekTo(0);
            clickSound.start();
        }
    }

    private void playSuccessSound() {
        if (successSound != null) {
            successSound.seekTo(0);
            successSound.start();
        }
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("th", "TH"));
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    textToSpeech.setLanguage(Locale.US);
                }
                textToSpeech.setPitch(1.4f);
                textToSpeech.setSpeechRate(0.7f);
            }
        });
    }

    private void setupRecyclerView() {
        // ✅ ใช้ภาพที่เหมาะสมตาม memory
        rowListItem = getAllItemList();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(lLayout);
        recyclerView.setOverScrollMode(View.OVER_SCROLL_NEVER);

        // ✅ ตั้งค่า recyclerView เพื่อลด memory
        recyclerView.setItemViewCacheSize(5); // ลด cache size
        recyclerView.setDrawingCacheEnabled(false);

        // ✅ ใช้ adapter พร้อมส่ง flag memory ต่ำ
        rcAdapter = new AZRecyclerViewAdapter(this, rowListItem, R.layout.item_grid);
        rcAdapter.setIsLowMemoryDevice(isLowMemoryDevice); // เพิ่ม method ใน adapter

        recyclerView.setAdapter(rcAdapter);

        progressBar.setMax(rowListItem.size());
        progressBar.getProgressDrawable().setColorFilter(
                ContextCompat.getColor(this, R.color.color_accent),
                android.graphics.PorterDuff.Mode.SRC_IN);

        rcAdapter.setOnItemClickListener(new AZRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemObject item, int position) {
                playClickSound();
                if (!isAutoReading) {
                    onItemSelected(item);
                }
            }

            @Override
            public void onSoundClick(ItemObject item, int position) {
                playClickSound();
                if (!isAutoReading) {
                    textToSpeech.setPitch(1.8f);
                    textToSpeech.setSpeechRate(0.6f);
                    textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);

                    new Handler().postDelayed(() -> {
                        textToSpeech.setPitch(1.4f);
                        textToSpeech.setSpeechRate(0.7f);
                    }, 1000);
                }
            }
        });
    }

    private void setupAutoReadControls() {
        btnStartAutoRead.setOnClickListener(v -> {
            playClickSound();
            startAutoRead();
        });

        btnStopAutoRead.setOnClickListener(v -> {
            playClickSound();
            stopAutoRead();
        });

        btnToggleMusic.setOnClickListener(v -> {
            playClickSound();
            toggleBackgroundMusic();
        });

        btnStopAutoRead.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
    }

    private void startAutoRead() {
        if (isAutoReading)
            return;
        isAutoReading = true;
        currentAutoReadIndex = 0;

        btnStartAutoRead.setVisibility(View.GONE);
        btnStopAutoRead.setVisibility(View.VISIBLE);
        layoutProgress.setVisibility(View.VISIBLE);

        autoReadRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentAutoReadIndex < rowListItem.size() && isAutoReading) {
                    ItemObject item = rowListItem.get(currentAutoReadIndex);
                    readItemAtIndex(currentAutoReadIndex);
                    currentAutoReadIndex++;

                    if (currentAutoReadIndex < rowListItem.size()) {
                        autoReadHandler.postDelayed(this, AUTO_READ_DELAY);
                    } else {
                        new Handler().postDelayed(() -> {
                            playSuccessSound();
                            Toast.makeText(MenuActivityAZ.this, "เย้! เก่งมาก! เราอ่าน A-Z ครบทุกตัวแล้ว! 🎉",
                                    Toast.LENGTH_LONG).show();
                            stopAutoRead();
                        }, 1000);
                    }
                }
            }
        };
        autoReadHandler.postDelayed(autoReadRunnable, 800);
    }

    private void stopAutoRead() {
        isAutoReading = false;
        autoReadHandler.removeCallbacks(autoReadRunnable);

        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }

        btnStartAutoRead.setVisibility(View.VISIBLE);
        btnStopAutoRead.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
        progressBar.setProgress(0);
        tvProgress.setText("0/" + rowListItem.size());
        rcAdapter.clearHighlight();
    }

    private void readItemAtIndex(int index) {
        progressBar.setProgress(index + 1);
        tvProgress.setText((index + 1) + "/" + rowListItem.size());

        ItemObject item = rowListItem.get(index);
        recyclerView.smoothScrollToPosition(index);
        rcAdapter.setHighlightedPosition(index);

        textToSpeech.setPitch(1.6f);
        textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, "az_" + index);

        new Handler().postDelayed(() -> textToSpeech.setPitch(1.4f), 500);
    }

    private void onItemSelected(ItemObject item) {
        String[] messages = {
                "เก่งมาก! นี่คือตัว " + item.getName() + "!",
                "ว้าว! ตัว " + item.getName() + " สวยจัง!",
                "เย้! คุณพบตัว " + item.getName() + "!",
                "ตัว " + item.getName() + " น่ารักมากเลย!"
        };
        int randomIndex = (int) (Math.random() * messages.length);
        Toast.makeText(this, messages[randomIndex], Toast.LENGTH_SHORT).show();

        textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
    }

    private List<ItemObject> getAllItemList() {
        List<ItemObject> list = new ArrayList<>();
        String[] letters = { "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M", "N", "O", "P", "Q", "R",
                "S", "T", "U", "V", "W", "X", "Y", "Z" };

        // ✅ ใช้ภาพที่เหมาะสมตาม memory
        int[] drawables = getOptimizedDrawables();

        for (int i = 0; i < 26; i++) {
            list.add(new ItemObject(letters[i], drawables[i], i));
        }
        return list;
    }

    // ✅ เพิ่ม: เลือกภาพตามความเหมาะสมของ memory
    private int[] getOptimizedDrawables() {
        if (isLowMemoryDevice) {
            // ใช้ภาพที่มีขนาดเล็กลงสำหรับเครื่อง memory ต่ำ
            return new int[] {
                    R.drawable.e_a, R.drawable.e_b, R.drawable.e_c, R.drawable.e_d,
                    R.drawable.e_e, R.drawable.e_f, R.drawable.e_g, R.drawable.e_h,
                    R.drawable.e_i, R.drawable.e_j, R.drawable.e_k, R.drawable.e_l,
                    R.drawable.e_m, R.drawable.e_n, R.drawable.e_o, R.drawable.e_p,
                    R.drawable.e_q, R.drawable.e_r, R.drawable.e_s, R.drawable.e_t,
                    R.drawable.e_u, R.drawable.e_v, R.drawable.e_w, R.drawable.e_x,
                    R.drawable.e_y, R.drawable.e_z
            };
        } else {
            // ใช้ภาพปกติ
            return new int[] {
                    R.drawable.e_a, R.drawable.e_b, R.drawable.e_c, R.drawable.e_d,
                    R.drawable.e_e, R.drawable.e_f, R.drawable.e_g, R.drawable.e_h,
                    R.drawable.e_i, R.drawable.e_j, R.drawable.e_k, R.drawable.e_l,
                    R.drawable.e_m, R.drawable.e_n, R.drawable.e_o, R.drawable.e_p,
                    R.drawable.e_q, R.drawable.e_r, R.drawable.e_s, R.drawable.e_t,
                    R.drawable.e_u, R.drawable.e_v, R.drawable.e_w, R.drawable.e_x,
                    R.drawable.e_y, R.drawable.e_z
            };
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRead();

        // ✅ เพิ่มการเคลียร์ memory
        clearMemory();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }
        if (clickSound != null)
            clickSound.release();
        if (successSound != null)
            successSound.release();
        if (backgroundMusic != null) {
            // MusicManager manages lifecycle
            backgroundMusic = null;
        }
    }

    // ✅ เพิ่ม: เมธอดเคลียร์ memory
    private void clearMemory() {
        try {
            // เคลียร์ adapter
            if (rcAdapter != null) {
                rcAdapter.onDestroy(); // ต้องมี method นี้ใน adapter
            }

            // เคลียร์ recyclerView
            if (recyclerView != null) {
                recyclerView.setAdapter(null);
                recyclerView.setLayoutManager(null);
                recyclerView.removeAllViews();
            }

            // เคลียร์ list
            if (rowListItem != null) {
                rowListItem.clear();
                rowListItem = null;
            }

            // เรียก garbage collector
            System.gc();

        } catch (Exception e) {
            Log.e("MemoryCleanup", "Error clearing memory: " + e.getMessage());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAutoReading)
            stopAutoRead();
        com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().pauseMusic();
    }

    @Override
    protected void onResume() {
        super.onResume();
        com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().startMusic(this);

        // Update button state
        boolean enabled = com.pongploydev.priyankvex.paintapp.utils.MusicManager.getInstance().isMusicEnabled(this);
        isMusicPlaying = enabled;
        btnToggleMusic.setText(enabled ? "🔊 ปิดเพลง" : "🔇 เปิดเพลง");
    }

    // ✅ เพิ่ม: จัดการเมื่อ memory ต่ำ
    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Log.w("Memory", "Low memory warning!");

        // เคลียร์ cache และ memory
        clearMemory();

        // แจ้งผู้ใช้
        Toast.makeText(this, "กำลังลดการใช้หน่วยความจำ...", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);

        // ✅ จัดการ memory ตาม level
        if (level >= TRIM_MEMORY_BACKGROUND) {
            Log.d("Memory", "Trimming memory, level: " + level);

            if (rcAdapter != null) {
                rcAdapter.onDestroy();
            }

            if (backgroundMusic != null && backgroundMusic.isPlaying()) {
                backgroundMusic.pause();
            }

            System.gc();
        }
    }
}