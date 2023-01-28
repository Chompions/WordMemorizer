package com.sawelo.wordmemorizer.data.data_class

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

    @field:SerializedName("antonyms")
    val antonyms: List<Any>?,

    @field:SerializedName("restrictions")
    val restrictions: List<Any>?,

    @field:SerializedName("links")
    val links: List<Any>?,

    @field:SerializedName("source")
    val source: List<Any>?,

    @field:SerializedName("see_also")
    val seeAlso: List<Any>?,

    @field:SerializedName("english_definitions")
    val englishDefinitions: List<String>?,

    @field:SerializedName("tags")
    val tags: List<Any>?,

    @field:SerializedName("info")
    val info: List<String>?,

    @field:SerializedName("sentences")
    val sentences: List<Any>?
)

data class JapaneseItem(

    @field:SerializedName("reading")
    val reading: String?,

    @field:SerializedName("word")
    val word: String?
)
