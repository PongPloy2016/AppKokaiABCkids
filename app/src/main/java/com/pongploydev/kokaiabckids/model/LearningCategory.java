package com.pongploydev.kokaiabckids.model;


public class LearningCategory {
    private final String title;
    private final int imageResId;
    private final int categoryPosition; // ใช้เพื่อส่งต่อใน Intent

    public LearningCategory(int categoryPosition, String title, int imageResId) {
        this.categoryPosition = categoryPosition;
        this.title = title;
        this.imageResId = imageResId;
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public int getImageResId() {
        return imageResId;
    }

    public int getCategoryPosition() {
        return categoryPosition;
    }
}