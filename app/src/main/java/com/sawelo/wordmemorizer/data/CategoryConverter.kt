package com.sawelo.wordmemorizer.data

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class CategoryConverter {

    @TypeConverter
    fun fromCategoryListToJson(
        categoryList: List<Category>
    ): String = Json.encodeToString(categoryList)

    @TypeConverter
    fun fromJsonToCategoryList(
        json: String
    ): List<Category> = Json.decodeFromString(json)

    @TypeConverter
    fun fromCategoryToJson(
        category: Category
    ): String = Json.encodeToString(category)

    @TypeConverter
    fun fromJsonToCategory(
        json: String
    ): Category = Json.decodeFromString(json)
}