package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.atilika.kuromoji.jumandic.Tokenizer
import com.sawelo.wordmemorizer.data.Word
import com.sawelo.wordmemorizer.data.WordDao
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val wordDao: WordDao
) : ViewModel() {
    val tokenizer = Tokenizer()

    private var _allWordListLiveData = MutableLiveData<List<Word>>()
    val allWordListLiveData: LiveData<List<Word>> = _allWordListLiveData

    private var _currentWordStringLiveData = MutableLiveData<String>()
    val currentWordStringLiveData: LiveData<String> = _currentWordStringLiveData
    private var _currentFuriganaStringLiveData = MutableLiveData<String>()
    val currentFuriganaStringLiveData: LiveData<String> = _currentFuriganaStringLiveData

    private var _similarWordListLiveData = MutableLiveData<List<Word>>()
    val similarWordListLiveData: LiveData<List<Word>> = _similarWordListLiveData

//    private var searchSimilarWordsJob: Job? = null

    init {
        viewModelScope.launch {
            wordDao.getWords().collectLatest {
                _allWordListLiveData.value = it
            }
        }
    }

    suspend fun setIsForgottenWord(word: Word, isForgotten: Boolean) {
        wordDao.updateIsForgottenById(word.id, isForgotten)
    }

    suspend fun setForgotCountIncreaseWord(word: Word, callback: () -> Unit) {
        wordDao.updateForgotCountById(word.id)
        callback.invoke()
    }

    suspend fun deleteWord(word: Word, callback: () -> Unit) {
        wordDao.deleteWord(word)
        callback.invoke()
    }

    suspend fun resetCount(callback: () -> Unit) {
        wordDao.deleteForgotCount()
        callback.invoke()
    }

    suspend fun searchSimilarWords(wordString: String) {
        _similarWordListLiveData.value = wordDao.getWordsByKanji(wordString)
    }

    suspend fun addWord(word: Word, callback: (id: Int) -> Unit) {
        val id = wordDao.insertWord(word)
        callback.invoke(id.toInt())
    }

    fun clearSimilarWordList() {
        _similarWordListLiveData.value = emptyList()
    }

//
//    fun stopCollectingSimilarWords() {
//        searchSimilarWordsJob?.cancel(null)
//        _similarWordsLiveData.postValue(emptyList())
//    }

}