package com.pongploydev.kokaiabckids.adapter; // เปลี่ยนเป็นชื่อ package ของคุณ

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;
import com.pongploydev.kokaiabckids.model.LearningCategory;
import com.pongploydev.kokaiabckids.R;
// ต้อง import Activity ปลายทาง
// import com.yourpackage.SubActivity;
// import com.yourpackage.LookChooseActivity;
// import com.yourpackage.ListenGuessActivity;

import java.util.List;

public class LearningCategoriesAdapter extends RecyclerView.Adapter<LearningCategoriesAdapter.ViewHolder> {

    class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgHomeCategory;
        CardView lloutHome; // ในโค้ดใหม่นี่คือ CardView/LinearLayout ภายนอก
        TextView txtCategoryTitle; // New TextView for the title

        ViewHolder(@NonNull View view) {
            super(view);
            this.imgHomeCategory = view.findViewById(R.id.imgHomeCategory);
            this.lloutHome = view.findViewById(R.id.lloutCardHome);
            this.txtCategoryTitle = view.findViewById(R.id.txtCategoryTitle);
        }
    }

    private final Context context;
    private final List<LearningCategory> categories;
    private final int activityType;

    // [NEW] Kids Color Palette
    private final int[] kidsColors = {
            0xFFFF6B6B, // Red
            0xFF4ECDC4, // Teal
            0xFFFFD166, // Yellow
            0xFF06D6A0, // Green
            0xFF118AB2, // Blue
            0xFFEF476F, // Pink
            0xFF7209B7, // Purple
            0xFFF15BB5 // Pink-Purple
    };

    private android.media.SoundPool soundPool;
    private int soundClickId;
    private boolean isSoundLoaded = false;

    public LearningCategoriesAdapter(Context context, List<LearningCategory> categories, int type) {
        this.context = context;
        this.categories = categories;
        this.activityType = type;

        initSounds();
    }

    private void initSounds() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            android.media.AudioAttributes audioAttributes = new android.media.AudioAttributes.Builder()
                    .setUsage(android.media.AudioAttributes.USAGE_GAME)
                    .setContentType(android.media.AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build();
            soundPool = new android.media.SoundPool.Builder()
                    .setMaxStreams(1)
                    .setAudioAttributes(audioAttributes)
                    .build();
        } else {
            soundPool = new android.media.SoundPool(1, android.media.AudioManager.STREAM_MUSIC, 0);
        }

        try {
            soundClickId = soundPool.load(context, R.raw.button, 1);
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

    private void playClickSound() {
        if (isSoundLoaded && soundPool != null) {
            soundPool.play(soundClickId, 1f, 1f, 0, 0, 1f);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.card_list_home, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder viewHolder, final int position) {
        final LearningCategory category = categories.get(position);

        // 1. Load Image
        Glide.with(context)
                .load(category.getImageResId())
                .apply(new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL))
                .into(viewHolder.imgHomeCategory);

        // 2. Set Title
        viewHolder.txtCategoryTitle.setText(category.getTitle());
        // [NEW] Set Dynamic Background Color
        viewHolder.txtCategoryTitle.setBackgroundColor(kidsColors[position % kidsColors.length]);

        // 3. Click Listener
        viewHolder.lloutHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                // Play Sound
                playClickSound();

                Intent intent;

                switch (activityType) {
                    case 1:
                        intent = new Intent(context, com.pongploydev.kokaiabckids.activity.learning.SubActivity.class);
                        intent.putExtra("categoryPosition", category.getCategoryPosition());
                        intent.putExtra("Category", category.getTitle());
                        intent.putExtra("Type", activityType);
                        break;
                    // case 2:
                    // // ต้องเปลี่ยน LookChooseActivity.class เป็นชื่อ Activity จริง
                    // targetActivity = LookChooseActivity.class;
                    // intent = new Intent(context, targetActivity);
                    // intent.putExtra("categoryPosition", category.getCategoryPosition());
                    // intent.putExtra("SubCate", category.getTitle());
                    // intent.putExtra("TYPE", activityType);
                    // break;
                    // case 3:
                    // // ต้องเปลี่ยน ListenGuessActivity.class เป็นชื่อ Activity จริง
                    // targetActivity = ListenGuessActivity.class;
                    // intent = new Intent(context, targetActivity);
                    // intent.putExtra("categoryPosition", category.getCategoryPosition());
                    // intent.putExtra("SubCate", category.getTitle());
                    // intent.putExtra("TYPE", activityType);
                    // break;
                    default:
                        return; // ไม่ทำอะไรถ้า Type ไม่ถูกต้อง
                }
                context.startActivity(intent);
            }
        });

        // [NEW] Animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(context,
                R.anim.pop_in);
        viewHolder.itemView.startAnimation(animation);
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}