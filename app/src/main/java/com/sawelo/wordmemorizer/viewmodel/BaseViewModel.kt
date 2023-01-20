package com.sawelo.wordmemorizer.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sawelo.wordmemorizer.util.Event


abstract class BaseViewModel: ViewModel() {

    // Mutable/LiveData of String resource reference Event
    private val _message = MutableLiveData<Event<String>>()
    val message : LiveData<Event<String>>
        get() = _message

    // Post in background thread
    fun postMessage(message: String) {
        _message.postValue(Event(message))
    }

    // Post in main thread
    fun setMessage(message: String) {
        _message.value = Event(message)
    }

}