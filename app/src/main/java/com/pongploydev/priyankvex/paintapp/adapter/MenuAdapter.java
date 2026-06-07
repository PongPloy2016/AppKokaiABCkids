package com.pongploydev.priyankvex.paintapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.RecyclerView;


import com.pongploydev.priyankvex.paintapp.holder.MenuItemViewHolder;
import com.pongploydev.priyankvex.paintapp.model.MenuItem;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.List;
public class MenuAdapter extends RecyclerView.Adapter<MenuItemViewHolder> {

    private List<MenuItem> itemList;
    private MenuItemViewHolder.OnItemClickListener itemListener;
    private MenuItemViewHolder.OnSoundClickListener soundListener;

    // [NEW] Kids Color Palette
    private final int[] kidsColors = {
            0xFFFF6B6B, // Red
            0xFF4ECDC4, // Teal
            0xFFFFD166, // Yellow
            0xFF06D6A0, // Green
            0xFF118AB2, // Blue
            0xFFEF476F, // Pink
            0xFF7209B7, // Purple
            0xFFF15BB5  // Pink-Purple
    };

    public MenuAdapter(List<MenuItem> itemList,
                       MenuItemViewHolder.OnItemClickListener itemListener,
                       MenuItemViewHolder.OnSoundClickListener soundListener) {
        this.itemList = itemList;
        this.itemListener = itemListener;
        this.soundListener = soundListener;
    }

    @Override
    public MenuItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View layoutView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_view_list, parent, false);
        return new MenuItemViewHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(MenuItemViewHolder holder, int position) {
        holder.bind(itemList.get(position), itemListener, soundListener);

        // [NEW] Animation
        android.view.animation.Animation animation = android.view.animation.AnimationUtils.loadAnimation(holder.itemView.getContext(), R.anim.pop_in);
        holder.itemView.startAnimation(animation);

        // [NEW] Set Dynamic Background Color for the label
        // Note: Assuming MenuItemViewHolder exposes the text view or we can find it
        android.widget.TextView textView = holder.itemView.findViewById(R.id.country_name);
        if (textView != null) {
            textView.setBackgroundColor(kidsColors[position % kidsColors.length]);
        }
    }

    @Override
    public int getItemCount() {
        return itemList.size();
    }

    public void updateData(List<MenuItem> newItems) {
        this.itemList = newItems;
        notifyDataSetChanged();
    }
}