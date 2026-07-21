package com.pongploydev.videowatchfeature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.pongploydev.videowatchfeature.databases.room.AppDatabase
import com.pongploydev.videowatchfeature.databases.room.FavoriteEntity
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.repository.FavoriteRepository
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class FavoriteViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FavoriteRepository
    val favoriteVideos: LiveData<List<Video>>

    init {
        val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
        repository = FavoriteRepository(favoriteDao)
        
        // Convert Flow<List<FavoriteEntity>> to LiveData<List<Video>>
        favoriteVideos = repository.allFavorites.map { entities ->
            entities.map { it.toVideo() }
        }.asLiveData()
    }

    fun removeFavorite(vid: String) {
        viewModelScope.launch {
            repository.delete(vid)
        }
    }
}
