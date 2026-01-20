package com.example.quiz_engwords.di

import android.content.Context
import com.example.quiz_engwords.data.local.database.AppDatabase

/**
 * Singleton для предоставления зависимостей.
 * Простой вариант DI без Hilt (для начала).
 */
object AppModule {
    
    @Volatile
    private var database: AppDatabase? = null
    
    /**
     * Получить экземпляр БД.
     */
    fun provideDatabase(context: Context): AppDatabase {
        return database ?: synchronized(this) {
            val instance = AppDatabase.getDatabase(context)
            database = instance
            instance
        }
    }
    
    /**
     * Получить WordDao.
     */
    fun provideWordDao(context: Context) = provideDatabase(context).wordDao()
    
    /**
     * Получить CategoryDao.
     */
    fun provideCategoryDao(context: Context) = provideDatabase(context).categoryDao()
}
