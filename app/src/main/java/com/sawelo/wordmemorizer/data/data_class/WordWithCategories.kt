package com.sawelo.wordmemorizer.data.data_class

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation

data class WordWithCategories(
    @Embedded
    val word: Word,
    @Relation(
        entity = Category::class,
        parentColumn = "wordId",
        entityColumn = "categoryId",
        associateBy = Junction(
            value = WordCategoryMap::class,
            parentColumn = "wordIdMap",
            entityColumn = "categoryIdMap"
        )
    )
    val categories: List<Category>
)