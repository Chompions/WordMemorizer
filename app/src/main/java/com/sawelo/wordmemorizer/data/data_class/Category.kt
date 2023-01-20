package com.sawelo.wordmemorizer.data.data_class

import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import kotlinx.parcelize.Parcelize

@Parcelize
@Entity(
    indices = [
        Index(value = ["categoryName"], unique = true)
    ]
)
data class Category(
    @PrimaryKey(autoGenerate = true)
    val categoryId: Int = 0,
    val categoryName: String,
    val wordCount: Int = 0,
): Parcelable