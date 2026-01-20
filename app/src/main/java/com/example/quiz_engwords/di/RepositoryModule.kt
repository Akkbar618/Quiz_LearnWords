package com.example.quiz_engwords.di

import android.content.Context
import com.example.quiz_engwords.data.repository.WordRepository

/**
 * Singleton для предоставления Repository.
 */
object RepositoryModule {
    
    @Volatile
    private var wordRepository: WordRepository? = null
    
    /**
     * Получить экземпляр WordRepository.
     */
    fun provideWordRepository(context: Context): WordRepository {
        return wordRepository ?: synchronized(this) {
            val instance = WordRepository(
                wordDao = AppModule.provideWordDao(context),
                categoryDao = AppModule.provideCategoryDao(context)
            )
            wordRepository = instance
            instance
        }
    }
}
