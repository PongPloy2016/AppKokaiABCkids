package com.pongploydev.priyankvex.paintapp.activity.learning;

import android.content.Context;
import android.content.Intent;
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

// ต้อง Import Activity ปลายทางที่คุณจะเรียกใช้
// import com.yourpackage.SubActivity;
// import com.yourpackage.LookChooseActivity;
// import com.yourpackage.ListenGuessActivity;

public class LeaningActvity extends AppCompatActivity {

    private Context context;
    private int activityType = 1;
    private TextView txtExamTitle;
    private RecyclerView rvHomeCategories;

    // ส่วนของ AdView (ยังคงไว้)
    private RelativeLayout llAdView;
    private LinearLayout llAdViewFacebook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_learning);
        context = this;

        // 1. Get Intent Data
        Intent intent = getIntent();
        activityType = intent.getIntExtra("Type", 1);

        initViews();
        setupDataAndAdapter();

        // 3. Load Ads (สมมติว่า Utils.loadBannerAd มีอยู่)
        // Utils.loadBannerAd(this, llAdView, llAdViewFacebook);
    }

    private void initViews() {
        // Find Views
        txtExamTitle = findViewById(R.id.txtTitleSubHome);
        rvHomeCategories = findViewById(R.id.rvHomeCategories);
        llAdView = findViewById(R.id.llAdView);
        llAdViewFacebook = findViewById(R.id.llAdViewFacebook);

        // Set Title based on Type
        txtExamTitle.setText(CategoryDataHelper.getTitleForType(activityType));

        // Note: The original XML had txtTitleSubHome and txtExamTitle
        // I use txtTitleSubHome as the main title as per the logic.
    }

    private void setupDataAndAdapter() {
        // ใช้ Data Model จาก Helper
        List<LearningCategory> categories = CategoryDataHelper.getAllCategories();

        GridLayoutManager gridLayoutManager = new GridLayoutManager(context, 2, GridLayoutManager.VERTICAL, false);
        rvHomeCategories.setLayoutManager(gridLayoutManager);

        // ส่ง List<LearningCategory> เข้า Adapter
        LearningCategoriesAdapter homeCategoriesAdapter = new LearningCategoriesAdapter(context, categories, activityType);
        rvHomeCategories.setAdapter(homeCategoriesAdapter);
    }

    public void onClickBack(View view) {
        finish();
    }
}