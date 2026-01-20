package com.example.quiz_engwords.presentation.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_engwords.data.repository.WordRepository
import com.example.quiz_engwords.domain.model.Question
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel для экрана викторины.
 * 
 * Управляет состоянием UI и бизнес-логикой викторины.
 */
class QuizViewModel(
    private val repository: WordRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(QuizUiState())
    val uiState: StateFlow<QuizUiState> = _uiState.asStateFlow()
    
    init {
        loadNextQuestion()
    }
    
    /**
     * Обработка UI событий.
     */
    fun onEvent(event: QuizUiEvent) {
        when (event) {
            is QuizUiEvent.AnswerSelected -> onAnswerSelected(event.answerIndex)
            is QuizUiEvent.NextQuestion -> loadNextQuestion()
            is QuizUiEvent.SkipQuestion -> skipQuestion()
            is QuizUiEvent.RetryQuiz -> retryQuiz()
        }
    }
    
    /**
     * Загрузить следующий вопрос.
     */
    private fun loadNextQuestion() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            try {
                val question = repository.getQuizQuestion(variantCount = 4)
                
                if (question == null) {
                    // Викторина завершена
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            isQuizCompleted = true,
                            currentQuestion = null
                        )
                    }
                } else {
                    _uiState.update {
                        it.copy(
                            isLoading = false,
                            currentQuestion = question,
                            selectedAnswerIndex = null,
                            isAnswerRevealed = false,
                            isCorrectAnswer = null
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Ошибка загрузки вопроса: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * Обработать выбор ответа пользователем.
     */
    private fun onAnswerSelected(answerIndex: Int) {
        val currentState = _uiState.value
        
        // Игнорируем клики если ответ уже выбран
        if (currentState.isAnswerRevealed) return
        
        val question = currentState.currentQuestion ?: return
        val isCorrect = question.checkAnswer(answerIndex)
        
        // Обновляем UI
        _uiState.update {
            it.copy(
                selectedAnswerIndex = answerIndex,
                isAnswerRevealed = true,
                isCorrectAnswer = isCorrect,
                questionsAnswered = it.questionsAnswered + 1,
                correctAnswersCount = if (isCorrect) it.correctAnswersCount + 1 else it.correctAnswersCount
            )
        }
        
        // Сохраняем прогресс в БД
        viewModelScope.launch {
            try {
                repository.submitAnswer(question.correctWord.id, isCorrect)
            } catch (e: Exception) {
                // Логируем ошибку, но не блокируем UI
                e.printStackTrace()
            }
        }
    }
    
    /**
     * Пропустить вопрос (не влияет на статистику).
     */
    private fun skipQuestion() {
        loadNextQuestion()
    }
    
    /**
     * Начать викторину заново.
     */
    private fun retryQuiz() {
        _uiState.update {
            QuizUiState()  // Сброс состояния
        }
        loadNextQuestion()
    }
}
