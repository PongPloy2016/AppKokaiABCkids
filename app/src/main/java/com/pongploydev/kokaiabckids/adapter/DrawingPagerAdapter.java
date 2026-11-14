package com.pongploydev.kokaiabckids.adapter;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.pongploydev.kokaiabckids.R;
import com.pongploydev.kokaiabckids.drawingView.DrawingView;

import java.util.List;

public class DrawingPagerAdapter extends RecyclerView.Adapter<DrawingPagerAdapter.DrawingViewHolder> {

    private Context context;
    private int totalItems;
    private OnDrawingViewBoundListener onDrawingViewBoundListener;

    public interface OnDrawingViewBoundListener {
        void onDrawingViewBound(DrawingViewHolder holder, int position);
    }

    public DrawingPagerAdapter(Context context, int totalItems, OnDrawingViewBoundListener listener) {
        this.context = context;
        this.totalItems = totalItems;
        this.onDrawingViewBoundListener = listener;
    }

    @NonNull
    @Override
    public DrawingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_drawing_card, parent, false);
        return new DrawingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DrawingViewHolder holder, int position) {
        // Reset DrawingView state if needed
        holder.drawingView.startNew();

        // Notify activity to configure this DrawingView (set guides, background, etc.)
        if (onDrawingViewBoundListener != null) {
            onDrawingViewBoundListener.onDrawingViewBound(holder, position);
        }
    }

    @Override
    public int getItemCount() {
        return totalItems;
    }

    public static class DrawingViewHolder extends RecyclerView.ViewHolder {
        public DrawingView drawingView;
        public LottieAnimationView animationSuccess;
        public LottieAnimationView animationTryAgain;

        public android.widget.VideoView videoView;
        public android.view.ViewGroup videoContainer;
        public android.view.View btnCloseVideo;

        public DrawingViewHolder(@NonNull View itemView) {
            super(itemView);
            drawingView = itemView.findViewById(R.id.drawing);
            animationSuccess = itemView.findViewById(R.id.animationSuccess);
            animationTryAgain = itemView.findViewById(R.id.animationTryAgain);
            videoView = itemView.findViewById(R.id.videoView);
            videoContainer = itemView.findViewById(R.id.videoContainer);
            btnCloseVideo = itemView.findViewById(R.id.btnCloseVideo);
        }
    }
}
