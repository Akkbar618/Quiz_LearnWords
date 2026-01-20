package com.example.quiz_engwords.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_engwords.data.repository.WordRepository
import com.example.quiz_engwords.domain.model.Statistics
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * UI State для Home экрана.
 */
data class HomeUiState(
    val isLoading: Boolean = true,
    val statistics: Statistics? = null,
    val streak: Int = 0,  // Количество дней подряд
    val wordsLearnedToday: Int = 0,
    val error: String? = null
)

/**
 * ViewModel для Home экрана.
 */
class HomeViewModel(
    private val repository: WordRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()
    
    init {
        loadStatistics()
    }
    
    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            
            try {
                val stats = repository.getStatistics()
                
                // TODO: Реализовать подсчет streak и слов за день
                // Пока используем заглушки
                val streak = 0  // Будет реализовано позже
                val todayWords = 0  // Будет реализовано позже
                
                _uiState.value = HomeUiState(
                    isLoading = false,
                    statistics = stats,
                    streak = streak,
                    wordsLearnedToday = todayWords
                )
            } catch (e: Exception) {
                _uiState.value = HomeUiState(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
