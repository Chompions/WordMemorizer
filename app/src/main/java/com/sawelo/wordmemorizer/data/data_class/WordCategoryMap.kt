package com.sawelo.wordmemorizer.data.data_class

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    primaryKeys = ["wordIdMap", "categoryIdMap"],
    foreignKeys = [
        ForeignKey(
            entity = Word::class,
            parentColumns = ["wordId"],
            childColumns = ["wordIdMap"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryIdMap"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ]
)
data class WordCategoryMap (
    val wordIdMap: Int,
    @ColumnInfo(index = true)
    val categoryIdMap: Int,
)