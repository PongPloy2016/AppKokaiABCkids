package com.pongploydev.videowatchfeaturesupabase.callbacks;



import com.pongploydev.videowatchfeaturesupabase.model.CategoryLists;
import com.pongploydev.videowatchfeaturesupabase.model.Video;

import java.util.ArrayList;
import java.util.List;

public class CallbackCategoryDetails {

    public String status = "";
    public int count = -1;
    public int count_total = -1;
    public int pages = -1;
    public CategoryLists category = null;
    public List<Video> posts = new ArrayList<>();

}
