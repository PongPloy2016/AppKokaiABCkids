package com.pongploydev.videowatchfeature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pongploydev.videowatchfeature.data.repository.VideoListRepositoryImpl
import com.pongploydev.videowatchfeature.databases.prefs.SharedPref
import com.pongploydev.videowatchfeature.domain.usecase.GetVideoListUseCase
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.rests.RestAdapter
import kotlinx.coroutines.launch

class VideoListViewModel(application: Application) : AndroidViewModel(application) {

    private val sharedPref = SharedPref(application)

    // LiveData
    private val _videoList = MutableLiveData<List<Video>>()
    val videoList: LiveData<List<Video>> = _videoList

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    // Internal State
    private var originalVideos = mutableListOf<Video>()
    var postTotal = 0
    var currentCategory = "all"
    var searchQuery = ""

    private val getVideoListUseCase: GetVideoListUseCase

    init {
        val apiInterfaceV1 = RestAdapter.createAPIV1(application, sharedPref.baseUrl)
        val videoListRepo = VideoListRepositoryImpl(apiInterfaceV1)
        getVideoListUseCase = GetVideoListUseCase(videoListRepo)
    }

    fun fetchVideos(pageNo: Int, isRefresh: Boolean = false) {
        // If loading first page or refreshing, set loading state
        if (pageNo == 1) {
            _isLoading.value = true
        }

        val cacheControl = if (isRefresh) "no-cache" else null

        viewModelScope.launch {
            val result = getVideoListUseCase(cacheControl, "latest", 10, pageNo)
            
            // Only hide loading if first page or when request finishes
            if (pageNo == 1) {
                _isLoading.value = false
            }
            
            result.onSuccess { response ->
                postTotal = response.countTotal
                val posts = response.posts

                if (pageNo == 1) {
                    originalVideos.clear()
                    originalVideos.addAll(posts)
                } else {
                    originalVideos.addAll(posts)
                }

                // Apply filters if any
                filterVideos(searchQuery, currentCategory)
                _errorMessage.value = null // Clear error
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Failed to load videos"
            }
        }
    }

    fun filterVideos(query: String, category: String) {
        searchQuery = query
        currentCategory = category

        if (originalVideos.isEmpty()) {
            _videoList.value = emptyList()
            return
        }

        // Use the Video model's robust filtering logic
        val filtered = originalVideos.filter {
            it.matchesFilters(query, category, false, 0f)
        }

        _videoList.value = filtered
    }

    fun resetData() {
        originalVideos.clear()
        _videoList.value = emptyList()
    }
}
