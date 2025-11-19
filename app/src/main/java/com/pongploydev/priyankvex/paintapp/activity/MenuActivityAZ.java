package com.pongploydev.priyankvex.paintapp.activity;

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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.priyankvex.paintapp.adapter.RecyclerViewAdapter;
import com.pongploydev.priyankvex.paintapp.model.ItemObject;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by PongPloy2016 on 24/1/2560.
 */
public class MenuActivityAZ extends AppCompatActivity {

    private GridLayoutManager lLayout;
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

    private List<ItemObject> rowListItem;
    private RecyclerViewAdapter rcAdapter;

    private static final long AUTO_READ_DELAY = 3000; // 3 seconds between items

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_az); // คุณอาจต้องสร้าง layout ใหม่

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

        // Initially hide stop button and progress
        btnStopAutoRead.setVisibility(View.GONE);
        layoutProgress.setVisibility(View.GONE);
    }

    private void setupTextToSpeech() {
        textToSpeech = new TextToSpeech(this, status -> {
            if (status == TextToSpeech.SUCCESS) {
                // ลองตั้งค่าเป็นภาษาไทยก่อน
                int result = textToSpeech.setLanguage(new Locale("th", "TH"));

                if (result == TextToSpeech.LANG_MISSING_DATA ||
                        result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    Log.e("TTS", "Thai language not supported, trying English");

                    // ถ้าภาษาไทยไม่รองรับ ลองภาษาอังกฤษ
                    int englishResult = textToSpeech.setLanguage(Locale.US);
                    if (englishResult == TextToSpeech.LANG_MISSING_DATA ||
                            englishResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                        Log.e("TTS", "English also not supported, using device default");
                        // ถ้าภาษาอังกฤษก็ไม่รองรับ ใช้ภาษา default ของ device
                        textToSpeech.setLanguage(Locale.getDefault());
                    }
                } else {
                    Log.i("TTS", "Thai language supported");
                }
            } else {
                Log.e("TTS", "Initialization failed");
                Toast.makeText(this, "TTS initialization failed", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupRecyclerView() {
        rowListItem = getAllItemList();
        lLayout = new GridLayoutManager(MenuActivityAZ.this, 4);

        RecyclerView rView = findViewById(R.id.recycler_view);
        rView.setHasFixedSize(true);
        rView.setLayoutManager(lLayout);

        rcAdapter = new RecyclerViewAdapter(MenuActivityAZ.this, rowListItem, 2);
        rView.setAdapter(rcAdapter);

        // Set click listeners for adapter
        rcAdapter.setOnItemClickListener(new RecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(ItemObject item, int position) {
                if (!isAutoReading) {
                    onItemSelected(item, position);
                }
            }

            @Override
            public void onSoundClick(ItemObject item, int position) {
                if (!isAutoReading) {
                    onSoundClicked(item, position);
                }
            }
        });

        // Set progress bar max
        progressBar.setMax(rowListItem.size());
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
                if (currentAutoReadIndex < rowListItem.size() && isAutoReading) {
                    readItemAtIndex(currentAutoReadIndex);
                    currentAutoReadIndex++;

                    // Schedule next item
                    if (isAutoReading && currentAutoReadIndex < rowListItem.size()) {
                        autoReadHandler.postDelayed(this, AUTO_READ_DELAY);
                    } else {
                        // Finished reading all items
                        stopAutoRead();
                        Toast.makeText(MenuActivityAZ.this,
                                "Finished reading all A-Z!", Toast.LENGTH_SHORT).show();
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
        tvProgress.setText("0/" + rowListItem.size());
    }

    private void readItemAtIndex(int index) {
        if (rowListItem == null || index >= rowListItem.size()) return;

        ItemObject item = rowListItem.get(index);
        if (item != null && textToSpeech != null) {
            // Update progress
            progressBar.setProgress(index + 1);
            tvProgress.setText((index + 1) + "/" + rowListItem.size());

            // Speak the item name (letter)
            textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);

            // Optional: Scroll to current item
            scrollToPosition(index);

            // Optional: Highlight current item
            highlightCurrentItem(index);

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

    private void highlightCurrentItem(int position) {
        // You can implement item highlighting here
        // For example, change background color of the current item
        if (rcAdapter != null) {
            rcAdapter.setHighlightedPosition(position);
        }
    }

    private void onItemSelected(ItemObject item, int position) {
        Toast.makeText(this, "Selected: " + item.getName(), Toast.LENGTH_SHORT).show();
        // You can add more actions when item is clicked
    }

    private void onSoundClicked(ItemObject item, int position) {
        // Play sound or use TTS for the letter
        if (textToSpeech != null) {
            textToSpeech.speak(item.getName(), TextToSpeech.QUEUE_FLUSH, null, null);
        }
    }

    private List<ItemObject> getAllItemList(){
        List<ItemObject> allItems = new ArrayList<ItemObject>();
        allItems.add(new ItemObject("A", R.drawable.a,0));
        allItems.add(new ItemObject("B", R.drawable.b,1));
        allItems.add(new ItemObject("C", R.drawable.c,2));
        allItems.add(new ItemObject("D", R.drawable.d,3));
        allItems.add(new ItemObject("E", R.drawable.e,4));
        allItems.add(new ItemObject("F", R.drawable.f,5));
        allItems.add(new ItemObject("G", R.drawable.g,6));
        allItems.add(new ItemObject("H", R.drawable.h,7));
        allItems.add(new ItemObject("I", R.drawable.i,8));
        allItems.add(new ItemObject("J", R.drawable.j,9));

        allItems.add(new ItemObject("K", R.drawable.k,10));
        allItems.add(new ItemObject("L", R.drawable.l,11));
        allItems.add(new ItemObject("M", R.drawable.m,12));
        allItems.add(new ItemObject("N", R.drawable.n,13));
        allItems.add(new ItemObject("O", R.drawable.o,14));
        allItems.add(new ItemObject("P", R.drawable.p,15));
        allItems.add(new ItemObject("Q", R.drawable.q,16));
        allItems.add(new ItemObject("R", R.drawable.r,17));
        allItems.add(new ItemObject("S", R.drawable.s,18));
        allItems.add(new ItemObject("T", R.drawable.t,19));

        allItems.add(new ItemObject("U", R.drawable.u,20));
        allItems.add(new ItemObject("V", R.drawable.v,21));
        allItems.add(new ItemObject("W", R.drawable.w,22));
        allItems.add(new ItemObject("X", R.drawable.x,23));
        allItems.add(new ItemObject("Y", R.drawable.y,24));
        allItems.add(new ItemObject("Z", R.drawable.z,25));

        return allItems;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopAutoRead();

        if (textToSpeech != null) {
            textToSpeech.stop();
            textToSpeech.shutdown();
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