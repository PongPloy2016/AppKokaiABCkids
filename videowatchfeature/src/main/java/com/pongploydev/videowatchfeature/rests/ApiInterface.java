package com.pongploydev.videowatchfeature.rests;




import com.pongploydev.videowatchfeature.callbacks.CallbackListVideo;
import com.pongploydev.videowatchfeature.callbacks.CallbackVideoDetail;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.Query;

public interface ApiInterface {

    String CACHE = "Cache-Control: max-age=0";
    String AGENT = "Data-Agent: Your Videos Channel";

    @Headers({CACHE, AGENT})
    @GET("api/get_videos")
    Call<CallbackListVideo> getVideos(
            @Query("page") int page,
            @Query("count") int count,
            @Query("sort") String sort,
            @Query("api_key") String api_key
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
//    @Headers({CACHE, AGENT})
//    @GET("api/get_category_videos")
//    Call<CallbackCategoryDetails> getCategoryVideos(
//            @Query("id") int id,
//            @Query("page") int page,
//            @Query("count") int count,
//            @Query("sort") String sort,
//            @Query("api_key") String api_key
//    );
    @Headers({CACHE, AGENT})
    @GET("api/get_search_results")
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

}
