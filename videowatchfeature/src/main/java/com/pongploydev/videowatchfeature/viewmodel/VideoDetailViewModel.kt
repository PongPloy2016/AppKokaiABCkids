package com.pongploydev.videowatchfeature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.pongploydev.videowatchfeature.databases.room.AppDatabase
import com.pongploydev.videowatchfeature.databases.room.FavoriteEntity
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.repository.FavoriteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class VideoDetailViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FavoriteRepository
    private val _isFavorite = MutableLiveData<Boolean>()
    val isFavorite: LiveData<Boolean> = _isFavorite

    init {
        val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
        repository = FavoriteRepository(favoriteDao)
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
