package com.sawelo.wordmemorizer.data.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

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