package com.sawelo.wordmemorizer.data.data_class

import kotlinx.serialization.Serializable

@Serializable
data class WordInfoChar(
    val char: Char? = null,
    val strokeCount: Int? = null
)