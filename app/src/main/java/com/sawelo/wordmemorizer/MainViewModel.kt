package com.sawelo.wordmemorizer

import androidx.lifecycle.*
import androidx.recyclerview.widget.RecyclerView
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.data.WordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wordDao: WordDao
): ViewModel() {

    suspend fun insertAllWords(adapter: WordAdapter) {
        adapter.notifyInsertAllWords(wordDao.getWords())
    }

    suspend fun insertNewWord(word: Word, adapter: WordAdapter, recyclerView: RecyclerView) {
        adapter.notifyInsertNewWord(word.kanjiText, wordDao.getWords(), recyclerView)
    }

    suspend fun forgotWord(word: Word, adapter: WordAdapter, recyclerView: RecyclerView) {
        wordDao.updateForgotCountByKanji(word.kanjiText)
        adapter.notifyChangeWord(word.kanjiText, wordDao.getWords(), recyclerView)
    }

    suspend fun deleteWord(word: Word, adapter: WordAdapter) {
        wordDao.deleteWord(word)
        adapter.notifyDeleteWord(word.kanjiText, wordDao.getWords())
    }
}