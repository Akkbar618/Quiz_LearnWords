package com.example.quiz_engwords.domain.model

/**
 * Domain модель слова (чистая, без Room аннотаций).
 */
data class Word(
    val id: Long = 0,
    val original: String,
    val translate: String,
    val category: String,
    val difficultyLevel: Int = 0,
    val correctAttempts: Int = 0,
    val wrongAttempts: Int = 0,
    val lastReviewTime: Long = 0,
    val isCustom: Boolean = false
) {
    /**
     * Процент правильных ответов.
     */
    val accuracy: Float
        get() {
            val total = correctAttempts + wrongAttempts
            return if (total > 0) {
                (correctAttempts.toFloat() / total) * 100
            } else 0f
        }
    
    /**
     * Слово считается изученным если difficultyLevel >= 4.
     */
    val isLearned: Boolean
        get() = difficultyLevel >= 4
}
