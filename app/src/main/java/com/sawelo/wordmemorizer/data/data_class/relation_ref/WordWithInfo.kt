package com.sawelo.wordmemorizer.data.data_class.relation_ref

import androidx.room.Embedded
import androidx.room.Relation
import com.sawelo.wordmemorizer.data.data_class.entity.Word
import com.sawelo.wordmemorizer.data.data_class.entity.WordInfo

data class WordWithInfo(
    @Embedded val word: Word,
    @Relation(
        parentColumn = "wordId",
        entityColumn = "wordInfoId"
    )
    val wordInfo: WordInfo
)
