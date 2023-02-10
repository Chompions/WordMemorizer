package com.sawelo.wordmemorizer.data.data_class.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["categoryId"],
            childColumns = ["categoryInfoId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        ),
    ]
)
data class CategoryInfo(
    @PrimaryKey(autoGenerate = true)
    val categoryInfoId: Int = 0,
    val categoryId: Int = 0,
    val wordCount: Int = 0,
)