package com.example.quiz_engwords.data.export

import kotlinx.serialization.json.Json

/**
 * Единая конфигурация Json для всего приложения.
 * 
 * - ignoreUnknownKeys: forward-compatible, игнорирует неизвестные поля
 * - prettyPrint: читаемый JSON для экспорта
 * - encodeDefaults: включает дефолтные значения
 */
object JsonProvider {
    
    val json: Json = Json {
        ignoreUnknownKeys = true
        prettyPrint = true
        encodeDefaults = true
        isLenient = true
    }
}
