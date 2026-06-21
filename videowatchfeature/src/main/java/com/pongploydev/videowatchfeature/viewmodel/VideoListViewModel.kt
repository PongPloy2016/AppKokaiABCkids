package com.pongploydev.videowatchfeature.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.pongploydev.videowatchfeature.callbacks.CallbackListVideo
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = SharedPref(application)

    // LiveData
    private val _videoList = MutableLiveData<List<Video>>()
    val videoList: LiveData<List<Video>> = _videoList

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Internal State
    private var originalVideos = mutableListOf<Video>()
    private var callbackCall: Call<CallbackListVideo?>? = null
    var postTotal = 0
    var currentCategory = "all"
    var searchQuery = ""

    fun fetchVideos(pageNo: Int, isRefresh: Boolean = false) {
        // If loading first page or refreshing, set loading state
        if (pageNo == 1) {
            _isLoading.value = true
        }

        val apiInterface = RestAdapter.createAPI(getApplication(), sharedPref.getBaseUrl())

        val cacheControl = if (isRefresh) "no-cache" else null

        callbackCall =
                apiInterface.getVideos(
                        cacheControl,
                        "true",
                        "latest",
                        10,
                        pageNo
                )

        callbackCall?.enqueue(
                object : Callback<CallbackListVideo?> {
                    override fun onResponse(
                            call: Call<CallbackListVideo?>,
                            response: Response<CallbackListVideo?>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val videoListResponse = response.body()!!
                            postTotal = videoListResponse.count_total

                            val posts = videoListResponse.posts.filterNotNull()

                            if (pageNo == 1) {
                                originalVideos.clear()
                                originalVideos.addAll(posts)
                            } else {
                                originalVideos.addAll(posts)
                            }

                            // Apply filters if any
                            filterVideos(searchQuery, currentCategory)

                            _isLoading.value = false
                            _errorMessage.value = null // Clear error
                        } else {
                            _isLoading.value = false
                            _errorMessage.value = "Error: ${response.message()}"
                        }
                    }

                    override fun onFailure(call: Call<CallbackListVideo?>, t: Throwable) {
                        if (!call.isCanceled) {
                            Log.e("onFailure getVideos", t.message ?: "")
                            _isLoading.value = false
                            _errorMessage.value = t.message
                        }
                    }
                }
        )
    }

    fun filterVideos(query: String, category: String) {
        searchQuery = query
        currentCategory = category

        if (originalVideos.isEmpty()) {
            // If data not loaded yet, maybe trigger load?
            // But usually fetchVideos is called first.
            _videoList.value = emptyList()
            return
        }

        val filtered =
                originalVideos.filter { video ->
                    val matchesSearch =
                            query.isEmpty() ||
                                    video.video_title?.contains(query, ignoreCase = true) == true ||
                                    video.video_description?.contains(query, ignoreCase = true) ==
                                            true

                    val matchesCategory =
                            category == "all" ||
                                    video.category_name?.contains(category, ignoreCase = true) ==
                                            true

                    matchesSearch && matchesCategory
                }

        _videoList.value = filtered
    }

    fun resetData() {
        originalVideos.clear()
        _videoList.value = emptyList()
    }

    override fun onCleared() {
        super.onCleared()
        callbackCall?.cancel()
    }
}
