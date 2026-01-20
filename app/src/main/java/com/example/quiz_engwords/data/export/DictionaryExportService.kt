package com.example.quiz_engwords.data.export

import android.content.ContentResolver
import android.net.Uri
import com.example.quiz_engwords.data.export.WordDtoMapper.toDtoList
import com.example.quiz_engwords.data.repository.WordRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.OutputStreamWriter
import java.nio.charset.StandardCharsets

/**
 * Сервис для экспорта словаря в JSON.
 */
class DictionaryExportService(
    private val repository: WordRepository,
    private val contentResolver: ContentResolver
) {
    
    /**
     * Экспортирует все слова из словаря в JSON файл по указанному Uri.
     * 
     * @param uri Uri файла, полученный от ACTION_CREATE_DOCUMENT
     * @return ExportResult с информацией об успехе/ошибке и количеством экспортированных слов
     */
    suspend fun exportDictionary(uri: Uri): ExportResult = withContext(Dispatchers.IO) {
        try {
            // 1. Получаем все слова из репозитория
            val words = repository.getAllWords()
            
            if (words.isEmpty()) {
                return@withContext ExportResult.error("Словарь пуст, нечего экспортировать")
            }
            
            // 2. Маппим в DTO
            val wordDtos = words.toDtoList()
            
            // 3. Создаём DTO для всего документа
            val exportDto = DictionaryExportDto(
                schema = DictionaryExportDto.CURRENT_SCHEMA_VERSION,
                exportedAt = System.currentTimeMillis(),
                words = wordDtos
            )
            
            // 4. Сериализуем в JSON
            val jsonString = JsonProvider.json.encodeToString(
                DictionaryExportDto.serializer(),
                exportDto
            )
            
            // 5. Записываем в файл через ContentResolver
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream, StandardCharsets.UTF_8).use { writer ->
                    writer.write(jsonString)
                    writer.flush()
                }
            } ?: return@withContext ExportResult.error("Не удалось открыть файл для записи")
            
            ExportResult.success(wordDtos.size)
            
        } catch (e: Exception) {
            ExportResult.error("Ошибка экспорта: ${e.localizedMessage ?: e.message ?: "Неизвестная ошибка"}")
        }
    }
}
