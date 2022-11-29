package com.protonmail.jobforandroid.weather.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.protonmail.jobforandroid.weather.model.WeatherModel

class MainViewModel : ViewModel() {
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
}