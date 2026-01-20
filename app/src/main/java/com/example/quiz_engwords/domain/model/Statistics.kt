package com.example.quiz_engwords.domain.model

/**
 * Domain модель статистики обучения.
 */
data class Statistics(
    val totalWords: Int = 0,
    val learnedWords: Int = 0,
    val inProgressWords: Int = 0,
    val notStartedWords: Int = 0,
    val totalCorrectAnswers: Int = 0,
    val totalWrongAnswers: Int = 0
) {
    /**
     * Прогресс обучения в процентах (0-100).
     */
    val progressPercentage: Float
        get() = if (totalWords > 0) {
            (learnedWords.toFloat() / totalWords) * 100
        } else 0f
    
    /**
     * Общая точность ответов в процентах.
     */
    val overallAccuracy: Float
        get() {
            val total = totalCorrectAnswers + totalWrongAnswers
            return if (total > 0) {
                (totalCorrectAnswers.toFloat() / total) * 100
            } else 0f
        }
}
