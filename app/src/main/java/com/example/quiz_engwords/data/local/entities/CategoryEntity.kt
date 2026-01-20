package com.example.quiz_engwords.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для категорий слов.
 * 
 * Примеры категорий: Business, Travel, Technology, Materials, Time, etc.
 */
@Entity(tableName = "categories")
data class CategoryEntity(
    @PrimaryKey
    @ColumnInfo(name = "name")
    val name: String,
    
    @ColumnInfo(name = "icon")
    val icon: String, // Emoji или имя drawable ресурса
    
    @ColumnInfo(name = "color")
    val color: String, // Hex цвет для UI (#FF5733)
    
    @ColumnInfo(name = "total_words", defaultValue = "0")
    val totalWords: Int = 0
)
