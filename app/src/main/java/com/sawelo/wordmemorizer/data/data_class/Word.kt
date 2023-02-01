package com.sawelo.wordmemorizer.data.data_class

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = Word.TABLE_NAME)
data class Word(
    @PrimaryKey (autoGenerate = true)
    val wordId: Int = 0,
    val wordText: String,
    val furiganaText: String,
    val definitionText: String,
    val isForgotten: Boolean = false,
    val forgotCount: Int = 0,
    val createdTimeMillis: Long,
) {
    companion object {
        const val TABLE_NAME = "word_table"
    }
}