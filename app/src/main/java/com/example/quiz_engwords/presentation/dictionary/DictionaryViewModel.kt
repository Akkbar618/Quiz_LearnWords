package com.example.quiz_engwords.presentation.dictionary

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_engwords.data.repository.WordRepository
import com.example.quiz_engwords.domain.model.Category
import com.example.quiz_engwords.domain.model.Word
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State для Dictionary экрана.
 */
data class DictionaryUiState(
    val isLoading: Boolean = true,
    val words: List<Word> = emptyList(),
    val filteredWords: List<Word> = emptyList(),
    val categories: List<Category> = emptyList(),
    val searchQuery: String = "",
    val selectedCategory: String? = null,
    val selectedDifficulty: Int? = null,
    val showAddDialog: Boolean = false,
    val error: String? = null
)

/**
 * Events для Dictionary экрана.
 */
sealed class DictionaryEvent {
    data class SearchQueryChanged(val query: String) : DictionaryEvent()
    data class CategoryFilterChanged(val category: String?) : DictionaryEvent()
    data class DifficultyFilterChanged(val difficulty: Int?) : DictionaryEvent()
    data class DeleteWord(val wordId: Long) : DictionaryEvent()
    data class ResetWordProgress(val wordId: Long) : DictionaryEvent()
    data class AddWord(val original: String, val translate: String, val category: String) : DictionaryEvent()
    object ShowAddDialog : DictionaryEvent()
    object HideAddDialog : DictionaryEvent()
    object Refresh : DictionaryEvent()
}

/**
 * ViewModel для Dictionary экрана.
 */
class DictionaryViewModel(
    private val repository: WordRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(DictionaryUiState())
    val uiState: StateFlow<DictionaryUiState> = _uiState.asStateFlow()
    
    init {
        loadWords()
        loadCategories()
    }
    
    fun onEvent(event: DictionaryEvent) {
        when (event) {
            is DictionaryEvent.SearchQueryChanged -> updateSearchQuery(event.query)
            is DictionaryEvent.CategoryFilterChanged -> updateCategoryFilter(event.category)
            is DictionaryEvent.DifficultyFilterChanged -> updateDifficultyFilter(event.difficulty)
            is DictionaryEvent.DeleteWord -> deleteWord(event.wordId)
            is DictionaryEvent.ResetWordProgress -> resetProgress(event.wordId)
            is DictionaryEvent.AddWord -> addWord(event.original, event.translate, event.category)
            DictionaryEvent.ShowAddDialog -> _uiState.update { it.copy(showAddDialog = true) }
            DictionaryEvent.HideAddDialog -> _uiState.update { it.copy(showAddDialog = false) }
            DictionaryEvent.Refresh -> loadWords()
        }
    }
    
    private fun loadWords() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val words = repository.getAllWords()
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        words = words,
                        filteredWords = words
                    )
                }
                applyFilters()
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(isLoading = false, error = e.message)
                }
            }
        }
    }
    
    private fun loadCategories() {
        viewModelScope.launch {
            try {
                val categories = repository.getAllCategories()
                _uiState.update { it.copy(categories = categories) }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    
    private fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }
    
    private fun updateCategoryFilter(category: String?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }
    
    private fun updateDifficultyFilter(difficulty: Int?) {
        _uiState.update { it.copy(selectedDifficulty = difficulty) }
        applyFilters()
    }
    
    private fun applyFilters() {
        val currentState = _uiState.value
        var filtered = currentState.words
        
        // Поиск
        if (currentState.searchQuery.isNotBlank()) {
            filtered = filtered.filter {
                it.original.contains(currentState.searchQuery, ignoreCase = true) ||
                it.translate.contains(currentState.searchQuery, ignoreCase = true)
            }
        }
        
        // Фильтр по категории
        currentState.selectedCategory?.let { category ->
            filtered = filtered.filter { it.category == category }
        }
        
        // Фильтр по сложности
        currentState.selectedDifficulty?.let { difficulty ->
            filtered = filtered.filter { it.difficultyLevel == difficulty }
        }
        
        _uiState.update { it.copy(filteredWords = filtered) }
    }
    
    private fun deleteWord(wordId: Long) {
        viewModelScope.launch {
            try {
                repository.deleteWord(wordId)
                loadWords()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun resetProgress(wordId: Long) {
        viewModelScope.launch {
            try {
                val word = repository.getWordById(wordId)
                word?.let {
                    val resetWord = it.copy(
                        difficultyLevel = 0,
                        correctAttempts = 0,
                        wrongAttempts = 0,
                        lastReviewTime = 0
                    )
                    repository.updateWord(resetWord)
                    loadWords()
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
    
    private fun addWord(original: String, translate: String, category: String) {
        viewModelScope.launch {
            try {
                val newWord = Word(
                    original = original,
                    translate = translate,
                    category = category,
                    isCustom = true
                )
                repository.addWord(newWord)
                _uiState.update { it.copy(showAddDialog = false) }
                loadWords()
            } catch (e: Exception) {
                _uiState.update { it.copy(error = e.message) }
            }
        }
    }
}
