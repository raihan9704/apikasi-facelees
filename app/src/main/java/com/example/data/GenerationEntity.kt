package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "generations")
data class GenerationEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val prompt: String,
    val enhancedPrompt: String,
    val styleName: String,
    val colorPalette: String, // comma separated hexes, e.g., "#123456,#654321"
    val renderJson: String, // The structured JSON representing elements to render on Canvas
    val isFavorite: Boolean = false,
    val mode: String = "Normal", // Normal, Islamic Kids, etc.
    val isSticker: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
