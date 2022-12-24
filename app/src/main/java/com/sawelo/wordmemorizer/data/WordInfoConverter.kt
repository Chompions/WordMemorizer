package com.sawelo.wordmemorizer.data

import androidx.room.TypeConverter
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class WordInfoConverter {

    @TypeConverter
    fun fromInfoToJson(
        info: WordInfo?
    ): String = Json.encodeToString(info)

    @TypeConverter
    fun fromJsonToInfo(
        json: String
    ): WordInfo? {
        return if (json.isNotBlank()) {
            Json.decodeFromString(json)
        } else null
    }
}