package com.pongploydev.priyankvex.paintapp.adapter; // เปลี่ยนเป็นชื่อ package ของคุณ

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
import com.pongploydev.priyankvex.paintapp.activity.learning.SubActivity;
import com.pongploydev.priyankvex.paintapp.model.LearningCategory;
import com.pongploydev.priyankvex.paintapp.R;
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

    public LearningCategoriesAdapter(Context context, List<LearningCategory> categories, int type) {
        this.context = context;
        this.categories = categories;
        this.activityType = type;
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

        // 3. Click Listener
        viewHolder.lloutHome.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                Intent intent;

                // TODO: ต้องเปลี่ยน SubActivity, LookChooseActivity, ListenGuessActivity เป็นชื่อ Activity ที่ถูกต้อง
                Class<?> targetActivity;

                switch (activityType) {
                    case 1:
                        // ต้องเปลี่ยน SubActivity.class เป็นชื่อ Activity จริง
                        targetActivity = SubActivity.class;
                        intent = new Intent(context, targetActivity);
                        intent.putExtra("categoryPosition", category.getCategoryPosition());
                        intent.putExtra("Category", category.getTitle());
                        intent.putExtra("Type", activityType);
                        break;
//                    case 2:
//                        // ต้องเปลี่ยน LookChooseActivity.class เป็นชื่อ Activity จริง
//                        targetActivity = LookChooseActivity.class;
//                        intent = new Intent(context, targetActivity);
//                        intent.putExtra("categoryPosition", category.getCategoryPosition());
//                        intent.putExtra("SubCate", category.getTitle());
//                        intent.putExtra("TYPE", activityType);
//                        break;
//                    case 3:
//                        // ต้องเปลี่ยน ListenGuessActivity.class เป็นชื่อ Activity จริง
//                        targetActivity = ListenGuessActivity.class;
//                        intent = new Intent(context, targetActivity);
//                        intent.putExtra("categoryPosition", category.getCategoryPosition());
//                        intent.putExtra("SubCate", category.getTitle());
//                        intent.putExtra("TYPE", activityType);
//                        break;
                    default:
                        return; // ไม่ทำอะไรถ้า Type ไม่ถูกต้อง
                }
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }
}