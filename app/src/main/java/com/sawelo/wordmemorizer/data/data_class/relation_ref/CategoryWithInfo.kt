package com.sawelo.wordmemorizer.data.data_class.relation_ref

import androidx.room.Embedded
import androidx.room.Relation
import com.sawelo.wordmemorizer.data.data_class.entity.Category
import com.sawelo.wordmemorizer.data.data_class.entity.CategoryInfo

data class CategoryWithInfo(
    @Embedded val category: Category,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "categoryInfoId"
    )
    val categoryInfo: CategoryInfo
)
