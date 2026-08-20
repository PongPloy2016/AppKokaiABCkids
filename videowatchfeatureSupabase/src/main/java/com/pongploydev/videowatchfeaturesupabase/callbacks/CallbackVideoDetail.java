package com.pongploydev.videowatchfeaturesupabase.callbacks;

import com.google.gson.annotations.SerializedName;
import com.pongploydev.videowatchfeaturesupabase.model.Video;

import java.util.ArrayList;
import java.util.List;

public class CallbackVideoDetail {

    public String status = "";
    public Video post = null;
    @SerializedName("suggested") // ถ้า JSON เป็น posts
    public List<Video> suggested = new ArrayList<>();

}