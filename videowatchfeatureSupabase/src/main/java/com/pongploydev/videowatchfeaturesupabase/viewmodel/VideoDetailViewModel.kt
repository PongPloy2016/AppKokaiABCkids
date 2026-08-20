package com.pongploydev.videowatchfeaturesupabase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackVideoDetailV1
import com.pongploydev.videowatchfeaturesupabase.callbacks.CallbackListVideoV1
import com.pongploydev.videowatchfeaturesupabase.data.repository.VideoDetailRepositoryImpl
import com.pongploydev.videowatchfeaturesupabase.data.repository.VideoListRepositoryImpl
import com.pongploydev.videowatchfeaturesupabase.databases.room.AppDatabase
import com.pongploydev.videowatchfeaturesupabase.databases.room.FavoriteEntity
import com.pongploydev.videowatchfeaturesupabase.domain.usecase.GetVideoDetailUseCase
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.repository.FavoriteRepository
import com.pongploydev.videowatchfeaturesupabase.rests.RestAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VideoDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FavoriteRepository
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    // Clean Architecture LiveData
    private val _videoDetail = MutableLiveData<CallbackVideoDetailV1?>()
    val videoDetail: LiveData<CallbackVideoDetailV1?> = _videoDetail

    private val _randomVideos = MutableLiveData<List<Video>>()
    val randomVideos: LiveData<List<Video>> = _randomVideos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val getVideoDetailUseCase: GetVideoDetailUseCase
    private val videoListRepo: VideoListRepositoryImpl

    init {
        val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
        repository = FavoriteRepository(favoriteDao)

        // Initialize Clean Architecture layers manually
        val sharedPref = com.pongploydev.videowatchfeaturesupabase.databases.prefs.SharedPref(application)
        val apiInterfaceV1 = RestAdapter.createAPIV1(application, sharedPref.baseUrl)
        val videoDetailRepo = VideoDetailRepositoryImpl(apiInterfaceV1)
        getVideoDetailUseCase = GetVideoDetailUseCase(videoDetailRepo)
        
        videoListRepo = VideoListRepositoryImpl(apiInterfaceV1)
    }

    fun loadVideoDetail(vid: String, isRefresh: Boolean = false) {
        _isLoading.value = true
        _error.value = null
        viewModelScope.launch {
            val result = getVideoDetailUseCase(vid, isRefresh)
            _isLoading.value = false
            result.onSuccess { detail ->
                _videoDetail.value = detail
            }.onFailure { exception ->
                _error.value = exception.message ?: "Failed to load video details"
            }
        }
    }

    fun loadRandomVideos() {
        viewModelScope.launch {
            val sharedPref = com.pongploydev.videowatchfeaturesupabase.databases.prefs.SharedPref(getApplication())
            val useSupabase = sharedPref.isUseSupabaseApi
            val result = videoListRepo.getRandomVideos(null, useSupabase)
            result.onSuccess { response ->
                _randomVideos.value = response.posts
            }
        }
    }

    fun checkFavoriteStatus(vid: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Collect the flow from repository
            repository.checkFavorite(vid).collect { isFav -> _isFavorite.postValue(isFav) }
        }
    }

    fun toggleFavorite(video: Video) {
        viewModelScope.launch(Dispatchers.IO) {
            val currentStatus = repository.checkFavorite(video.vid).first()
            if (currentStatus) {
                // Remove from favorite
                repository.delete(video.vid)
            } else {
                // Add to favorite
                val entity = FavoriteEntity.fromVideo(video)
                repository.insert(entity)
            }
            // Status will be updated automatically via Flow collection in checkFavoriteStatus
        }
    }
}
