package com.sawelo.wordmemorizer.data

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import kotlinx.serialization.Serializable

@Entity
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val wordText: String,
    val furiganaText: String,
    val definitionText: String,
    val isForgotten: Boolean = false,
    val forgotCount: Int = 0,
    val createdTimeMillis: Long? = null,
    @TypeConverters(WordInfoConverter::class)
    val wordInfo: WordInfo? = null
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