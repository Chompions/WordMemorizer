package com.sawelo.wordmemorizer

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.data.WordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.cancellable
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wordDao: WordDao
): ViewModel() {
    private var _allWordsLiveData = MutableLiveData<List<Word>>()
    val allWordsLiveData: LiveData<List<Word>> = _allWordsLiveData
    private var _similarWordsLiveData = MutableLiveData<List<Word>>()
    val similarWordsLiveData: LiveData<List<Word>> = _similarWordsLiveData

    private var searchSimilarWordsJob: Job? = null

    init {
        viewModelScope.launch {
            wordDao.getWords().collectLatest {
                _allWordsLiveData.postValue(it)
            }
        }
    }

    fun forgotWord(word: Word, callback: () -> Unit) {
        viewModelScope.launch {
            wordDao.updateForgotCountById(word.id)
            callback.invoke()
        }
    }

    fun deleteWord(word: Word, callback: () -> Unit) {
        viewModelScope.launch {
            wordDao.deleteWord(word)
            callback.invoke()
        }
    }

    fun resetCount(callback: () -> Unit) {
        viewModelScope.launch {
            wordDao.deleteForgotCount()
            callback.invoke()
        }
    }

    fun searchSimilarWords(word: Word, callback: (isEmpty: Boolean) -> Unit) {
        searchSimilarWordsJob = viewModelScope.launch {
            wordDao.getWordsByKanji(word.kanjiText)
                .cancellable().collectLatest {
                if (it.isNotEmpty()) {
                    _similarWordsLiveData.postValue(it)
                    callback.invoke(false)
                } else {
                    callback.invoke(true)
                }
            }
        }
    }

    fun addWord(word: Word, callback: () -> Unit) {
        viewModelScope.launch {
            wordDao.insertWord(word)
            callback.invoke()
        }
    }

    fun stopCollectingSimilarWords() {
        searchSimilarWordsJob?.cancel(null)
        _similarWordsLiveData.postValue(emptyList())
    }

}