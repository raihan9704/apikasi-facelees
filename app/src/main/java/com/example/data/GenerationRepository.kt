package com.example.data

import kotlinx.coroutines.flow.Flow

class GenerationRepository(private val generationDao: GenerationDao) {
    val allGenerations: Flow<List<GenerationEntity>> = generationDao.getAllGenerations()
    val favoriteGenerations: Flow<List<GenerationEntity>> = generationDao.getFavoriteGenerations()

    suspend fun insert(generation: GenerationEntity): Long {
        return generationDao.insertGeneration(generation)
    }

    suspend fun update(generation: GenerationEntity) {
        generationDao.updateGeneration(generation)
    }

    suspend fun toggleFavorite(id: Long, isFavorite: Boolean) {
        generationDao.updateFavorite(id, isFavorite)
    }

    suspend fun deleteById(id: Long) {
        generationDao.deleteGenerationById(id)
    }

    suspend fun clearHistory() {
        generationDao.clearAllHistory()
    }
}
