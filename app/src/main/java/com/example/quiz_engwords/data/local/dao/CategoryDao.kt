package com.example.quiz_engwords.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.quiz_engwords.data.local.entities.CategoryEntity

/**
 * DAO для работы с таблицей categories.
 */
@Dao
interface CategoryDao {
    
    /**
     * Получить все категории, отсортированные по имени.
     */
    @Query("SELECT * FROM categories ORDER BY name")
    suspend fun getAllCategories(): List<CategoryEntity>
    
    /**
     * Получить категорию по имени.
     */
    @Query("SELECT * FROM categories WHERE name = :name")
    suspend fun getCategoryByName(name: String): CategoryEntity?
    
    /**
     * Вставить список категорий (или заменить при конфликте).
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>)
    
    /**
     * Вставить одну категорию.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity)
    
    /**
     * Удалить все категории.
     */
    @Query("DELETE FROM categories")
    suspend fun deleteAll()
}
