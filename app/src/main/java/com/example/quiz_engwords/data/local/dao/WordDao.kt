package com.example.quiz_engwords.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.quiz_engwords.data.local.entities.WordEntity

/**
 * DAO для работы с таблицей words.
 * 
 * Включает методы для:
 * - Получения слов для викторины с учетом Spaced Repetition
 * - Обновления прогресса обучения
 * - Статистики
 */
@Dao
interface WordDao {
    
    /**
     * Получить N слов для викторины с учетом Spaced Repetition алгоритма.
     * Приоритет отдается словам с низким difficultyLevel (неизученные/сложные).
     * 
     * Логика:
     * - difficultyLevel = 0-1 → показываем чаще (приоритет 1-2)
     * - difficultyLevel = 2-3 → показываем средне (приоритет 3)
     * - difficultyLevel = 4-5 → показываем реже
     */
    @Query("""
        SELECT * FROM words 
        WHERE difficulty_level < 5
        ORDER BY 
            (CASE 
                WHEN difficulty_level = 0 THEN 1
                WHEN difficulty_level = 1 THEN 2
                ELSE 3 
            END),
            RANDOM()
        LIMIT :count
    """)
    suspend fun getWordsForQuiz(count: Int): List<WordEntity>
    
    /**
     * Получить N случайных слов для неправильных вариантов ответов (distractors).
     * Исключает слово с excludeId.
     */
    @Query("""
        SELECT * FROM words 
        WHERE id != :excludeId 
        ORDER BY RANDOM() 
        LIMIT :count
    """)
    suspend fun getRandomDistractors(excludeId: Long, count: Int): List<WordEntity>
    
    /**
     * Обновить прогресс после ПРАВИЛЬНОГО ответа.
     * - Увеличивает difficultyLevel на 1 (макс 5)
     * - Увеличивает correctAttempts на 1
     * - Обновляет lastReviewTime
     */
    @Query("""
        UPDATE words 
        SET difficulty_level = MIN(difficulty_level + 1, 5),
            correct_attempts = correct_attempts + 1,
            last_review_time = :timestamp
        WHERE id = :wordId
    """)
    suspend fun markCorrect(wordId: Long, timestamp: Long)
    
    /**
     * Обновить прогресс после НЕПРАВИЛЬНОГО ответа.
     * - Уменьшает difficultyLevel на 1 (мин 0)
     * - Увеличивает wrongAttempts на 1
     * - Обновляет lastReviewTime
     */
    @Query("""
        UPDATE words 
        SET difficulty_level = MAX(difficulty_level - 1, 0),
            wrong_attempts = wrong_attempts + 1,
            last_review_time = :timestamp
        WHERE id = :wordId
    """)
    suspend fun markWrong(wordId: Long, timestamp: Long)
    
    // ==================== Статистика ====================
    
    /**
     * Получить количество изученных слов (difficultyLevel >= 4).
     */
    @Query("SELECT COUNT(*) FROM words WHERE difficulty_level >= 4")
    suspend fun getLearnedWordsCount(): Int
    
    /**
     * Получить общее количество слов в БД.
     */
    @Query("SELECT COUNT(*) FROM words")
    suspend fun getTotalWordsCount(): Int
    
    /**
     * Получить количество слов в процессе изучения (difficultyLevel 1-3).
     */
    @Query("SELECT COUNT(*) FROM words WHERE difficulty_level >= 1 AND difficulty_level <= 3")
    suspend fun getInProgressWordsCount(): Int
    
    /**
     * Получить количество неизученных слов (difficultyLevel = 0).
     */
    @Query("SELECT COUNT(*) FROM words WHERE difficulty_level = 0")
    suspend fun getNotStartedWordsCount(): Int
    
    // ==================== По категориям ====================
    
    /**
     * Получить все слова определенной категории.
     */
    @Query("SELECT * FROM words WHERE category = :category ORDER BY original")
    suspend fun getWordsByCategory(category: String): List<WordEntity>
    
    /**
     * Получить количество слов в категории.
     */
    @Query("SELECT COUNT(*) FROM words WHERE category = :category")
    suspend fun getWordsCountByCategory(category: String): Int
    
    // ==================== CRUD операции ====================
    
    /**
     * Вставить список слов (или заменить при конфликте).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWords(words: List<WordEntity>)
    
    /**
     * Вставить одно слово.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertWord(word: WordEntity): Long
    
    /**
     * Обновить слово.
     */
    @Update
    suspend fun updateWord(word: WordEntity)
    
    /**
     * Получить слово по ID.
     */
    @Query("SELECT * FROM words WHERE id = :wordId")
    suspend fun getWordById(wordId: Long): WordEntity?
    
    /**
     * Получить все слова.
     */
    @Query("SELECT * FROM words ORDER BY original")
    suspend fun getAllWords(): List<WordEntity>
    
    /**
     * Удалить все слова (для тестирования или сброса).
     */
    @Query("DELETE FROM words")
    suspend fun deleteAll()
    
    /**
     * Удалить слово по ID.
     */
    @Query("DELETE FROM words WHERE id = :wordId")
    suspend fun deleteWord(wordId: Long)
    
    /**
     * Найти слово по normalized original (lowercase + trim) и category.
     * Используется для определения дубликатов при импорте.
     */
    @Query("""
        SELECT * FROM words 
        WHERE LOWER(TRIM(original)) = LOWER(TRIM(:original)) 
        AND LOWER(TRIM(category)) = LOWER(TRIM(:category))
        LIMIT 1
    """)
    suspend fun findByNormalizedOriginalAndCategory(original: String, category: String): WordEntity?
}
