package com.sawelo.wordmemorizer.data.data_class.api_response

import com.google.gson.annotations.SerializedName

data class JishoResponse(

    @field:SerializedName("data")
    val data: List<DataItem>?
)

data class DataItem(

    @field:SerializedName("japanese")
    val japanese: List<JapaneseItem>?,

    @field:SerializedName("senses")
    val senses: List<SensesItem>?,

    @field:SerializedName("jlpt")
    val jlpt: List<String>?,

    @field:SerializedName("slug")
    val slug: String?,

    @field:SerializedName("tags")
    val tags: List<String>?,

    @field:SerializedName("is_common")
    val isCommon: Boolean?
)

data class SensesItem(

    @field:SerializedName("parts_of_speech")
    val partsOfSpeech: List<String>?,

    @field:SerializedName("english_definitions")
    val englishDefinitions: List<String>?,

    @field:SerializedName("info")
    val info: List<String>?,
)

data class JapaneseItem(

    @field:SerializedName("reading")
    val reading: String?,

    @field:SerializedName("word")
    val word: String?
)
