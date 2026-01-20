package com.example.quiz_engwords.data.local.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.quiz_engwords.data.local.dao.CategoryDao
import com.example.quiz_engwords.data.local.dao.WordDao
import com.example.quiz_engwords.data.local.entities.CategoryEntity
import com.example.quiz_engwords.data.local.entities.WordEntity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.InputStreamReader

/**
 * Room Database для приложения Quiz_EngWords.
 * 
 * Содержит таблицы:
 * - words: слова для изучения
 * - categories: категории слов
 */
@Database(
    entities = [WordEntity::class, CategoryEntity::class],
    version = 1,
    exportSchema = true
)
abstract class AppDatabase : RoomDatabase() {
    
    abstract fun wordDao(): WordDao
    abstract fun categoryDao(): CategoryDao
    
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * Получить singleton экземпляр БД.
         */
        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "quiz_engwords_database"
                )
                    .addCallback(DatabaseCallback(context))
                    .build()
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * Callback для предзаполнения БД при первом создании.
         */
        private class DatabaseCallback(
            private val context: Context
        ) : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                
                // Запускаем в отдельной корутине
                CoroutineScope(Dispatchers.IO).launch {
                    INSTANCE?.let { database ->
                        populateDatabase(database, context)
                    }
                }
            }
        }
        
        /**
         * Предзаполнение БД из JSON файла.
         */
        private suspend fun populateDatabase(database: AppDatabase, context: Context) {
            val wordDao = database.wordDao()
            val categoryDao = database.categoryDao()
            
            try {
                // Загрузка категорий
                val categories = listOf(
                    CategoryEntity("Materials", "⚙️", "#FF6B6B", 0),
                    CategoryEntity("Medical", "⚕️", "#4ECDC4", 0),
                    CategoryEntity("General", "📚", "#95E1D3", 0),
                    CategoryEntity("Social", "👥", "#FFE66D", 0),
                    CategoryEntity("Science", "🔬", "#A8E6CF", 0),
                    CategoryEntity("Time", "⏰", "#FFD3B6", 0),
                    CategoryEntity("Family", "👨‍👩‍👧", "#FFAAA5", 0),
                    CategoryEntity("Culture", "🎭", "#FF8B94", 0),
                    CategoryEntity("Communication", "💬", "#B4A7D6", 0),
                    CategoryEntity("Language", "🗣️", "#D4A5A5", 0),
                    CategoryEntity("Fun", "🎉", "#FFDAC1", 0)
                )
                categoryDao.insertCategories(categories)
                
                // Загрузка слов из assets (если файл существует)
                try {
                    val inputStream = context.assets.open("initial_words.json")
                    val reader = InputStreamReader(inputStream)
                    val type = object : TypeToken<List<InitialWord>>() {}.type
                    val initialWords: List<InitialWord> = Gson().fromJson(reader, type)
                    
                    val words = initialWords.map { initial ->
                        WordEntity(
                            original = initial.original,
                            translate = initial.translate,
                            category = initial.category
                        )
                    }
                    wordDao.insertWords(words)
                    reader.close()
                } catch (e: Exception) {
                    // Если файла нет, загружаем дефолтные слова из кода
                    loadDefaultWords(wordDao)
                }
                
            } catch (e: Exception) {
                e.printStackTrace()
                // В случае ошибки загружаем минимальный набор
                loadDefaultWords(wordDao)
            }
        }
        
        /**
         * Загрузка дефолтных слов (из текущего LearnWordsTrainer).
         */
        private suspend fun loadDefaultWords(wordDao: WordDao) {
            val defaultWords = listOf(
                WordEntity(original = "Aluminium", translate = "Алюминий", category = "Materials"),
                WordEntity(original = "Anaesthetist", translate = "анестезиолог", category = "Medical"),
                WordEntity(original = "Anonymous", translate = "анонимный", category = "General"),
                WordEntity(original = "Ethnicity", translate = "этническая или расовая принадлежность", category = "Social"),
                WordEntity(original = "Facilitate", translate = "облегчать", category = "General"),
                WordEntity(original = "February", translate = "февраль", category = "Time"),
                WordEntity(original = "Hereditary", translate = "наследственный", category = "Science"),
                WordEntity(original = "Hospitable", translate = "гостеприимный", category = "Social"),
                WordEntity(original = "Onomatopoeia", translate = "звукоподражание", category = "Language"),
                WordEntity(original = "Particularly", translate = "в особенности", category = "General"),
                WordEntity(original = "Phenomenon", translate = "феномен", category = "Science"),
                WordEntity(original = "Philosophical", translate = "философский", category = "Culture"),
                WordEntity(original = "Prejudice", translate = "предубеждение", category = "Social"),
                WordEntity(original = "Prioritising", translate = "определение приоритетов", category = "General"),
                WordEntity(original = "Pronunciation", translate = "произношение", category = "Language"),
                WordEntity(original = "Provocatively", translate = "вызывающе", category = "Communication"),
                WordEntity(original = "Regularly", translate = "регулярно", category = "Time"),
                WordEntity(original = "Remuneration", translate = "вознаграждение", category = "General"),
                WordEntity(original = "Statistics", translate = "статистические данные", category = "Science"),
                WordEntity(original = "Thesaurus", translate = "справочник", category = "General")
            )
            wordDao.insertWords(defaultWords)
        }
    }
}

/**
 * Data class для парсинга JSON файла с начальными словами.
 */
data class InitialWord(
    val original: String,
    val translate: String,
    val category: String
)
