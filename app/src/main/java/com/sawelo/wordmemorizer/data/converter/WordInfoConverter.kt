package com.sawelo.wordmemorizer.data.converter

import androidx.room.TypeConverter
import com.sawelo.wordmemorizer.data.data_class.WordInfo
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