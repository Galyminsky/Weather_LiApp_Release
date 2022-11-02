package com.protonmail.jobforandroid.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.protonmail.jobforandroid.weather.adapters.WeatherModel

class MainViewModel : ViewModel() {
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
}