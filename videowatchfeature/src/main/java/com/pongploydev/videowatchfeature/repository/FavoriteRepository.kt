package com.pongploydev.videowatchfeature.repository

import com.pongploydev.videowatchfeature.databases.room.FavoriteDao
import com.pongploydev.videowatchfeature.databases.room.FavoriteEntity
import kotlinx.coroutines.flow.Flow

class FavoriteRepository(private val favoriteDao: FavoriteDao) {

    val allFavorites: Flow<List<FavoriteEntity>> = favoriteDao.getAllFavorites()

    suspend fun insert(favoriteEntity: FavoriteEntity) {
        favoriteDao.insert(favoriteEntity)
    }

    suspend fun delete(vid: String) {
        favoriteDao.delete(vid)
    }

    fun checkFavorite(vid: String): Flow<Boolean> {
        return favoriteDao.checkFavorite(vid)
    }
}
