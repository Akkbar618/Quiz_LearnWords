package com.example.quiz_engwords.data.export

/**
 * Результат операции экспорта словаря.
 */
data class ExportResult(
    val success: Boolean,
    val exportedCount: Int = 0,
    val errorMessage: String? = null
) {
    companion object {
        fun success(count: Int) = ExportResult(
            success = true,
            exportedCount = count
        )
        
        fun error(message: String) = ExportResult(
            success = false,
            errorMessage = message
        )
    }
}
