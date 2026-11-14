package com.pongploydev.kokaiabckids.holder;

import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.pongploydev.kokaiabckids.model.MenuItem;
import com.pongploydev.kokaiabckids.R;


public class MenuItemViewHolder extends RecyclerView.ViewHolder {

    public TextView countryName;
    public ImageView countryPhoto;
    public CardView cardView;
    public ImageButton btnSound;

    public MenuItemViewHolder(View itemView) {
        super(itemView);

        // Initialize views
        countryName = itemView.findViewById(R.id.country_name);
        countryPhoto = itemView.findViewById(R.id.country_photo);
        cardView = itemView.findViewById(R.id.card_view);
        btnSound = itemView.findViewById(R.id.btn_sound);

        // Optional: Set sound button style programmatically
        setupSoundButton();
    }

    public void bind(MenuItem item,
                     OnItemClickListener listener,
                     OnSoundClickListener soundListener) {

        // Set basic item data
        countryName.setText(item.getName());
        countryPhoto.setImageResource(item.getImage());

        // Set content description for accessibility
        countryPhoto.setContentDescription(item.getName());
        btnSound.setContentDescription("Play sound for " + item.getName());

        // Set card click listener
        if (listener != null) {
            cardView.setOnClickListener(v -> listener.onItemClick(item, getAdapterPosition()));
        }

        // Set sound button click listener
        if (soundListener != null) {
            btnSound.setOnClickListener(v -> soundListener.onSoundClick(item, getAdapterPosition()));
        } else {
            // Fallback: if no sound listener provided, use default behavior
            btnSound.setOnClickListener(v -> {
                // Default sound play behavior
                playDefaultSound(item);
            });
        }
    }

    /**
     * Alternative bind method without sound listener
     */
    public void bind(MenuItem item, OnItemClickListener listener) {
        bind(item, listener, null);
    }

    /**
     * Setup sound button appearance
     */
    private void setupSoundButton() {
        // Set button properties programmatically
        btnSound.setBackgroundResource(android.R.color.transparent);
        btnSound.setPadding(4, 4, 4, 4);

        // Optional: Add ripple effect
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            btnSound.setBackgroundResource(R.drawable.sound_button_ripple);
        }
    }

    /**
     * Default sound playback (can be overridden or removed)
     */
    private void playDefaultSound(MenuItem item) {
        // This is a basic implementation
        // You might want to handle sound playback in Activity/Fragment instead

        try {
            // Example: Play a default beep sound or use TTS
            // For now, just log the action
            android.util.Log.d("MenuItemViewHolder", "Play sound for: " + item.getName());

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Update sound button state (playing/not playing)
     */
    public void setSoundPlaying(boolean isPlaying) {
        if (isPlaying) {
            btnSound.setImageResource(R.drawable.ic_volume_off);
            btnSound.setContentDescription("Stop sound");
        } else {
            btnSound.setImageResource(R.drawable.ic_volume_up);
            btnSound.setContentDescription("Play sound");
        }
    }

    /**
     * Enable/disable sound button
     */
    public void setSoundEnabled(boolean enabled) {
        btnSound.setEnabled(enabled);
        btnSound.setAlpha(enabled ? 1.0f : 0.5f);
    }

    /**
     * Update item appearance based on selection state
     */
    public void setSelected(boolean selected) {
        if (selected) {
            cardView.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.selected_color)
            );
        } else {
            cardView.setCardBackgroundColor(
                    itemView.getContext().getResources().getColor(R.color.card_default_color)
            );
        }
    }

    // Interfaces for click listeners
    public interface OnItemClickListener {
        void onItemClick(MenuItem item, int position);
    }

    public interface OnSoundClickListener {
        void onSoundClick(MenuItem item, int position);
    }

    /**
     * Simple interfaces without position parameter
     */
    public interface SimpleItemClickListener {
        void onItemClick(MenuItem item);
    }

    public interface SimpleSoundClickListener {
        void onSoundClick(MenuItem item);
    }
}