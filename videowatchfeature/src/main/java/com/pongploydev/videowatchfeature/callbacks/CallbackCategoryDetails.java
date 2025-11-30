package com.pongploydev.videowatchfeature.callbacks;



import com.pongploydev.videowatchfeature.model.CategoryLists;
import com.pongploydev.videowatchfeature.model.Video;

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
