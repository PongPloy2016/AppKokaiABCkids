package com.pongploydev.videowatchfeature.callbacks;

import com.google.gson.annotations.SerializedName;
import com.pongploydev.videowatchfeature.model.Video;

import java.io.Serializable;
import java.util.List;

public class CallbackListVideo implements Serializable {
    public String status;
    public int count;
    public int count_total;
    public int pages;
    @SerializedName("posts") // ถ้า JSON เป็น posts
    public List<Video> posts;
}