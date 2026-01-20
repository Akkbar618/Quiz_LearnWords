package com.example.quiz_engwords.data.export

import com.example.quiz_engwords.domain.model.Word

/**
 * Маппер между Word (domain) и WordDto (export).
 */
object WordDtoMapper {
    
    /**
     * Конвертирует domain Word в DTO для экспорта.
     * Экспортирует только original, translation, category.
     */
    fun Word.toDto(): WordDto = WordDto(
        original = this.original.trim(),
        translation = this.translate.trim(),
        category = this.category.trim()
    )
    
    /**
     * Конвертирует список Word в список DTO.
     */
    fun List<Word>.toDtoList(): List<WordDto> = this.map { it.toDto() }
}
