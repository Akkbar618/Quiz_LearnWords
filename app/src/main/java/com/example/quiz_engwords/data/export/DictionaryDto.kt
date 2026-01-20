package com.example.quiz_engwords.data.export

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * DTO для экспорта/импорта словаря в JSON.
 * 
 * JSON Schema v1 - forward-compatible.
 */
@Serializable
data class DictionaryExportDto(
    @SerialName("schema")
    val schema: Int = CURRENT_SCHEMA_VERSION,
    
    @SerialName("exportedAt")
    val exportedAt: Long = System.currentTimeMillis(),
    
    @SerialName("words")
    val words: List<WordDto> = emptyList()
) {
    companion object {
        const val CURRENT_SCHEMA_VERSION = 1
    }
}

/**
 * DTO для отдельного слова.
 */
@Serializable
data class WordDto(
    @SerialName("original")
    val original: String,
    
    @SerialName("translation")
    val translation: String,
    
    @SerialName("category")
    val category: String
)
