package com.pongploydev.priyankvex.paintapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.priyankvex.paintapp.model.ItemObject;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.List;
import java.util.Random;

public class AZRecyclerViewAdapter extends RecyclerView.Adapter<AZRecyclerViewAdapter.ViewHolder> {

    public interface OnItemClickListener {
        void onItemClick(ItemObject item, int position);
        void onSoundClick(ItemObject item, int position);
    }

    private List<ItemObject> rowListItem;
    private Context context;
    private int layoutResource;
    private OnItemClickListener onItemClickListener;
    private int highlightedPosition = -1;
    private int lastAnimatedPosition = -1;
    private Random random = new Random();

    // สีสันสดใสสำหรับเด็ก
    private int[] cardColors = {
            Color.parseColor("#FFCDD2"), // Light Red
            Color.parseColor("#F8BBD0"), // Light Pink
            Color.parseColor("#E1BEE7"), // Light Purple
            Color.parseColor("#D1C4E9"), // Light Deep Purple
            Color.parseColor("#C5CAE9"), // Light Indigo
            Color.parseColor("#BBDEFB"), // Light Blue
            Color.parseColor("#B3E5FC"), // Light Light Blue
            Color.parseColor("#B2EBF2"), // Light Cyan
            Color.parseColor("#B2DFDB"), // Light Teal
            Color.parseColor("#C8E6C9"), // Light Green
            Color.parseColor("#DCEDC8"), // Light Light Green
            Color.parseColor("#F0F4C3"), // Light Lime
            Color.parseColor("#FFF9C4"), // Light Yellow
            Color.parseColor("#FFECB3"), // Light Amber
            Color.parseColor("#FFE0B2"), // Light Orange
            Color.parseColor("#FFCCBC"), // Light Deep Orange
    };

    // สีสำหรับไฮไลท์ (สีที่เข้มขึ้นเล็กน้อย)
    private int[] highlightColors = {
            Color.parseColor("#EF9A9A"), // Red
            Color.parseColor("#F48FB1"), // Pink
            Color.parseColor("#CE93D8"), // Purple
            Color.parseColor("#B39DDB"), // Deep Purple
            Color.parseColor("#9FA8DA"), // Indigo
            Color.parseColor("#90CAF9"), // Blue
            Color.parseColor("#81D4FA"), // Light Blue
            Color.parseColor("#80DEEA"), // Cyan
            Color.parseColor("#80CBC4"), // Teal
            Color.parseColor("#A5D6A7"), // Green
            Color.parseColor("#C5E1A5"), // Light Green
            Color.parseColor("#E6EE9C"), // Lime
            Color.parseColor("#FFF59D"), // Yellow
            Color.parseColor("#FFE082"), // Amber
            Color.parseColor("#FFCC80"), // Orange
            Color.parseColor("#FFAB91"), // Deep Orange
    };

    public AZRecyclerViewAdapter(Context context, List<ItemObject> rowListItem, int layoutResource) {
        this.context = context;
        this.rowListItem = rowListItem;
        this.layoutResource = layoutResource;
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.onItemClickListener = listener;
    }

    public void setHighlightedPosition(int position) {
        this.highlightedPosition = position;
        notifyDataSetChanged();
    }

    public void clearHighlight() {
        this.highlightedPosition = -1;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_grid, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ItemObject item = rowListItem.get(position);

        // Set item data
        holder.itemName.setText(item.getName());
        holder.itemImage.setImageResource(item.getImage());

        // ตั้งค่าสีการ์ดแบบสุ่มแต่คงที่สำหรับแต่ละตำแหน่ง
        int baseColor = cardColors[position % cardColors.length];
        holder.cardView.setCardBackgroundColor(baseColor);

        // ตั้งค่ารัศมีมุมการ์ดให้โค้งมนมากขึ้นสำหรับเด็ก
        holder.cardView.setRadius(32f);

        // Highlight current item during auto read
        if (position == highlightedPosition) {
            int highlightColor = highlightColors[position % highlightColors.length];
            holder.cardView.setCardBackgroundColor(highlightColor);

            // อนิเมชันกระพริบเมื่อถูกไฮไลท์
            Animation pulse = AnimationUtils.loadAnimation(context, R.anim.pulse_animation);
            holder.cardView.startAnimation(pulse);

            // เพิ่มเอฟเฟกต์ขยายเล็กน้อย
            holder.cardView.setScaleX(1.1f);
            holder.cardView.setScaleY(1.1f);

            // เพิ่มขอบสีเมื่อไฮไลท์
            holder.cardView.setCardElevation(8f);
        } else {
            holder.cardView.setCardBackgroundColor(baseColor);
            holder.cardView.clearAnimation();
            holder.cardView.setScaleX(1.0f);
            holder.cardView.setScaleY(1.0f);
            holder.cardView.setCardElevation(4f);
        }

        // อนิเมชันเมื่อแสดงไอเทม (ล่าช้าเล็กน้อยสำหรับแต่ละตัว)
        if (position > lastAnimatedPosition) {
            Animation animation = AnimationUtils.loadAnimation(context, R.anim.bounce_in);
            animation.setStartOffset(position * 100L); // ล่าช้าเพิ่มตามตำแหน่ง
            holder.itemView.startAnimation(animation);
            lastAnimatedPosition = position;
        }

        // ตั้งค่าสีข้อความตามพื้นหลัง (เพื่อความคมชัด)
        setTextColorBasedOnBackground(holder.itemName, holder.cardView.getCardBackgroundColor().getDefaultColor());

        // เพิ่มขนาดตัวอักษรสำหรับเด็ก
        holder.itemName.setTextSize(20f);

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                // อนิเมชันเด้งเมื่อกด
                Animation bounce = AnimationUtils.loadAnimation(context, R.anim.bounce);
                holder.cardView.startAnimation(bounce);

                onItemClickListener.onItemClick(item, position);
            }
        });

        if (holder.btnSound != null) {
            // ตั้งค่าปุ่มเสียงให้มีสีสันและขนาดใหญ่ขึ้น
            setupSoundButton(holder.btnSound, position);

            holder.btnSound.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    // อนิเมชันสั่นเมื่อกดปุ่มเสียง
                    Animation shake = AnimationUtils.loadAnimation(context, R.anim.shake);
                    holder.btnSound.startAnimation(shake);

                    onItemClickListener.onSoundClick(item, position);
                }
            });
        }

        // Long click listener สำหรับฟีเจอร์เพิ่มเติม
        holder.cardView.setOnLongClickListener(v -> {
            // อนิเมชันหมุนเมื่อกดค้าง
            Animation rotate = AnimationUtils.loadAnimation(context, R.anim.rotate);
            holder.cardView.startAnimation(rotate);

            // แสดงข้อความพิเศษ
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item, position);
            }
            return true;
        });
    }

    private void setTextColorBasedOnBackground(TextView textView, int backgroundColor) {
        // ตรวจสอบความสว่างของพื้นหลังเพื่อเลือกสีข้อความที่เหมาะสม
        double brightness = Color.red(backgroundColor) * 0.299 +
                Color.green(backgroundColor) * 0.587 +
                Color.blue(backgroundColor) * 0.114;

        if (brightness > 150) {
            textView.setTextColor(Color.parseColor("#1A237E")); // สีน้ำเงินเข้ม
        } else {
            textView.setTextColor(Color.WHITE);
        }

        // เพิ่มเงาให้ข้อความเพื่อให้อ่านง่าย
        textView.setShadowLayer(4f, 2f, 2f, Color.parseColor("#40000000"));
    }

    private void setupSoundButton(Button button, int position) {
        // ตั้งค่าสีปุ่มเสียงตามตำแหน่ง
        int[] soundButtonColors = {
                Color.parseColor("#FF5252"), // Red
                Color.parseColor("#E040FB"), // Purple
                Color.parseColor("#536DFE"), // Blue
                Color.parseColor("#69F0AE"), // Green
                Color.parseColor("#FFD740"), // Yellow
                Color.parseColor("#FF6E40"), // Orange
        };

        button.setBackgroundColor(soundButtonColors[position % soundButtonColors.length]);
        button.setTextColor(Color.WHITE);
        button.setTextSize(16f);

        // ทำให้ปุ่มโค้งมน
        //button.setRadius(20f);

        // เปลี่ยนเอโมจิปุ่มเสียงแบบสุ่ม
        String[] soundEmojis = {"🔊", "🔔", "🎵", "🎶", "📢", "💫", "🌟", "✨"};
        button.setText(soundEmojis[position % soundEmojis.length]);
    }

    @Override
    public int getItemCount() {
        return rowListItem.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public TextView itemName;
        public ImageView itemImage;
        public CardView cardView;
        public Button btnSound;

        public ViewHolder(View itemView) {
            super(itemView);
            itemName = itemView.findViewById(R.id.item_name);
            itemImage = itemView.findViewById(R.id.item_image);
            cardView = itemView.findViewById(R.id.card_view);
            btnSound = itemView.findViewById(R.id.btn_sound);
        }
    }

    // Method to update data
    public void updateData(List<ItemObject> newData) {
        this.rowListItem = newData;
        notifyDataSetChanged();
    }

    // Method to get item at position
    public ItemObject getItemAt(int position) {
        if (position >= 0 && position < rowListItem.size()) {
            return rowListItem.get(position);
        }
        return null;
    }
}