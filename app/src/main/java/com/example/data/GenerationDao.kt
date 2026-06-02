package com.example.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

@Dao
interface GenerationDao {
    @Query("SELECT * FROM generations ORDER BY timestamp DESC")
    fun getAllGenerations(): Flow<List<GenerationEntity>>

    @Query("SELECT * FROM generations WHERE isFavorite = 1 ORDER BY timestamp DESC")
    fun getFavoriteGenerations(): Flow<List<GenerationEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertGeneration(generation: GenerationEntity): Long

    @Update
    suspend fun updateGeneration(generation: GenerationEntity)

    @Query("UPDATE generations SET isFavorite = :isFav WHERE id = :id")
    suspend fun updateFavorite(id: Long, isFav: Boolean)

    @Query("DELETE FROM generations WHERE id = :id")
    suspend fun deleteGenerationById(id: Long)

    @Query("DELETE FROM generations")
    suspend fun clearAllHistory()
}
