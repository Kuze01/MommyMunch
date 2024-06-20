package com.dicoding.mommymunch.ui.fragment.account

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class AccountViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is authentication Fragment"
    }
    val text: LiveData<String> = _text
}