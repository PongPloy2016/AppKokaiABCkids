package com.pongploydev.priyankvex.paintapp.activity.thailetters;

import android.animation.Animator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.HorizontalScrollView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.airbnb.lottie.LottieAnimationView;
import com.google.android.material.card.MaterialCardView;
import com.pongploydev.priyankvex.paintapp.R;
import com.pongploydev.priyankvex.paintapp.activity.VideoStream1;
import com.pongploydev.priyankvex.paintapp.activity.VideoStream2;
import com.pongploydev.priyankvex.paintapp.confixdata.File_Confix_Data;
import com.pongploydev.priyankvex.paintapp.drawingView.DrawingView;
import com.pongploydev.priyankvex.paintapp.model.MenuItem;

import java.util.ArrayList;
import java.util.List;

public class ThaiLettersActivity extends AppCompatActivity implements View.OnClickListener, DrawingView.DrawingCheckListener {
    private static final String TAG = "ThaiLettersActivity";

    private DrawingView mDrawingView;
    private MaterialCardView currPaintCard;
    private ImageButton currPaintButton;
    private MediaPlayer playimgButton;
    private MediaPlayer playimgS;

    // Constants
    private static final String KEY_VIDEO = "key_2";

    // Brush sizes
    private float smallBrush, mediumBrush, largeBrush;

    // ImageButton references
    private ImageButton drawButton, eraseButton, newButton, saveButton, checkButton;
    private ImageButton btnPrevious, btnNext, btnVideo, btnSound;

    // ตัวแปรสำหรับระบบตรวจสอบ
    private LottieAnimationView animationSuccess, animationTryAgain;
    private CardView cardProgress;
    private TextView textScore, textProgress;
    private ImageView star1, star2, star3;

    private int currentScore = 0;
    private int totalAttempts = 0;
    private int successfulAttempts = 0;
    private boolean isCheckingMode = false;

    // ตัวแปรสำหรับระบบตัวอักษรไทย
    private String[] thaiLetters;
    private String[] thaiLetterNames;
    private int currentLetterIndex = 0;

    // ตัวแปร UI ใหม่
    private TextView textCurrentLetter, textLetterName;
    private ImageButton btnPrevLetter, btnNextLetter;
    private LinearLayout letterSelectionLayout;
    private HorizontalScrollView lettersScrollView;

    // ตัวแปรสำหรับรับข้อมูลจาก Intent
    private MenuItem selectedMenuItem;
    private int selectedPosition = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // ซ่อน ActionBar
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        setContentView(R.layout.activity_main2);

        // รับข้อมูลจาก Intent ก่อน
        handleIntentData();

        initializeThaiLetters();
        setupUI();
        setupClickListeners();
        setupCheckingSystem();
        setupLetterSelection();
    }

    private void handleIntentData() {
        Intent intent = getIntent();
        if (intent != null) {
            // รับข้อมูลจาก MenuThaiLettersActivity
            selectedPosition = intent.getIntExtra("SELECTED_POSITION", 0);
            currentLetterIndex = intent.getIntExtra("SELECTED_LETTER_INDEX", 0);

            // รับข้อมูลเพิ่มเติมถ้ามี
            String selectedLetterName = intent.getStringExtra("SELECTED_LETTER_NAME");
            int selectedItemId = intent.getIntExtra("SELECTED_ITEM_ID", 0);

            Log.d(TAG, "Received intent data - Position: " + selectedPosition +
                    ", LetterIndex: " + currentLetterIndex +
                    ", LetterName: " + selectedLetterName +
                    ", ItemId: " + selectedItemId);

            // รับ MenuItem object ถ้าส่งมา (ต้องทำให้ MenuItem implements Parcelable ก่อน)
            if (intent.hasExtra("SELECTED_MENU_ITEM")) {
                selectedMenuItem = intent.getParcelableExtra("SELECTED_MENU_ITEM");
                if (selectedMenuItem != null) {
                    Log.d(TAG, "Received MenuItem: " + selectedMenuItem.getName());
                }
            }

            // ตรวจสอบขอบเขตของ index
            if (currentLetterIndex < 0) {
                currentLetterIndex = 0;
            }
        }

        Log.d(TAG, "Final currentLetterIndex: " + currentLetterIndex);
    }

    private void initializeThaiLetters() {
        thaiLetters = new String[]{
                "ก", "ข", "ฃ", "ค", "ฅ", "ฆ", "ง", "จ", "ฉ", "ช", "ซ", "ฌ", "ญ", "ฎ", "ฏ",
                "ฐ", "ฑ", "ฒ", "ณ", "ด", "ต", "ถ", "ท", "ธ", "น", "บ", "ป", "ผ", "ฝ", "พ",
                "ฟ", "ภ", "ม", "ย", "ร", "ล", "ว", "ศ", "ษ", "ส", "ห", "ฬ", "อ", "ฮ"
        };

        thaiLetterNames = new String[]{
                "ก ไก่", "ข ไข่", "ฃ ขวด", "ค ควาย", "ฅ คน", "ฆ ระฆัง", "ง งู", "จ จาน", "ฉ ฉิ่ง", "ช ช้าง",
                "ซ โซ่", "ฌ เฌอ", "ญ หญิง", "ฎ ชฎา", "ฏ ปฏัก", "ฐ ฐาน", "ฑ มณโฑ", "ฒ ผู้เฒ่า", "ณ เณร",
                "ด เด็ก", "ต เต่า", "ถ ถุง", "ท ทหาร", "ธ ธง", "น หนู", "บ ใบไม้", "ป ปลา", "ผ ผึ้ง",
                "ฝ ฝา", "พ พาน", "ฟ ฟัน", "ภ สำเภา", "ม ม้า", "ย ยักษ์", "ร เรือ", "ล ลิง", "ว แหวน",
                "ศ ศาลา", "ษ ฤๅษี", "ส เสือ", "ห หีบ", "ฬ จุฬา", "อ อ่าง", "ฮ นกฮูก"
        };

        // ตรวจสอบขอบเขตอีกครั้งหลังจาก initialize
        if (currentLetterIndex >= thaiLetters.length) {
            currentLetterIndex = thaiLetters.length - 1;
        }
        if (currentLetterIndex < 0) {
            currentLetterIndex = 0;
        }
    }

    private void setupLetterSelection() {
        // อ้างอิงถึง View ใหม่
        textCurrentLetter = findViewById(R.id.textCurrentLetter);
        textLetterName = findViewById(R.id.textLetterName);
        btnPrevLetter = findViewById(R.id.btnPrevLetter);
        btnNextLetter = findViewById(R.id.btnNextLetter);
        letterSelectionLayout = findViewById(R.id.letter_selection);
        lettersScrollView = findViewById(R.id.lettersScrollView);

        // ตั้งค่า Click Listener
        btnPrevLetter.setOnClickListener(this);
        btnNextLetter.setOnClickListener(this);

        // สร้างการ์ดตัวอักษรทั้งหมด
        createLetterCards();

        // เลือกตัวอักษรตามข้อมูลจาก Intent
        updateCurrentLetterDisplay();
        scrollToCurrentLetter();
    }


    private void createLetterCards() {
        letterSelectionLayout.removeAllViews();

        for (int i = 0; i < thaiLetters.length; i++) {
            MaterialCardView card = createLetterCard(i);
            letterSelectionLayout.addView(card);
        }
    }

    private MaterialCardView createLetterCard(int index) {
        // สร้าง LayoutParams
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(
                dpToPx(60), dpToPx(60)
        );
        layoutParams.setMargins(dpToPx(4), dpToPx(4), dpToPx(4), dpToPx(4));

        // สร้าง MaterialCardView
        MaterialCardView card = new MaterialCardView(this);
        card.setLayoutParams(layoutParams);
        card.setCardElevation(dpToPx(8));
        card.setStrokeWidth(dpToPx(1));
        card.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gray_light));
        card.setTag(index);
        card.setOnClickListener(this);

        // สร้าง LinearLayout ภายใน
        LinearLayout innerLayout = new LinearLayout(this);
        innerLayout.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
        ));
        innerLayout.setOrientation(LinearLayout.VERTICAL);
        innerLayout.setGravity(Gravity.CENTER);
        innerLayout.setBackground(ContextCompat.getDrawable(this, R.drawable.ripple_effect));

        // สร้าง TextView สำหรับตัวอักษร
        TextView letterText = new TextView(this);
        letterText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        letterText.setText(thaiLetters[index]);
        letterText.setTextSize(18);
        letterText.setTextColor(ContextCompat.getColor(this, R.color.black));
        letterText.setTypeface(letterText.getTypeface(), Typeface.BOLD);

        // สร้าง TextView สำหรับชื่อตัวอักษร
        TextView nameText = new TextView(this);
        nameText.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        ));
        nameText.setText(thaiLetterNames[index].split(" ")[1]); // แสดงเฉพาะชื่อสัตว์/สิ่งของ
        nameText.setTextSize(8);
        nameText.setTextColor(ContextCompat.getColor(this, R.color.gray_dark));

        // เพิ่ม TextView ลงใน Layout
        innerLayout.addView(letterText);
        innerLayout.addView(nameText);

        // เพิ่ม Inner Layout ลงใน Card
        card.addView(innerLayout);

        return card;
    }

    private int dpToPx(int dp) {
        return (int) (dp * getResources().getDisplayMetrics().density);
    }

    private void updateCurrentLetterDisplay() {
        if (textCurrentLetter != null) {
            textCurrentLetter.setText(thaiLetters[currentLetterIndex]);
        }
        if (textLetterName != null) {
            textLetterName.setText(thaiLetterNames[currentLetterIndex]);
        }

        // อัพเดตการเลือกในการ์ดทั้งหมด
        updateAllLetterCards();

        // อัพเดตภาพพื้นหลังและเส้นนำทาง
        updateDrawingBackground();
        createGuidePathForLetter(currentLetterIndex);

        // รีเซ็ตพื้นที่วาด
        mDrawingView.startNew();

        // รีเซ็ตสถิติ
        resetStatsForNewLetter();

        // เล่นเสียงตัวอักษร
        playLetterSound();
    }

    private void updateAllLetterCards() {
        for (int i = 0; i < letterSelectionLayout.getChildCount(); i++) {
            View child = letterSelectionLayout.getChildAt(i);
            if (child instanceof MaterialCardView) {
                MaterialCardView card = (MaterialCardView) child;
                int cardIndex = (int) card.getTag();

                if (cardIndex == currentLetterIndex) {
                    // การ์ดที่ถูกเลือก
                    card.setStrokeColor(ContextCompat.getColor(this, R.color.purple));
                    card.setStrokeWidth(dpToPx(3));
                    card.setCardElevation(dpToPx(4));

                    // เปลี่ยนสีตัวอักษร
                    TextView letterText = findTextViewInCard(card, 0);
                    if (letterText != null) {
                        letterText.setTextColor(ContextCompat.getColor(this, R.color.purple));
                    }
                } else {
                    // การ์ดที่ไม่ถูกเลือก
                    card.setStrokeColor(ContextCompat.getColor(this, R.color.gray_light));
                    card.setStrokeWidth(dpToPx(1));
                    card.setCardElevation(dpToPx(2));

                    // รีเซ็ตสีตัวอักษร
                    TextView letterText = findTextViewInCard(card, 0);
                    if (letterText != null) {
                        letterText.setTextColor(ContextCompat.getColor(this, R.color.black));
                    }
                }
            }
        }
    }

    private TextView findTextViewInCard(ViewGroup cardView, int index) {
        int textViewCount = 0;
        for (int i = 0; i < cardView.getChildCount(); i++) {
            View child = cardView.getChildAt(i);
            if (child instanceof LinearLayout) {
                LinearLayout layout = (LinearLayout) child;
                for (int j = 0; j < layout.getChildCount(); j++) {
                    View innerChild = layout.getChildAt(j);
                    if (innerChild instanceof TextView) {
                        if (textViewCount == index) {
                            return (TextView) innerChild;
                        }
                        textViewCount++;
                    }
                }
            }
        }
        return null;
    }

    private void scrollToCurrentLetter() {
        lettersScrollView.post(() -> {
            View selectedCard = letterSelectionLayout.getChildAt(currentLetterIndex);
            if (selectedCard != null) {
                int scrollX = selectedCard.getLeft() - (lettersScrollView.getWidth() - selectedCard.getWidth()) / 2;
                lettersScrollView.smoothScrollTo(scrollX, 0);
            }
        });
    }

    private void navigateToPreviousLetter() {
        if (currentLetterIndex > 0) {
            currentLetterIndex--;
            updateCurrentLetterDisplay();
            scrollToCurrentLetter();
            playSoundEffect();
        } else {
            showToast("นี่คือตัวอักษรแรกแล้วครับ");
        }
    }

    private void navigateToNextLetter() {
        if (currentLetterIndex < thaiLetters.length - 1) {
            currentLetterIndex++;
            updateCurrentLetterDisplay();
            scrollToCurrentLetter();
            playSoundEffect();
        } else {
            showToast("นี่คือตัวอักษรสุดท้ายแล้วครับ");
        }
    }

    private void playLetterSound() {
        // เล่นเสียงตัวอักษรเมื่อเปลี่ยนตัวอักษร
        if (isValidSoundIndex(currentLetterIndex)) {
            try {
                if (playimgS != null) {
                    playimgS.release();
                    playimgS = null;
                }

                int soundResource = File_Confix_Data.sound1[currentLetterIndex];
                playimgS = MediaPlayer.create(this, soundResource);

                if (playimgS != null) {
                    playimgS.setOnCompletionListener(mp -> {
                        mp.release();
                        playimgS = null;
                    });
                    playimgS.start();
                }
            } catch (Exception e) {
                Log.e(TAG, "Error playing letter sound", e);
            }
        }
    }

    private void setupUI() {
        initializeMediaPlayer();
        setupDrawingView();
        setupPaintColors();
        setupToolButtons();
        setupBrushSizes();
        setupProgressUI();
    }

    private void initializeMediaPlayer() {
        try {
            playimgButton = MediaPlayer.create(this, R.raw.button);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing media player", e);
        }
    }

    private void setupDrawingView() {
        mDrawingView = findViewById(R.id.drawing);
        mDrawingView.setDrawingCheckListener(this);
        updateDrawingBackground();
    }

    private void updateDrawingBackground() {
        if (isValidImageIndex(currentLetterIndex)) {
            try {
                int drawableId = File_Confix_Data.image_confixe1[currentLetterIndex];
                Drawable myDrawable = ContextCompat.getDrawable(this, drawableId);
                if (myDrawable != null) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mDrawingView.setBackground(myDrawable);
                    } else {
                        mDrawingView.setBackgroundDrawable(myDrawable);
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting background", e);
                showToast("Error loading image");
            }
        } else {
            Log.e(TAG, "Invalid image index: " + currentLetterIndex);
            showToast("Invalid image index");
        }
    }

    private boolean isValidImageIndex(int index) {
        return index >= 0 && index < File_Confix_Data.image_confixe1.length;
    }

    private boolean isValidSoundIndex(int index) {
        return index >= 0 && index < File_Confix_Data.sound1.length;
    }

    private void setupPaintColors() {
        LinearLayout paintLayout = findViewById(R.id.paint_colors);
        if (paintLayout != null && paintLayout.getChildCount() > 1) {
            View firstColorView = paintLayout.getChildAt(1);
            if (firstColorView instanceof MaterialCardView) {
                currPaintCard = (MaterialCardView) firstColorView;

                ImageButton imageButton = findImageButtonInCardView(currPaintCard);
                if (imageButton != null) {
                    currPaintButton = imageButton;
                    currPaintButton.setImageResource(R.drawable.pallet_selected);

                    currPaintCard.setStrokeColor(ContextCompat.getColorStateList(this, R.color.purple));
                    currPaintCard.setStrokeWidth(2);
                }
            }
        }
    }

    private ImageButton findImageButtonInCardView(ViewGroup cardView) {
        for (int i = 0; i < cardView.getChildCount(); i++) {
            View child = cardView.getChildAt(i);
            if (child instanceof ImageButton) {
                return (ImageButton) child;
            }
        }
        return null;
    }

    private void setupToolButtons() {
        // Initialize tool buttons
        drawButton = findViewById(R.id.buttonBrush);
        eraseButton = findViewById(R.id.buttonErase);
        newButton = findViewById(R.id.buttonNew);
        saveButton = findViewById(R.id.buttonSave);
        checkButton = findViewById(R.id.buttonCheck);

        // Set click listeners
        if (drawButton != null) drawButton.setOnClickListener(this);
        if (eraseButton != null) eraseButton.setOnClickListener(this);
        if (newButton != null) newButton.setOnClickListener(this);
        if (saveButton != null) saveButton.setOnClickListener(this);
        if (checkButton != null) checkButton.setOnClickListener(this);
    }

    private void setupBrushSizes() {
        smallBrush = getResources().getInteger(R.integer.small_size);
        mediumBrush = getResources().getInteger(R.integer.medium_size);
        largeBrush = getResources().getInteger(R.integer.large_size);

        mDrawingView.setBrushSize(mediumBrush);
    }

    private void setupProgressUI() {
        // อ้างอิงถึงองค์ประกอบ UI สำหรับแสดงความคืบหน้า
        cardProgress = findViewById(R.id.cardProgress);
        textScore = findViewById(R.id.textScore);
        textProgress = findViewById(R.id.textProgress);
        star1 = findViewById(R.id.star1);
        star2 = findViewById(R.id.star2);
        star3 = findViewById(R.id.star3);

        animationSuccess = findViewById(R.id.animationSuccess);
        animationTryAgain = findViewById(R.id.animationTryAgain);

        updateProgressUI();
    }

    private void setupClickListeners() {
        setupNavigationButtons();
        setupMediaButtons();
    }

    private void setupNavigationButtons() {
        btnPrevious = findViewById(R.id.button_black);
        btnNext = findViewById(R.id.button_next);

        if (btnPrevious != null) {
            btnPrevious.setOnClickListener(v -> navigateToPreviousLetter());
        }

        if (btnNext != null) {
            btnNext.setOnClickListener(v -> navigateToNextLetter());
        }
    }

    private void setupMediaButtons() {
        btnVideo = findViewById(R.id.buttonVDO);
        btnSound = findViewById(R.id.buttonsound);

        if (btnVideo != null) {
            btnVideo.setOnClickListener(v -> openVideoActivity());
        }

        if (btnSound != null) {
            btnSound.setOnClickListener(v -> playSound());
        }
    }

    private void setupCheckingSystem() {
        // ตั้งค่าระบบตรวจสอบเริ่มต้น
        mDrawingView.setCheckingEnabled(false);
        updateCheckButtonAppearance();
    }

    private void openVideoActivity() {
        playSoundEffect();
        try {
            Intent intent = new Intent(this, VideoStream1.class);
            intent.putExtra(KEY_VIDEO, currentLetterIndex);
            startActivity(intent);
        } catch (Exception e) {
            Log.e(TAG, "Error opening video activity", e);
            showToast("Cannot open video");
        }
    }

    private void playSound() {
        if (!isValidSoundIndex(currentLetterIndex)) {
            showToast("ไม่พบไฟล์เสียง");
            return;
        }

        try {
            if (playimgS != null) {
                playimgS.release();
                playimgS = null;
            }

            int soundResource = File_Confix_Data.sound1[currentLetterIndex];
            playimgS = MediaPlayer.create(this, soundResource);

            if (playimgS != null) {
                playimgS.setOnCompletionListener(mp -> {
                    mp.release();
                    playimgS = null;
                });
                playimgS.setOnErrorListener((mp, what, extra) -> {
                    Log.e(TAG, "MediaPlayer error: " + what + ", " + extra);
                    playimgS.release();
                    playimgS = null;
                    showToast("Error playing sound");
                    return true;
                });
                playimgS.start();
            } else {
                showToast("ไม่สามารถเล่นเสียงได้");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound", e);
            showToast("ไม่สามารถเล่นเสียงได้");
        }
    }

    private void playSoundEffect() {
        try {
            if (playimgButton != null) {
                if (playimgButton.isPlaying()) {
                    playimgButton.seekTo(0);
                } else {
                    playimgButton.start();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error playing sound effect", e);
        }
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    public void paintClicked(View view) {
        MaterialCardView clickedCard = null;
        ImageButton clickedButton = null;

        if (view instanceof MaterialCardView) {
            clickedCard = (MaterialCardView) view;
            clickedButton = findImageButtonInCardView(clickedCard);
        } else if (view instanceof ImageButton) {
            clickedButton = (ImageButton) view;
            ViewParent parent = view.getParent();
            if (parent instanceof MaterialCardView) {
                clickedCard = (MaterialCardView) parent;
            }
        }

        if (clickedCard == null || clickedButton == null) {
            return;
        }

        if (clickedCard != currPaintCard) {
            Object tag = clickedButton.getTag();
            if (tag != null) {
                mDrawingView.setColor(tag.toString());
            }

            clickedButton.setImageResource(R.drawable.pallet_selected);
            clickedCard.setStrokeColor(ContextCompat.getColorStateList(this, R.color.purple));
            clickedCard.setStrokeWidth(2);

            if (currPaintCard != null && currPaintButton != null) {
                currPaintButton.setImageResource(R.drawable.pallet);
                currPaintCard.setStrokeColor(ContextCompat.getColorStateList(this, R.color.gray_light));
                currPaintCard.setStrokeWidth(1);
            }

            currPaintCard = clickedCard;
            currPaintButton = clickedButton;
            mDrawingView.setErase(false);
            mDrawingView.setBrushSize(mDrawingView.getLastBrushSize());
        }
    }

    @Override
    public void onClick(View v) {
        if (v == null) return;

        playSoundEffect();
        int id = v.getId();

        if (id == R.id.buttonBrush) {
            showBrushSizeChooserDialog();
        } else if (id == R.id.buttonErase) {
            showEraserSizeChooserDialog();
        } else if (id == R.id.buttonNew) {
            showNewPaintingAlertDialog();
        } else if (id == R.id.buttonSave) {
            showSavePaintingConfirmationDialog();
        } else if (id == R.id.buttonCheck) {
            toggleCheckingMode();
        } else if (id == R.id.btnPrevLetter) {
            navigateToPreviousLetter();
        } else if (id == R.id.btnNextLetter) {
            navigateToNextLetter();
        } else if (v instanceof MaterialCardView) {
            // เมื่อคลิกที่การ์ดตัวอักษร
            int clickedIndex = (int) v.getTag();
            if (clickedIndex != currentLetterIndex) {
                currentLetterIndex = clickedIndex;
                updateCurrentLetterDisplay();
                scrollToCurrentLetter();
            }
        }
    }

    private void resetStatsForNewLetter() {
        currentScore = 0;
        totalAttempts = 0;
        successfulAttempts = 0;
        updateProgressUI();
    }

    // เมธอดใหม่สำหรับเปิด/ปิดโหมดตรวจสอบ
    private void toggleCheckingMode() {
        isCheckingMode = !isCheckingMode;
        mDrawingView.setCheckingEnabled(isCheckingMode);
        updateCheckButtonAppearance();

        if (isCheckingMode) {
            showToast("เปิดโหมดตรวจสอบการลากเส้น ✅");
            mDrawingView.invalidate(); // รีเฟรชเพื่อแสดงเส้นนำทาง
        } else {
            showToast("ปิดโหมดตรวจสอบการลากเส้น ⬜");
        }
    }

    private void updateCheckButtonAppearance() {
        if (checkButton != null) {
            if (isCheckingMode) {
                checkButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.green));
                checkButton.setColorFilter(ContextCompat.getColor(this, R.color.white));
            } else {
                checkButton.setBackgroundTintList(ContextCompat.getColorStateList(this, R.color.gray_light));
                checkButton.setColorFilter(ContextCompat.getColor(this, R.color.black));
            }
        }
    }

    // Implement methods from DrawingCheckListener
    @Override
    public void onDrawingChecked(boolean isCorrect, float accuracy) {
        runOnUiThread(() -> {
            totalAttempts++;
            if (isCorrect) {
                successfulAttempts++;
                currentScore += (int)(accuracy * 20); // คะแนนตามความแม่นยำ
                showSuccessAnimation();

                if (accuracy > 0.9f) {
                    showToast("สมบูรณ์แบบ! 💯");
                    awardStars(3);
                } else if (accuracy > 0.7f) {
                    showToast("ดีมาก! 👍");
                    awardStars(2);
                } else {
                    showToast("ถูกต้อง! 👏");
                    awardStars(1);
                }
            } else {
                showTryAgainAnimation();
                if (accuracy > 0.5f) {
                    showToast("ใกล้เคียงแล้ว! ลองใหม่อีกครั้ง ✏️");
                } else {
                    showToast("ลองใหม่อีกครั้ง ลากเส้นให้ตรงตามแบบ 📝");
                }
            }

            updateProgressUI();
            showAccuracyDialog(accuracy, isCorrect);
        });
    }

    @Override
    public void onDrawingProgress(float progress) {
        // สามารถใช้สำหรับอัพเดต progress แบบ real-time ได้
        runOnUiThread(() -> {
            if (textProgress != null) {
                textProgress.setText(String.format("%.0f%%", progress * 100));
            }
        });
    }

    @Override
    public void onStrokeCompleted(int strokeIndex, boolean isCorrect) {
        runOnUiThread(() -> {
            if (isCorrect) {
                showToast("เส้นที่ " + (strokeIndex + 1) + " ถูกต้อง! ✓");
            } else {
                showToast("เส้นที่ " + (strokeIndex + 1) + " ยังไม่ถูกต้อง ✗");
            }
        });
    }

    @Override
    public void onLetterCompleted(boolean isPerfect) {
        runOnUiThread(() -> {
            if (isPerfect) {
                showToast("ยินดีด้วย! คุณเขียนตัว " + thaiLetters[currentLetterIndex] + " สำเร็จแล้ว! 🎉");
                currentScore += 50; // โบนัสเมื่อเขียนเสร็จสมบูรณ์
                updateProgressUI();

                // แสดง dialog สำเร็จ
                showCompletionDialog();
            }
        });
    }

    private void showCompletionDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("สำเร็จ! 🎉")
                .setMessage("คุณเขียนตัวอักษร " + thaiLetters[currentLetterIndex] + " สำเร็จแล้ว!\n\n" +
                        "คะแนนที่ได้: " + currentScore + " คะแนน\n" +
                        "ความแม่นยำ: สูงมาก!")
                .setPositiveButton("เขียนอีกครั้ง", (dialog, which) -> {
                    mDrawingView.startNew();
                })
                .setNegativeButton("ตัวต่อไป", (dialog, which) -> {
                    navigateToNextLetter();
                })
                .setNeutralButton("อยู่ต่อ", null)
                .show();
    }

    private void awardStars(int count) {
        // อัพเดตดาวตามจำนวนที่ได้
        updateStars(count);
    }

    private void showSuccessAnimation() {
        if (animationSuccess != null) {
            animationSuccess.setVisibility(View.VISIBLE);
            animationSuccess.playAnimation();

            animationSuccess.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationSuccess.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }
    }

    private void showTryAgainAnimation() {
        if (animationTryAgain != null) {
            animationTryAgain.setVisibility(View.VISIBLE);
            animationTryAgain.playAnimation();

            animationTryAgain.addAnimatorListener(new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {}

                @Override
                public void onAnimationEnd(Animator animation) {
                    animationTryAgain.setVisibility(View.GONE);
                }

                @Override
                public void onAnimationCancel(Animator animation) {}

                @Override
                public void onAnimationRepeat(Animator animation) {}
            });
        }
    }

    private void updateProgressUI() {
        if (textScore != null) {
            textScore.setText(String.valueOf(currentScore));
        }

        if (textProgress != null) {
            int progress = totalAttempts > 0 ? (successfulAttempts * 100) / totalAttempts : 0;
            textProgress.setText(progress + "%");
        }
    }

    private void updateStars(int starCount) {
        // อัพเดตภาพดาวตามจำนวน
        if (star1 != null) {
            star1.setImageResource(starCount >= 1 ? R.drawable.star : R.drawable.s_sun);
        }
        if (star2 != null) {
            star2.setImageResource(starCount >= 2 ? R.drawable.star : R.drawable.s_sun);
        }
        if (star3 != null) {
            star3.setImageResource(starCount >= 3 ? R.drawable.star : R.drawable.s_sun);
        }
    }

    private void showAccuracyDialog(float accuracy, boolean isCorrect) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(isCorrect ? "สำเร็จ! 🎉" : "ลองใหม่ 🔄");

        // สร้าง View สำหรับ dialog
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 30, 50, 30);

        TextView messageText = new TextView(this);
        messageText.setText(isCorrect ?
                "คุณลากเส้นได้ถูกต้อง!" :
                "พยายามอีกนิด ลากเส้นให้ตรงตามแบบมากขึ้น");
        messageText.setTextSize(16);
        messageText.setPadding(0, 0, 0, 20);

        TextView accuracyText = new TextView(this);
        accuracyText.setText("ความแม่นยำ: " + String.format("%.0f", accuracy * 100) + "%");
        accuracyText.setTextSize(18);
        accuracyText.setTextColor(isCorrect ? Color.GREEN : Color.RED);
        accuracyText.setPadding(0, 0, 0, 10);

        layout.addView(messageText);
        layout.addView(accuracyText);

        builder.setView(layout);
        builder.setPositiveButton("ตกลง", null);

        if (isCorrect && accuracy > 0.8f) {
            builder.setNeutralButton("ลองตัวต่อไป", (dialog, which) -> {
                navigateToNextLetter();
            });
        }

        builder.show();
    }

    private void showBrushSizeChooserDialog() {
        showSizeChooserDialog("เลือกขนาดแปรง", false);
    }

    private void showEraserSizeChooserDialog() {
        showSizeChooserDialog("เลือกขนาดยางลบ", true);
    }

    private void showSizeChooserDialog(String title, boolean isEraser) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_brush_size, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();
        setupBrushSizeButtons(dialogView, dialog, isEraser);
        dialog.show();
    }

    private void setupBrushSizeButtons(View dialogView, AlertDialog dialog, boolean isEraser) {
        int[] buttonIds = {R.id.small_brush, R.id.medium_brush, R.id.large_brush};
        float[] sizes = {smallBrush, mediumBrush, largeBrush};

        for (int i = 0; i < buttonIds.length; i++) {
            ImageButton button = dialogView.findViewById(buttonIds[i]);
            if (button != null) {
                final float size = sizes[i];
                button.setOnClickListener(v -> {
                    if (isEraser) {
                        mDrawingView.setErase(true);
                        mDrawingView.setBrushSize(size);
                    } else {
                        mDrawingView.setBrushSize(size);
                        mDrawingView.setLastBrushSize(size);
                        mDrawingView.setErase(false);
                    }
                    dialog.dismiss();
                });
            }
        }
    }

    private void showNewPaintingAlertDialog() {
        new AlertDialog.Builder(this)
                .setTitle("ภาพใหม่")
                .setMessage("เริ่มภาพใหม่? (ภาพปัจจุบันจะหายไป)")
                .setPositiveButton("ตกลง", (dialog, which) -> {
                    mDrawingView.startNew();
                    // รีเซ็ตสถิติสำหรับภาพใหม่
                    totalAttempts = 0;
                    successfulAttempts = 0;
                    updateProgressUI();
                })
                .setNegativeButton("ยกเลิก", null)
                .setCancelable(true)
                .show();
    }

    private void showSavePaintingConfirmationDialog() {
        new AlertDialog.Builder(this)
                .setTitle("บันทึกภาพ")
                .setMessage("บันทึกลง Gallery?")
                .setPositiveButton("บันทึก", (dialog, which) -> saveDrawingToGallery())
                .setNegativeButton("ยกเลิก", null)
                .setCancelable(true)
                .show();
    }

    private void saveDrawingToGallery() {
        if (mDrawingView == null) {
            showToast("Drawing view not available");
            return;
        }

        mDrawingView.setDrawingCacheEnabled(true);
        try {
            Bitmap drawingCache = mDrawingView.getDrawingCache();
            if (drawingCache != null) {
                String imgSaved = MediaStore.Images.Media.insertImage(
                        getContentResolver(),
                        drawingCache,
                        "drawing_" + System.currentTimeMillis() + ".png",
                        "drawing"
                );

                if (imgSaved != null) {
                    showToast("บันทึกภาพสำเร็จ!");
                    sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                            Uri.parse("file://" + imgSaved)));
                } else {
                    showToast("บันทึกภาพไม่สำเร็จ");
                }
            } else {
                showToast("ไม่พบภาพที่จะบันทึก");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error saving image", e);
            showToast("เกิดข้อผิดพลาดในการบันทึก");
        } finally {
            mDrawingView.destroyDrawingCache();
        }
    }

    // เมธอดสำหรับสร้างเส้นนำทางตามตัวอักษร
    private void createGuidePathForLetter(int letterIndex) {
        Path guidePath = new Path();

        // ตัวอย่างการสร้างเส้นนำทางพื้นฐาน
        createBasicGuidePath(guidePath, letterIndex);

        mDrawingView.setGuidePath(guidePath);

        // สร้างเส้นขีดต่างๆ สำหรับตัวอักษรที่ซับซ้อน
        List<Path> strokePaths = createStrokePathsForLetter(letterIndex);
        mDrawingView.setStrokePaths(strokePaths);
    }

    private void createBasicGuidePath(Path path, int letterIndex) {
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int centerX = width / 2;
        int centerY = height / 2;
        int size = Math.min(width, height) / 3;

        path.reset();

        // สร้างเส้นนำทางพื้นฐานตามประเภทของตัวอักษร
        switch (letterIndex % 5) {
            case 0: // ตัวอักษรกลุ่มเส้นโค้ง
                path.addCircle(centerX, centerY, size * 0.4f, Path.Direction.CW);
                break;
            case 1: // ตัวอักษรกลุ่มเส้นตรง
                path.moveTo(centerX - size/2, centerY - size/2);
                path.lineTo(centerX + size/2, centerY + size/2);
                break;
            case 2: // ตัวอักษรกลุ่มเส้นผสม
                path.moveTo(centerX - size/2, centerY);
                path.lineTo(centerX + size/2, centerY);
                path.moveTo(centerX, centerY - size/2);
                path.lineTo(centerX, centerY + size/2);
                break;
            case 3: // ตัวอักษรกลุ่มเส้นโค้งผสม
                path.moveTo(centerX - size/3, centerY);
                path.quadTo(centerX, centerY - size/3, centerX + size/3, centerY);
                break;
            case 4: // ตัวอักษรกลุ่มเส้นซับซ้อน
                path.moveTo(centerX - size/2, centerY - size/4);
                path.lineTo(centerX, centerY - size/2);
                path.lineTo(centerX + size/2, centerY - size/4);
                path.lineTo(centerX + size/3, centerY + size/2);
                path.lineTo(centerX - size/3, centerY + size/2);
                path.close();
                break;
        }
    }

    private List<Path> createStrokePathsForLetter(int letterIndex) {
        List<Path> strokes = new ArrayList<>();
        int width = getResources().getDisplayMetrics().widthPixels;
        int height = getResources().getDisplayMetrics().heightPixels;
        int centerX = width / 2;
        int centerY = height / 2;
        int size = Math.min(width, height) / 3;

        // ตัวอย่างการสร้างเส้นขีดสำหรับตัวอักษรง่ายๆ
        switch (letterIndex) {
            case 0: // ก
                Path stroke1 = new Path();
                stroke1.moveTo(centerX - size/3, centerY);
                stroke1.quadTo(centerX, centerY - size/3, centerX + size/3, centerY);
                strokes.add(stroke1);

                Path stroke2 = new Path();
                stroke2.moveTo(centerX + size/3, centerY);
                stroke2.lineTo(centerX + size/3, centerY + size/2);
                strokes.add(stroke2);
                break;

            case 1: // ข
                Path stroke1b = new Path();
                stroke1b.moveTo(centerX - size/4, centerY - size/4);
                stroke1b.lineTo(centerX + size/4, centerY - size/4);
                strokes.add(stroke1b);

                Path stroke2b = new Path();
                stroke2b.moveTo(centerX, centerY - size/4);
                stroke2b.lineTo(centerX, centerY + size/2);
                strokes.add(stroke2b);
                break;

            default:
                // เส้นขีดพื้นฐานสำหรับตัวอักษรอื่นๆ
                Path defaultStroke = new Path();
                defaultStroke.addCircle(centerX, centerY, size * 0.3f, Path.Direction.CW);
                strokes.add(defaultStroke);
                break;
        }

        return strokes;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releaseMediaPlayers();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaPlayers();
    }

    private void releaseMediaPlayers() {
        if (playimgButton != null) {
            playimgButton.release();
            playimgButton = null;
        }
        if (playimgS != null) {
            playimgS.release();
            playimgS = null;
        }
    }
}