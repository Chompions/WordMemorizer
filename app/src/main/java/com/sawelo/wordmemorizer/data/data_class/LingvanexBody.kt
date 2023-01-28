package com.sawelo.wordmemorizer.data.data_class

import com.google.gson.annotations.SerializedName

data class LingvanexBody(

	@field:SerializedName("data")
	val data: String,

	@field:SerializedName("from")
	val from: String = "ja_JA",

	@field:SerializedName("to")
	val to: String = "en_US",

	@field:SerializedName("platform")
	val platform: String = "api"
)
