package com.sawelo.wordmemorizer.data.data_class.relation_ref

import androidx.room.Embedded
import androidx.room.Junction
import androidx.room.Relation
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity_cross_ref.WordCategoryMap

data class WordWithCategories(
    @Embedded
    val wordWithInfo: WordWithInfo,
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