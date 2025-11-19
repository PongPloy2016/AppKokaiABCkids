package com.pongploydev.priyankvex.paintapp.activity.thailetters;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.Handler;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.priyankvex.paintapp.model.MenuItem;
import com.pongploydev.priyankvex.paintapp.module.MenuModule;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.List;
import java.util.Locale;
public class MenuThaiLettersActivity extends AppCompatActivity {

    private MenuModule menuModule;
    private MediaPlayer mediaPlayer;
    private TextToSpeech textToSpeech;

    // Auto read variables
    private Button btnStartAutoRead;
    private Button btnStopAutoRead;
    private ProgressBar progressBar;
    private TextView tvProgress;
    private LinearLayout layoutProgress;

    private boolean isAutoReading = false;
    private int currentAutoReadIndex = 0;
    private Handler autoReadHandler = new Handler();
    private Runnable autoReadRunnable;

    private static final long AUTO_READ_DELAY = 3000; // 3 seconds between items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu);

        setupViews();
        setupAutoReadControls();
        setupTextToSpeech();
        setupRecyclerView();
    }

    private void setupViews() {
        btnStartAutoRead = findViewById(R.id.btn_start_auto_read);
        btnStopAutoRead = findViewById(R.id.btn_stop_auto_read);
        progressBar = findViewById(R.id.progress_bar);
        tvProgress = findViewById(R.id.tv_progress);
        layoutProgress = findViewById(R.id.layout_progress);
    }

    private void setupAutoReadControls() {
        btnStartAutoRead.setOnClickListener(v -> startAutoRead());
        btnStopAutoRead.setOnClickListener(v -> stopAutoRead());
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                int result = textToSpeech.setLanguage(new Locale("th", "TH"));;
                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Language not supported");
                    // Fallback to device default language
                    textToSpeech.setLanguage(Locale.getDefault());
                }
            } else {
                Log.e("TTS", "Initialization failed");
            }
        });
    }

    private void setupRecyclerView() {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);

        // Initialize menu module
        menuModule = new MenuModule(this, recyclerView);

        // Set menu items
        List<MenuItem> menuItems = MenuModule.createDefaultMenuItems(this);
        menuModule.setMenuItems(menuItems);

        // Set progress bar max
        progressBar.setMax(menuItems.size());

        // Set click listeners
        menuModule.setOnItemClickListener(
                // Item click listener
                (item, position) -> {
                    if (!isAutoReading) {
                        onItemSelected(item, position);
                    }
                },
                // Sound click listener
                (item, position) -> {
                    if (!isAutoReading) {
                        onSoundClicked(item, position);
                    }
                }
        );
    }

    private void startAutoRead() {
        if (isAutoReading) return;

        isAutoReading = true;
        currentAutoReadIndex = 0;

        // Update UI
        btnStartAutoRead.setVisibility(View.GONE);
        btnStopAutoRead.setVisibility(View.VISIBLE);
        layoutProgress.setVisibility(View.VISIBLE);

        // Start auto read sequence
        autoReadRunnable = new Runnable() {
            @Override
            public void run() {
                if (currentAutoReadIndex < menuModule.getItemCount() && isAutoReading) {
                    readItemAtIndex(currentAutoReadIndex);
                    currentAutoReadIndex++;

                    // Schedule next item
                    if (isAutoReading && currentAutoReadIndex < menuModule.getItemCount()) {
                        autoReadHandler.postDelayed(this, AUTO_READ_DELAY);
                    } else {
                        // Finished reading all items
                        stopAutoRead();
                        Toast.makeText(MenuThaiLettersActivity.this,
                                "Finished reading all items!", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        };

        autoReadHandler.post(autoReadRunnable);
    }

    private void stopAutoRead() {
        isAutoReading = false;

        // Remove any pending callbacks
        if (autoReadHandler != null && autoReadRunnable != null) {
            autoReadHandler.removeCallbacks(autoReadRunnable);
        }

        // Stop TTS if speaking
        if (textToSpeech != null && textToSpeech.isSpeaking()) {
            textToSpeech.stop();
        }

        // Update UI
        btnStartAutoRead.setVisibility(View.VISIBLE);
        btnStopAutoRead.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);

        // Reset progress
        progressBar.setProgress(0);
        if (menuModule != null) {
            tvProgress.setText("0/" + menuModule.getItemCount());
        }
    }

    private void readItemAtIndex(int index) {
        if (menuModule == null) return;

        MenuItem item = menuModule.getItemAt(index);
        if (item != null && textToSpeech != null) {
            // Update progress
            progressBar.setProgress(index + 1);
            tvProgress.setText((index + 1) + "/" + menuModule.getItemCount());

            // Speak the item name
            textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);

            // Optional: Scroll to current item
            scrollToPosition(index);

            // Optional: Show which item is being read
            Toast.makeText(this, "Reading: " + item.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void scrollToPosition(int position) {
        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        if (recyclerView != null) {
            recyclerView.smoothScrollToPosition(position);
        }
    }

    private void onItemSelected(MenuItem item, int position) {
        Toast.makeText(this, "Selected: " + item.getName(), Toast.LENGTH_SHORT).show();

        Intent intent = new Intent(this, ThaiLettersActivity.class);

        // ส่งข้อมูลที่จำเป็นไปด้วย
        intent.putExtra("SELECTED_POSITION", position);
        intent.putExtra("SELECTED_LETTER_INDEX", position); // หรือใช้ index โดยตรง
        intent.putExtra("SELECTED_LETTER_NAME", item.getName());
        intent.putExtra("SELECTED_ITEM_ID", item.getId());

        // ถ้าต้องการส่งทั้ง object (ต้องทำให้ MenuItem implements Parcelable ก่อน)
        // intent.putExtra("SELECTED_MENU_ITEM", item);

        startActivity(intent);

        // เพิ่ม animation ถ้าต้องการ
        // overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left);
    }

    private void onSoundClicked(MenuItem item, int position) {
        Toast.makeText(this, "เลือก: " + item.getName(), Toast.LENGTH_SHORT).show();

        playSoundForItem(item);
    }

    private void playSoundForItem(MenuItem item) {
        try {
            int soundResId = getSoundResourceId(item);
            if (soundResId != 0) {
                if (mediaPlayer != null) {
                    mediaPlayer.release();
                }
                mediaPlayer = MediaPlayer.create(this, soundResId);
                mediaPlayer.setOnCompletionListener(mp -> {
                    mediaPlayer.release();
                    mediaPlayer = null;
                });
                mediaPlayer.start();
            } else {
                // Fallback to TTS if no sound file
                if (textToSpeech != null) {
                    textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to TTS on error
            if (textToSpeech != null) {
                textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
            }
        }
    }

    private int getSoundResourceId(MenuItem item) {
        String soundName = "soundk" + (item.getId() + 1);
        return getResources().getIdentifier(soundName, "raw", getPackageName());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRead();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        if (autoReadHandler != null) {
            autoReadHandler.removeCallbacksAndMessages(null);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isAutoReading) {
            stopAutoRead();
        }
    }
}