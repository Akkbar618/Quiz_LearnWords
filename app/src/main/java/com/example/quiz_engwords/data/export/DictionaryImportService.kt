package com.example.quiz_engwords.data.export

import android.content.ContentResolver
import android.net.Uri
import com.example.quiz_engwords.data.local.dao.WordDao
import com.example.quiz_engwords.data.local.entities.WordEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStreamReader
import java.nio.charset.StandardCharsets

/**
 * Сервис для импорта словаря из JSON.
 */
class DictionaryImportService(
    private val wordDao: WordDao,
    private val contentResolver: ContentResolver
) {
    
    /**
     * Импортирует слова из JSON файла по указанному Uri.
     * 
     * Логика merge:
     * - Ключ дубликата: category + normalizedOriginal (trim + lowercase)
     * - Если найден дубликат: обновить translation, НЕ трогая прогресс/статы
     * - Если не найден: вставить новое слово
     * 
     * @param uri Uri файла, полученный от ACTION_OPEN_DOCUMENT
     * @return ImportResult с информацией о добавленных/обновлённых/пропущенных
     */
    suspend fun importDictionary(uri: Uri): ImportResult = withContext(Dispatchers.IO) {
        try {
            // 1. Читаем JSON из файла
            val jsonString = contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream, StandardCharsets.UTF_8).readText()
            } ?: return@withContext ImportResult.error("Не удалось открыть файл")
            
            if (jsonString.isBlank()) {
                return@withContext ImportResult.error("Файл пуст")
            }
            
            // 2. Парсим DTO
            val exportDto = try {
                JsonProvider.json.decodeFromString(
                    DictionaryExportDto.serializer(),
                    jsonString
                )
            } catch (e: Exception) {
                return@withContext ImportResult.error("Неверный формат JSON: ${e.message}")
            }
            
            // 3. Проверяем schema version
            if (exportDto.schema != DictionaryExportDto.CURRENT_SCHEMA_VERSION) {
                return@withContext ImportResult.error(
                    "Неподдерживаемая версия схемы: ${exportDto.schema}"
                )
            }
            
            if (exportDto.words.isEmpty()) {
                return@withContext ImportResult.error("Файл не содержит слов")
            }
            
            // 4. Обрабатываем слова
            var addedCount = 0
            var updatedCount = 0
            var skippedCount = 0
            
            for (wordDto in exportDto.words) {
                val result = processWord(wordDto)
                when (result) {
                    WordProcessResult.ADDED -> addedCount++
                    WordProcessResult.UPDATED -> updatedCount++
                    WordProcessResult.SKIPPED -> skippedCount++
                }
            }
            
            ImportResult.success(addedCount, updatedCount, skippedCount)
            
        } catch (e: Exception) {
            ImportResult.error("Ошибка импорта: ${e.localizedMessage ?: e.message ?: "Неизвестная ошибка"}")
        }
    }
    
    /**
     * Обрабатывает одно слово: валидация, поиск дубликата, insert/update.
     */
    private suspend fun processWord(wordDto: WordDto): WordProcessResult {
        // Валидация
        val original = wordDto.original.trim()
        val translation = wordDto.translation.trim()
        val category = wordDto.category.trim()
        
        if (original.isBlank() || translation.isBlank()) {
            return WordProcessResult.SKIPPED
        }
        
        // Поиск дубликата по normalized key (category + original)
        val existingWord = wordDao.findByNormalizedOriginalAndCategory(original, category)
        
        return if (existingWord != null) {
            // UPDATE: обновляем только original, translate, category
            // НЕ трогаем прогресс/статы (difficultyLevel, correctAttempts, wrongAttempts, lastReviewTime)
            val updatedWord = existingWord.copy(
                original = original,
                translate = translation,
                category = category
                // Прогресс остаётся прежним!
            )
            wordDao.updateWord(updatedWord)
            WordProcessResult.UPDATED
        } else {
            // INSERT: новое слово с дефолтным прогрессом
            val newWord = WordEntity(
                original = original,
                translate = translation,
                category = category,
                difficultyLevel = 0,
                correctAttempts = 0,
                wrongAttempts = 0,
                lastReviewTime = 0,
                createdAt = System.currentTimeMillis(),
                isCustom = true  // Импортированные слова считаются кастомными
            )
            wordDao.insertWord(newWord)
            WordProcessResult.ADDED
        }
    }
    
    private enum class WordProcessResult {
        ADDED, UPDATED, SKIPPED
    }
}
