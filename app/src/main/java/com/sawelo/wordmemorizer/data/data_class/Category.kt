package com.sawelo.wordmemorizer.data.data_class

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

@Entity
@Serializable
@Parcelize
data class Category(
    @PrimaryKey(autoGenerate = true)
    var id: Int = 0,
    var categoryName: String,
    var wordCount: Int = 0,
): Parcelable