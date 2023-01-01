package com.sawelo.wordmemorizer.data.data_class

import kotlinx.serialization.Serializable

@Serializable
data class WordInfo(
    val isCommon: Boolean? = null,
    val jlptLevel: Int? = null,
    val charList: List<WordInfoChar>? = null
)