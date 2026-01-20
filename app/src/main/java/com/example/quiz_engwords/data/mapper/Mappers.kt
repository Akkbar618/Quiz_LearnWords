package com.example.quiz_engwords.data.mapper

import com.example.quiz_engwords.data.local.entities.CategoryEntity
import com.example.quiz_engwords.data.local.entities.WordEntity
import com.example.quiz_engwords.domain.model.Category
import com.example.quiz_engwords.domain.model.Word

/**
 * Mapper для конвертации между Data и Domain моделями.
 */

// WordEntity -> Word
fun WordEntity.toDomain(): Word {
    return Word(
        id = this.id,
        original = this.original,
        translate = this.translate,
        category = this.category,
        difficultyLevel = this.difficultyLevel,
        correctAttempts = this.correctAttempts,
        wrongAttempts = this.wrongAttempts,
        lastReviewTime = this.lastReviewTime,
        isCustom = this.isCustom
    )
}

// Word -> WordEntity
fun Word.toEntity(): WordEntity {
    return WordEntity(
        id = this.id,
        original = this.original,
        translate = this.translate,
        category = this.category,
        difficultyLevel = this.difficultyLevel,
        correctAttempts = this.correctAttempts,
        wrongAttempts = this.wrongAttempts,
        lastReviewTime = this.lastReviewTime,
        isCustom = this.isCustom
    )
}

// CategoryEntity -> Category
fun CategoryEntity.toDomain(): Category {
    return Category(
        name = this.name,
        icon = this.icon,
        color = this.color,
        totalWords = this.totalWords
    )
}

// Category -> CategoryEntity
fun Category.toEntity(): CategoryEntity {
    return CategoryEntity(
        name = this.name,
        icon = this.icon,
        color = this.color,
        totalWords = this.totalWords
    )
}

// List extensions
fun List<WordEntity>.toDomainList(): List<Word> = this.map { it.toDomain() }
fun List<CategoryEntity>.toCategoryDomainList(): List<Category> = this.map { it.toDomain() }
