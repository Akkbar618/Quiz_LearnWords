package com.example.quiz_engwords.domain.model

/**
 * Domain модель категории слов.
 */
data class Category(
    val name: String,
    val icon: String,
    val color: String,
    val totalWords: Int = 0
)
