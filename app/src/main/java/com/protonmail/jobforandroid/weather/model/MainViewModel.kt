package com.protonmail.jobforandroid.weather.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.protonmail.jobforandroid.weather.model.WeatherModel

class MainViewModel : ViewModel() {
    val liveDataToday = MutableLiveData<WeatherModel>()
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
    val liveDataWeatherBackdrop = MutableLiveData<WeatherBackdropState>()
    val liveDataTodayMetrics = MutableLiveData<TodayWeatherMetrics>()
    val liveDataWeatherIcon = MutableLiveData<WeatherIconState>()
}

data class WeatherBackdropState(val topColor: Int, val bottomColor: Int)

data class WeatherIconState(val conditionCode: Int, val isDay: Boolean?)

data class TodayWeatherMetrics(
    val feelsLikeC: String?,
    val windSpeedKph: String?,
    val windDirection: String?,
    val humidityPercent: String?,
    val precipitationChancePercent: String?,
    val pressureHpa: String?
)
