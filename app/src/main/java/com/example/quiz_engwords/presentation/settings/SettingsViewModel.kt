package com.example.quiz_engwords.presentation.settings

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_engwords.data.repository.WordRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * UI State для Settings экрана.
 */
data class SettingsUiState(
    val isLoading: Boolean = false,
    val isDarkTheme: Boolean = false,
    val exportMessage: String? = null,
    val importMessage: String? = null,
    val error: String? = null
)

/**
 * Events для Settings экрана.
 */
sealed class SettingsEvent {
    object ExportData : SettingsEvent()
    data class ImportData(val jsonContent: String) : SettingsEvent()
    data class ThemeChanged(val isDark: Boolean) : SettingsEvent()
    object ClearMessages : SettingsEvent()
}

/**
 * ViewModel для Settings экрана.
 */
class SettingsViewModel(
    private val repository: WordRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ExportData -> exportData()
            is SettingsEvent.ImportData -> importData(event.jsonContent)
            is SettingsEvent.ThemeChanged -> updateTheme(event.isDark)
            is SettingsEvent.ClearMessages -> clearMessages()
        }
    }
    
    /**
     * Экспорт данных в JSON.
     * Возвращает JSON string через uiState.
     */
    fun exportData(): String? {
        var jsonResult: String? = null
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, exportMessage = null) }
            
            try {
                val words = repository.getAllWords()
                
                // Простой JSON export (можно улучшить с kotlinx.serialization)
                val json = buildString {
                    append("[\n")
                    words.forEachIndexed { index, word ->
                        append("  {\n")
                        append("    \"original\": \"${word.original}\",\n")
                        append("    \"translate\": \"${word.translate}\",\n")
                        append("    \"category\": \"${word.category}\"\n")
                        append("  }")
                        if (index < words.size - 1) append(",")
                        append("\n")
                    }
                    append("]")
                }
                
                jsonResult = json
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        exportMessage = "Exported ${words.size} words"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Export failed: ${e.message}"
                    )
                }
            }
        }
        return jsonResult
    }
    
    /**
     * Импорт данных из JSON.
     */
    private fun importData(jsonContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, importMessage = null) }
            
            try {
                // TODO: Реализовать парсинг с kotlinx.serialization
                // Сейчас просто заглушка
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        importMessage = "Import functionality coming soon"
                    )
                }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = "Import failed: ${e.message}"
                    )
                }
            }
        }
    }
    
    private fun updateTheme(isDark: Boolean) {
        _uiState.update { it.copy(isDarkTheme = isDark) }
        // TODO: Сохранить в DataStore
    }
    
    private fun clearMessages() {
        _uiState.update {
            it.copy(exportMessage = null, importMessage = null, error = null)
        }
    }
}
