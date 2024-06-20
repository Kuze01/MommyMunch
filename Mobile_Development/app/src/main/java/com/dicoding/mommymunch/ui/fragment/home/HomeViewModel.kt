package com.dicoding.mommymunch.ui.fragment.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.liveData
import com.dicoding.mommymunch.data.api.ApiConfigHome
import kotlinx.coroutines.Dispatchers

class HomeViewModel : ViewModel() {
    private val apiService = ApiConfigHome.getApiConfigHome()

    val foodItems = liveData(Dispatchers.IO) {
        val response = apiService.getData()
        if (response.isSuccessful) {
            emit(response.body() ?: emptyList())
        } else {
            emit(emptyList())
        }
    }
}