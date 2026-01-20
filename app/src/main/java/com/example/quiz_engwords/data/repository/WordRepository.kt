package com.example.quiz_engwords.data.repository

import com.example.quiz_engwords.data.local.dao.CategoryDao
import com.example.quiz_engwords.data.local.dao.WordDao
import com.example.quiz_engwords.data.mapper.toCategoryDomainList
import com.example.quiz_engwords.data.mapper.toDomain
import com.example.quiz_engwords.data.mapper.toDomainList
import com.example.quiz_engwords.data.mapper.toEntity
import com.example.quiz_engwords.domain.model.Category
import com.example.quiz_engwords.domain.model.Question
import com.example.quiz_engwords.domain.model.Statistics
import com.example.quiz_engwords.domain.model.Word

/**
 * Repository для работы со словами и категориями.
 * 
 * Слой абстракции между UI и Data источниками (Room Database).
 * Использует Spaced Repetition алгоритм для умного выбора слов.
 */
class WordRepository(
    private val wordDao: WordDao,
    private val categoryDao: CategoryDao
) {
    
    // ==================== Quiz Logic ====================
    
    /**
     * Получить вопрос для викторины.
     * 
     * @param variantCount количество вариантов ответа (по умолчанию 4)
     * @return Question или null если слов недостаточно
     */
    suspend fun getQuizQuestion(variantCount: Int = 4): Question? {
        // Получаем одно слово с учетом Spaced Repetition
        val wordEntity = wordDao.getWordsForQuiz(1).firstOrNull() ?: return null
        
        // Получаем случайные слова для неправильных вариантов
        val distractorEntities = wordDao.getRandomDistractors(
            excludeId = wordEntity.id,
            count = variantCount - 1
        )
        
        // Проверяем, что получили достаточно слов
        if (distractorEntities.size < variantCount - 1) {
            return null
        }
        
        // Объединяем и перемешиваем
        val allWordEntities = (distractorEntities + wordEntity).shuffled()
        
        return Question(
            correctWord = wordEntity.toDomain(),
            variants = allWordEntities.toDomainList()
        )
    }
    
    /**
     * Обработать ответ пользователя.
     * 
     * @param wordId ID слова
     * @param isCorrect правильный ли ответ
     */
    suspend fun submitAnswer(wordId: Long, isCorrect: Boolean) {
        val timestamp = System.currentTimeMillis()
        if (isCorrect) {
            wordDao.markCorrect(wordId, timestamp)
        } else {
            wordDao.markWrong(wordId, timestamp)
        }
    }
    
    // ==================== Statistics ====================
    
    /**
     * Получить статистику обучения.
     */
    suspend fun getStatistics(): Statistics {
        val totalWords = wordDao.getTotalWordsCount()
        val learnedWords = wordDao.getLearnedWordsCount()
        val inProgressWords = wordDao.getInProgressWordsCount()
        val notStartedWords = wordDao.getNotStartedWordsCount()
        
        // Подсчет общих правильных/неправильных ответов
        val allWords = wordDao.getAllWords()
        val totalCorrect = allWords.sumOf { it.correctAttempts }
        val totalWrong = allWords.sumOf { it.wrongAttempts }
        
        return Statistics(
            totalWords = totalWords,
            learnedWords = learnedWords,
            inProgressWords = inProgressWords,
            notStartedWords = notStartedWords,
            totalCorrectAnswers = totalCorrect,
            totalWrongAnswers = totalWrong
        )
    }
    
    // ==================== Words CRUD ====================
    
    /**
     * Получить все слова.
     */
    suspend fun getAllWords(): List<Word> {
        return wordDao.getAllWords().toDomainList()
    }
    
    /**
     * Получить слова по категории.
     */
    suspend fun getWordsByCategory(category: String): List<Word> {
        return wordDao.getWordsByCategory(category).toDomainList()
    }
    
    /**
     * Получить слово по ID.
     */
    suspend fun getWordById(wordId: Long): Word? {
        return wordDao.getWordById(wordId)?.toDomain()
    }
    
    /**
     * Добавить новое слово (кастомное).
     */
    suspend fun addWord(word: Word): Long {
        return wordDao.insertWord(word.toEntity())
    }
    
    /**
     * Обновить слово.
     */
    suspend fun updateWord(word: Word) {
        wordDao.updateWord(word.toEntity())
    }
    
    /**
     * Удалить слово.
     */
    suspend fun deleteWord(wordId: Long) {
        wordDao.deleteWord(wordId)
    }
    
    // ==================== Categories ====================
    
    /**
     * Получить все категории.
     */
    suspend fun getAllCategories(): List<Category> {
        return categoryDao.getAllCategories().toCategoryDomainList()
    }
    
    /**
     * Получить категорию по имени.
     */
    suspend fun getCategoryByName(name: String): Category? {
        return categoryDao.getCategoryByName(name)?.toDomain()
    }
    
    /**
     * Получить количество слов в категории.
     */
    suspend fun getWordsCountByCategory(category: String): Int {
        return wordDao.getWordsCountByCategory(category)
    }
}
