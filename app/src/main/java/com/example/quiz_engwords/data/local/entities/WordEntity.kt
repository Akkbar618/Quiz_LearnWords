package com.example.quiz_engwords.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entity для хранения слов в базе данных Room.
 * 
 * Включает поля для реализации алгоритма Spaced Repetition:
 * - difficultyLevel: уровень сложности (0-5), определяет частоту показа
 * - correctAttempts/wrongAttempts: статистика правильных/неправильных ответов
 * - lastReviewTime: время последнего показа слова
 */
@Entity(tableName = "words")
data class WordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    @ColumnInfo(name = "original")
    val original: String,
    
    @ColumnInfo(name = "translate")
    val translate: String,
    
    @ColumnInfo(name = "category")
    val category: String,
    
    // Spaced Repetition fields
    @ColumnInfo(name = "difficulty_level", defaultValue = "0")
    val difficultyLevel: Int = 0, // 0-5: чем выше, тем реже показывать
    
    @ColumnInfo(name = "correct_attempts", defaultValue = "0")
    val correctAttempts: Int = 0,
    
    @ColumnInfo(name = "wrong_attempts", defaultValue = "0")
    val wrongAttempts: Int = 0,
    
    @ColumnInfo(name = "last_review_time", defaultValue = "0")
    val lastReviewTime: Long = 0, // Timestamp в миллисекундах
    
    // Metadata
    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
    
    @ColumnInfo(name = "is_custom", defaultValue = "0")
    val isCustom: Boolean = false // true если добавлено пользователем
)
