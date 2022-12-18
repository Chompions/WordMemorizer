package com.sawelo.wordmemorizer.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class Word(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val kanjiText: String,
    val hiraganaText: String,
    val definitionText: String,
    val forgotCount: Int = 0,
)