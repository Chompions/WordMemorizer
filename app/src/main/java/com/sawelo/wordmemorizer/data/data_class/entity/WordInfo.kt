package com.sawelo.wordmemorizer.data.data_class.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    indices = [
        Index(value = ["createdTimeMillis"], unique = true)
    ],
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["wordId"],
            childColumns = ["wordInfoId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
data class WordInfo(
    @PrimaryKey(autoGenerate = true)
    val wordInfoId: Int = 0,
    val wordId: Int = 0,
    var isForgotten: Boolean = false,
    var rememberCount: Int = 0,
    val createdTimeMillis: Long,
)