package com.pongploydev.priyankvex.paintapp;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioGroup;

import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.RequestConfiguration; // IMPORTS เดิม
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
// **[NEW IMPORTS]**
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
// **[/NEW IMPORTS]**
import com.pongploydev.priyankvex.paintapp.activity.thailetters.MenuThaiLettersActivity;
import com.pongploydev.priyankvex.paintapp.activity.MenuActivityAZ;
import com.pongploydev.priyankvex.paintapp.activity.learning.LeaningActvity;
import com.pongploydev.priyankvex.paintapp.adapter.CustomAdapterAndroidGridView;
import com.pongploydev.priyankvex.paintapp.dialog.AboutDialog;
import com.pongploydev.priyankvex.paintapp.model.ItemObjectAndroidGridView;
import com.pongploydev.priyankvex.paintapp.utils.GridSpacingItemDecoration;

import java.util.ArrayList;
import java.util.List;

public class MainActivityAndroidGridView extends Activity {

    private CustomAdapterAndroidGridView customAdapter;
    private RecyclerView recyclerView;
    private RadioGroup viewModeGroup;
    private Toolbar toolbar;
    private boolean isGridView = true;
    private AdView adView;

    // **[NEW]** In-App Update Variables
    private AppUpdateManager appUpdateManager;
    private static final int MY_REQUEST_CODE = 500;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_gridview);

        // **แก้ไข: เริ่มต้น Mobile Ads SDK และตั้งค่า Child-Directed Treatment ในเมธอดเดียว**
        initializeAdMob();

        initViews();
        setupToolbar();
        setupRecyclerView();
        setupViewModeSelector();
        // **แก้ไข: โหลดโฆษณาใน setupAdMob() โดยไม่ต้องตั้งค่า RequestConfiguration ซ้ำ**
        setupAdMob();

        // **[NEW]** Initialize AppUpdateManager and check for update
        appUpdateManager = AppUpdateManagerFactory.create(this);
        checkForAppUpdate();
    }

    // ----------------------------------------------------------------------
    // **[NEW]** In-App Update Methods
    // ----------------------------------------------------------------------

    private void checkForAppUpdate() {
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {

            // 1. Check if an update is available (and flexible update is allowed)
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)) {

                Log.d("InAppUpdate", "Flexible update available. Starting flow.");

                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            MY_REQUEST_CODE);
                } catch (Exception e) {
                    Log.e("InAppUpdate", "Failed to start update flow: " + e.getMessage());
                }

                // 2. Check if an update is already in progress and needs to be resumed
            } else if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                Log.d("InAppUpdate", "Update in progress. Resuming flow.");
                try {
                    appUpdateManager.startUpdateFlowForResult(
                            appUpdateInfo,
                            AppUpdateType.FLEXIBLE,
                            this,
                            MY_REQUEST_CODE);
                } catch (Exception e) {
                    Log.e("InAppUpdate", "Failed to resume update flow: " + e.getMessage());
                }
            }
        }).addOnFailureListener(e -> {
            Log.e("InAppUpdate", "Update check failed: " + e.getMessage());
        });
    }

    // ----------------------------------------------------------------------
    // Standard Methods
    // ----------------------------------------------------------------------

    /**
     * Initializes Mobile Ads SDK and sets the RequestConfiguration for Child-Directed Treatment.
     * This ensures compliance with Families Policy and COPPA/GDPR.
     */
    private void initializeAdMob() {
        // 1. สร้าง RequestConfiguration Builder สำหรับการตั้งค่าความเป็นส่วนตัวทั่วโลก
        RequestConfiguration.Builder requestConfigurationBuilder = new RequestConfiguration.Builder();

        // 2. ตั้งค่า Tag For Child-Directed Treatment (COPPA Compliance)
        requestConfigurationBuilder.setTagForChildDirectedTreatment(
                RequestConfiguration.TAG_FOR_CHILD_DIRECTED_TREATMENT_TRUE);

        // 3. ตั้งค่า Under Age of Consent (GDPR Compliance)
        requestConfigurationBuilder.setTagForUnderAgeOfConsent(
                RequestConfiguration.TAG_FOR_UNDER_AGE_OF_CONSENT_TRUE);

        // 4. (ทางเลือก) กำหนด Max Ad Content Rating เป็น G (เหมาะสมกับทุกเพศทุกวัย)
        requestConfigurationBuilder.setMaxAdContentRating(
                RequestConfiguration.MAX_AD_CONTENT_RATING_G);

        // 5. สร้างและตั้งค่า RequestConfiguration ให้กับ MobileAds
        RequestConfiguration requestConfiguration = requestConfigurationBuilder.build();
        MobileAds.setRequestConfiguration(requestConfiguration);

        // 6. เริ่มต้น Mobile Ads SDK
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
                Log.d("AdMob", "Initialization complete. Child-Directed Treatment set to TRUE.");
            }
        });
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        recyclerView = findViewById(R.id.recyclerView);
        viewModeGroup = findViewById(R.id.viewModeGroup);
       adView = findViewById(R.id.adView);
    }

    private void setupToolbar() {
        toolbar.setTitle("แอปฝึกเขียนตัวอักษร");
        toolbar.setTitleTextColor(getResources().getColor(android.R.color.white));

        toolbar.inflateMenu(R.menu.menu_main);
        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                return onOptionsItemSelected(item);
            }
        });
    }

    private void setupRecyclerView() {
        setupGridView();

        List<ItemObjectAndroidGridView> allItems = getAllItemObject();
        customAdapter = new CustomAdapterAndroidGridView(this, allItems);
        recyclerView.setAdapter(customAdapter);

//        customAdapter.setOnItemClickListener(new CustomAdapterAndroidGridView.OnItemClickListener() {
//            @Override
//            public void onItemClick(int position, ItemObjectAndroidGridView item) {
//                handleItemClick(position, item);
//            }
//        });
    }

    private void setupViewModeSelector() {
        viewModeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if (checkedId == R.id.btnGridView) {
                    switchToGridView();
                } else if (checkedId == R.id.btnListView) {
                    switchToListView();
                }
            }
        });
    }

    private void setupAdMob() {
        if (adView == null) {
            Log.e("AdMob", "AdView is null. Check layout ID.");
            return;
        }

        // โหลดโฆษณาตามปกติ RequestConfiguration ถูกตั้งค่า Global ใน initializeAdMob() แล้ว
        adView.loadAd(new AdRequest.Builder().build());

        adView.setAdListener(new AdListener() {
            @Override
            public void onAdLoaded() { Log.d("AdMob", "Ad loaded successfully"); }
            @Override
            public void onAdFailedToLoad(LoadAdError adError) { Log.e("AdMob", "Ad failed to load: " + adError.getMessage()); }
            @Override
            public void onAdOpened() { Log.d("AdMob", "Ad opened"); }
            @Override
            public void onAdClicked() { Log.d("AdMob", "Ad clicked"); }
            @Override
            public void onAdClosed() {
                Log.d("AdMob", "Ad closed. Loading new ad...");
                // สร้าง AdRequest ใหม่เพื่อโหลดโฆษณาถัดไป
                AdRequest adRequest = new AdRequest.Builder().build();
                adView.loadAd(adRequest);
            }
            @Override
            public void onAdImpression() { Log.d("AdMob", "Ad impression recorded"); }
        });
    }

    private void switchToGridView() {
        if (!isGridView) {
            setupGridView();
            isGridView = true;
            customAdapter.setGridView(true);
            customAdapter.notifyDataSetChanged();
        }
    }

    private void switchToListView() {
        if (isGridView) {
            setupListView();
            isGridView = false;
            customAdapter.setGridView(false);
            customAdapter.notifyDataSetChanged();
        }
    }

    private void setupGridView() {
        GridLayoutManager gridLayoutManager = new GridLayoutManager(this, 2);
        recyclerView.setLayoutManager(gridLayoutManager);
        removeAllItemDecorations();
        recyclerView.addItemDecoration(new GridSpacingItemDecoration(2, 16, true));
    }

    private void setupListView() {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);
        removeAllItemDecorations();
        recyclerView.addItemDecoration(new ListSpacingItemDecoration(16));
    }

    private void removeAllItemDecorations() {
        while (recyclerView.getItemDecorationCount() > 0) {
            recyclerView.removeItemDecorationAt(0);
        }
    }

    private void handleItemClick(int position, ItemObjectAndroidGridView item) {
        int targetActivity = item.getTargetActivity();

        switch (targetActivity) {
            case 0: startLearningActivity(); break;
            case 1: startMenuActivity(); break;
            case 2: startMenuActivityAZ(); break;
            case 3: showAboutDialogNew(); break;
            default: handleItemClickByPosition(position); break;
        }
    }

    private void handleItemClickByPosition(int position) {
        if (position == 0) startLearningActivity();
        else if (position == 1) startMenuActivity();
        else if (position == 2) startMenuActivityAZ();
        else if (position == 3) showAboutDialogNew();
    }

    private void startLearningActivity() {
        Intent i = new Intent(MainActivityAndroidGridView.this, LeaningActvity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void startMenuActivity() {
        Intent i = new Intent(MainActivityAndroidGridView.this, MenuThaiLettersActivity.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void startMenuActivityAZ() {
        Intent i = new Intent(MainActivityAndroidGridView.this, MenuActivityAZ.class);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    private void showAboutDialogNew() {
        AboutDialog aboutDialog = new AboutDialog(this);
        aboutDialog.show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            openSettings();
            return true;
        } else if (id == R.id.action_about) {
            showAboutDialogNew();
            return true;
        } else if (id == R.id.action_ads_info) {
            showAdsInfo();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void openSettings() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("ตั้งค่า")
                .setMessage("ฟังก์ชันตั้งค่าจะถูกเพิ่มในเวอร์ชันถัดไป")
                .setPositiveButton("ตกลง", null)
                .show();
    }

    private void showAdsInfo() {
        android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(this);
        builder.setTitle("ข้อมูลโฆษณา")
                .setMessage("โฆษณาในแอปพลิเคชันนี้ช่วยให้เราสามารถ\nพัฒนาและปรับปรุงแอปฯ ให้ดีขึ้นอย่างต่อเนื่อง\n\nขอบคุณสำหรับการสนับสนุน!")
                .setPositiveButton("เข้าใจแล้ว", null)
                .show();
    }

    private List<ItemObjectAndroidGridView> getAllItemObject() {
        List<ItemObjectAndroidGridView> items = new ArrayList<>();
        items.add(new ItemObjectAndroidGridView("ฝึกอ่าน", R.drawable.doing_learn_01, R.raw.animation_thai, 0));
        items.add(new ItemObjectAndroidGridView("ฝึกเขียน ก - ฮ", R.drawable.card_one, R.raw.animation_thai, 1));
        items.add(new ItemObjectAndroidGridView("ฝึกเขียน A - Z", R.drawable.card_two, 2));
        items.add(new ItemObjectAndroidGridView("จัดทำโดย", R.drawable.card_three, 3));
        return items;
    }

    // ----------------------------------------------------------------------
    // Activity Life Cycle Management
    // ----------------------------------------------------------------------

    @Override
    protected void onResume() {
        super.onResume();

        // AdMob
        if (customAdapter != null) {
            customAdapter.notifyDataSetChanged();
        }
        if (adView != null) {
            adView.resume();
        }

        // **[NEW]** In-App Update: Resume if download is complete but not installed
        appUpdateManager.getAppUpdateInfo().addOnSuccessListener(appUpdateInfo -> {
            if (appUpdateInfo.updateAvailability() == UpdateAvailability.DEVELOPER_TRIGGERED_UPDATE_IN_PROGRESS) {
                // In a production app, you'd typically show a Snackbar/Dialog first before forcing update
                // appUpdateManager.completeUpdate();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        // AdMob
        if (adView != null) {
            adView.pause();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // AdMob
        if (recyclerView != null) {
            recyclerView.setAdapter(null);
        }
        if (adView != null) {
            adView.destroy();
        }
    }

    // **[NEW]** Handle the result of the update flow
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == MY_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Log.w("InAppUpdate", "Update flow failed! Result code: " + resultCode);
            } else {
                Log.d("InAppUpdate", "Update flow succeeded.");
            }
        }
    }

    // Inner class สำหรับ List Spacing ที่สมบูรณ์
    public class ListSpacingItemDecoration extends RecyclerView.ItemDecoration {
        private final int spacing;

        public ListSpacingItemDecoration(int spacing) {
            this.spacing = spacing;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            outRect.left = spacing;
            outRect.right = spacing;
            outRect.bottom = spacing;

            if (parent.getChildAdapterPosition(view) == 0) {
                outRect.top = spacing;
            } else {
                outRect.top = 0;
            }
        }
    }
}