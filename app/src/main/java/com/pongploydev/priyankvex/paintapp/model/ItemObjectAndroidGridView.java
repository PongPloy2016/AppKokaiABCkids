package com.pongploydev.priyankvex.paintapp.model;

public class ItemObjectAndroidGridView {
    private String title;
    private int imageResId;
    private String imageUrl;
    private int lottieResId;
    private String gifUrl;
    private int targetActivity; // 0: MenuActivity, 1: MenuActivityAZ, 2: About

    // Constructor สำหรับภาพปกติ
    public ItemObjectAndroidGridView(String title, int imageResId, int targetActivity) {
        this.title = title;
        this.imageResId = imageResId;
        this.targetActivity = targetActivity;
    }

    // Constructor สำหรับภาพปกติ + Lottie
    public ItemObjectAndroidGridView(String title, int imageResId, int lottieResId, int targetActivity) {
        this.title = title;
        this.imageResId = imageResId;
        this.lottieResId = lottieResId;
        this.targetActivity = targetActivity;
    }

    // Constructor สำหรับ GIF
    public ItemObjectAndroidGridView(String title, String gifUrl, int targetActivity) {
        this.title = title;
        this.gifUrl = gifUrl;
        this.targetActivity = targetActivity;
    }

    // Getters and Setters
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public void setImageResId(int imageResId) {
        this.imageResId = imageResId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public int getLottieResId() {
        return lottieResId;
    }

    public void setLottieResId(int lottieResId) {
        this.lottieResId = lottieResId;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public int getTargetActivity() {
        return targetActivity;
    }

    public void setTargetActivity(int targetActivity) {
        this.targetActivity = targetActivity;
    }
}