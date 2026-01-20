package com.example.quiz_engwords.presentation.quiz

import com.example.quiz_engwords.domain.model.Question
import com.example.quiz_engwords.domain.model.Word

/**
 * UI State для экрана викторины.
 */
data class QuizUiState(
    val isLoading: Boolean = true,
    val currentQuestion: Question? = null,
    val selectedAnswerIndex: Int? = null,
    val isAnswerRevealed: Boolean = false,
    val isCorrectAnswer: Boolean? = null,
    val questionsAnswered: Int = 0,
    val correctAnswersCount: Int = 0,
    val error: String? = null,
    val isQuizCompleted: Boolean = false
) {
    /**
     * Прогресс в формате "3/20".
     */
    val progressText: String
        get() = "$correctAnswersCount/$questionsAnswered"
    
    /**
     * Процент прогресса (0.0 - 1.0) для прогресс-бара.
     */
    val progressPercent: Float
        get() = if (questionsAnswered > 0) {
            correctAnswersCount.toFloat() / questionsAnswered
        } else 0f
}

/**
 * UI Events для викторины.
 */
sealed class QuizUiEvent {
    data class AnswerSelected(val answerIndex: Int) : QuizUiEvent()
    object NextQuestion : QuizUiEvent()
    object SkipQuestion : QuizUiEvent()
    object RetryQuiz : QuizUiEvent()
}

/**
 * Состояние кнопки ответа.
 */
enum class AnswerButtonState {
    NEUTRAL,    // Обычное состояние
    CORRECT,    // Правильный ответ
    WRONG       // Неправильный ответ
}
