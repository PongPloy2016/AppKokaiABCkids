package com.pongploydev.videowatchfeaturesupabase.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.asLiveData
import com.pongploydev.videowatchfeaturesupabase.databases.room.AppDatabase
import com.pongploydev.videowatchfeaturesupabase.model.Video
import com.pongploydev.videowatchfeaturesupabase.repository.FavoriteRepository
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
