package com.pongploydev.videowatchfeature.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.pongploydev.videowatchfeature.databases.room.AppDatabase
import com.pongploydev.videowatchfeature.model.Video
import com.pongploydev.videowatchfeature.repository.FavoriteRepository
import kotlinx.coroutines.flow.map

class FavoritesViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: FavoriteRepository
    val allFavorites: LiveData<List<Video>>

    init {
        val favoriteDao = AppDatabase.getDatabase(application).favoriteDao()
        repository = FavoriteRepository(favoriteDao)

        // Convert Flow<List<FavoriteEntity>> to LiveData<List<Video>>
        allFavorites =
                repository
                        .allFavorites
                        .map { entities -> entities.map { it.toVideo() } }
                        .asLiveData()
    }
}
