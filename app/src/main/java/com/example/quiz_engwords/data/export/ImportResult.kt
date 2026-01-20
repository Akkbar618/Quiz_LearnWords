package com.example.quiz_engwords.data.export

/**
 * Результат операции импорта словаря.
 */
data class ImportResult(
    val success: Boolean,
    val addedCount: Int = 0,
    val updatedCount: Int = 0,
    val skippedCount: Int = 0,
    val errorMessage: String? = null
) {
    val totalProcessed: Int get() = addedCount + updatedCount + skippedCount
    
    companion object {
        fun success(added: Int, updated: Int, skipped: Int) = ImportResult(
            success = true,
            addedCount = added,
            updatedCount = updated,
            skippedCount = skipped
        )
        
        fun error(message: String) = ImportResult(
            success = false,
            errorMessage = message
        )
    }
    
    /**
     * Форматированное сообщение для UI.
     */
    fun toUserMessage(): String {
        return if (success) {
            buildString {
                append("Импорт завершён: ")
                val parts = mutableListOf<String>()
                if (addedCount > 0) parts.add("$addedCount добавлено")
                if (updatedCount > 0) parts.add("$updatedCount обновлено")
                if (skippedCount > 0) parts.add("$skippedCount пропущено")
                append(parts.joinToString(", "))
            }
        } else {
            errorMessage ?: "Ошибка импорта"
        }
    }
}
