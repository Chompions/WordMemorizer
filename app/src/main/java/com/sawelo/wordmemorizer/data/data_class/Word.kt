package com.sawelo.wordmemorizer.data.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Word(
    @PrimaryKey (autoGenerate = true)
    val wordId: Int = 0,
    val wordText: String,
    val furiganaText: String,
    val definitionText: String,
    val isForgotten: Boolean = false,
    val forgotCount: Int = 0,
    val createdTimeMillis: Long,
)