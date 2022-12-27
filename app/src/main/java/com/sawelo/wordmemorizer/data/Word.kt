package com.sawelo.wordmemorizer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Entity
data class Word(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var wordText: String,
    var furiganaText: String,
    var definitionText: String,
    var isForgotten: Boolean = false,
    var forgotCount: Int = 0,
    var createdTimeMillis: Long,
    var categoryList: List<Category> = emptyList(),
    var wordInfo: WordInfo? = null
)

@Serializable
data class WordInfo(
    val isCommon: Boolean? = null,
    val jlptLevel: Int? = null,
    val charList: List<WordInfoChar>? = null
)

@Serializable
data class WordInfoChar(
    val char: Char? = null,
    val strokeCount: Int? = null
)