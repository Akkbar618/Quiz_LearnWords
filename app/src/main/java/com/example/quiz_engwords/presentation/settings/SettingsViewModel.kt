package com.example.quiz_engwords.presentation.settings

import android.content.ContentResolver
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.quiz_engwords.data.export.DictionaryExportService
import com.example.quiz_engwords.data.export.DictionaryImportService
import com.example.quiz_engwords.data.local.dao.WordDao
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
    data class ExportToUri(val uri: Uri) : SettingsEvent()
    data class ImportFromUri(val uri: Uri) : SettingsEvent()
    data class ThemeChanged(val isDark: Boolean) : SettingsEvent()
    object ClearMessages : SettingsEvent()
}

/**
 * ViewModel для Settings экрана.
 */
class SettingsViewModel(
    private val repository: WordRepository,
    private val wordDao: WordDao,
    private val contentResolver: ContentResolver
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()
    
    private val exportService by lazy {
        DictionaryExportService(repository, contentResolver)
    }
    
    private val importService by lazy {
        DictionaryImportService(wordDao, contentResolver)
    }
    
    fun onEvent(event: SettingsEvent) {
        when (event) {
            is SettingsEvent.ExportToUri -> exportToUri(event.uri)
            is SettingsEvent.ImportFromUri -> importFromUri(event.uri)
            is SettingsEvent.ThemeChanged -> updateTheme(event.isDark)
            is SettingsEvent.ClearMessages -> clearMessages()
        }
    }
    
    /**
     * Экспорт словаря в JSON файл по указанному Uri.
     */
    private fun exportToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, exportMessage = null) }
            
            val result = exportService.exportDictionary(uri)
            
            if (result.success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        exportMessage = "Экспортировано ${result.exportedCount} слов"
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.errorMessage ?: "Ошибка экспорта"
                    )
                }
            }
        }
    }
    
    /**
     * Импорт словаря из JSON файла по указанному Uri.
     */
    private fun importFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null, importMessage = null) }
            
            val result = importService.importDictionary(uri)
            
            if (result.success) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        importMessage = result.toUserMessage()
                    )
                }
            } else {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        error = result.errorMessage ?: "Ошибка импорта"
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
