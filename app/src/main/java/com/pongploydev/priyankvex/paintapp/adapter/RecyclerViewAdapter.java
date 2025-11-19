package com.pongploydev.priyankvex.paintapp.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.priyankvex.paintapp.model.ItemObject;
import com.pongploydev.priyankvex.paintapp.R;

import java.util.List;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    // Add these interfaces
    public interface OnItemClickListener {
        void onItemClick(ItemObject item, int position);
        void onSoundClick(ItemObject item, int position);
    }

    private List<ItemObject> rowListItem;
    private Context context;
    private int layoutResource;
    private OnItemClickListener onItemClickListener;
    private int highlightedPosition = -1;

    public RecyclerViewAdapter(Context context, List<ItemObject> rowListItem, int layoutResource) {
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

        // Highlight current item during auto read
        if (position == highlightedPosition) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#E3F2FD")); // Light blue
        } else {
            holder.cardView.setCardBackgroundColor(Color.WHITE);
        }

        // Set click listeners
        holder.cardView.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(item, position);
            }
        });

        if (holder.btnSound != null) {
            holder.btnSound.setOnClickListener(v -> {
                if (onItemClickListener != null) {
                    onItemClickListener.onSoundClick(item, position);
                }
            });
        }

        // You can also set long click listener if needed
        holder.cardView.setOnLongClickListener(v -> {
            // Handle long click if needed
            return true;
        });
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

            // If your layout doesn't have btn_sound, it will be null
            // Make sure to check for null in onBindViewHolder
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