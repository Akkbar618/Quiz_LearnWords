package com.example.quiz_engwords.domain.model

/**
 * Domain модель вопроса для викторины.
 * 
 * Содержит правильное слово и список всех вариантов (включая правильный).
 */
data class Question(
    val correctWord: Word,
    val variants: List<Word>
) {
    /**
     * Получить индекс правильного ответа в списке вариантов.
     */
    fun getCorrectAnswerIndex(): Int {
        return variants.indexOfFirst { it.id == correctWord.id }
    }
    
    /**
     * Проверить, правильный ли ответ пользователя.
     */
    fun checkAnswer(selectedIndex: Int): Boolean {
        return selectedIndex == getCorrectAnswerIndex()
    }
}
