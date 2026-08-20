package com.pongploydev.videowatchfeaturesupabase.rests;




import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackCategoryDetails;
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideo;
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36";

    @Headers({AGENT})
    @GET("api/videolist")
    Call<CallbackListVideo> getVideos(
            @Header("Cache-Control") String cacheControl,
            @Query("get_videos") String get_videos,
            @Query("sort") String sort,
            @Query("count") int count,
            @Query("page") int page
    );

//    @Headers({CACHE, AGENT})
//    @GET("api/get_post_detail")
//    Call<CallbackVideoDetail> getVideoDetail(
//            @Query("id") String id
//    );
//
    @Headers({CACHE, AGENT})
    @GET("api/get_watch_video_detail")
    Call<CallbackVideoDetail> getWatchVideoDetail(
            @Query("id") String id
    );




//    @Headers({CACHE, AGENT})
//    @GET("api/get_category_index")
//    Call<CallbackCategories> getAllCategories(
//            @Query("api_key") String api_key
//    );
//
    @Headers({CACHE, AGENT})
    @GET("api/get_category_videos/")
    Call<CallbackCategoryDetails> getCategoryVideos(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("api_key") String api_key
    );
    @Headers({CACHE, AGENT})
    @GET("api/get_search_results/")
    Call<CallbackListVideo> getSearchPosts(
            @Query("search") String search,
            @Query("count") int count,
            @Query("api_key") String api_key
    );
//
//    @Headers({CACHE, AGENT})
//    @GET("api/get_config")
//    Call<CallbackSettings> getConfig(
//            @Query("package_name") String package_name,
//            @Query("api_key") String api_key
//    );

//
//    @Headers({CACHE, AGENT})
//    @GET("api/get_news_detail")
//    Call<CallbackPostDetail> getPostDetail(
//           @Query("api_key") String api_key,
//            @Query("id") long id
//    );
//
//    @Headers({CACHE, AGENT})
//    @GET("api/get_recent_posts")
//    Call<CallbackRecent> getRecentPost(
//            @Query("api_key") String api_key,
//            @Query("page") int page,
//            @Query("count") int count
//    );
//
//    @Headers({CACHE, AGENT})
//    @GET("api/get_config")
//    Call<CallbackSettings> getConfig(
//            @Query("package_name") String package_name,
//            @Query("api_key") String api_key
//    );

    @GET("api/get_category_videos/")
    Call<String> getCategoryVideosRaw(
            @Query("id") int id,
            @Query("page") int page,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("api_key") String api_key
    );
}
