package com.pongploydev.videowatchfeature.databases.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(favoriteEntity: FavoriteEntity)

    @Query("DELETE FROM tbl_favorite WHERE vid = :vid") suspend fun delete(vid: String)

    @Query("SELECT * FROM tbl_favorite ORDER BY date_time DESC")
    fun getAllFavorites(): Flow<List<FavoriteEntity>>

    @Query("SELECT EXISTS(SELECT 1 FROM tbl_favorite WHERE vid = :vid)")
    fun checkFavorite(vid: String): Flow<Boolean>
}
