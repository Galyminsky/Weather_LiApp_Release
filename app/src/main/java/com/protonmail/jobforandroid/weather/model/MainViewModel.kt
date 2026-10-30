package com.protonmail.jobforandroid.weather.model

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.protonmail.jobforandroid.weather.model.WeatherModel

class MainViewModel(private val savedStateHandle: SavedStateHandle) : ViewModel() {
    var userSelectedCity: String?
        get() = savedStateHandle[KEY_USER_SELECTED_CITY]
        set(value) {
            savedStateHandle[KEY_USER_SELECTED_CITY] = value
        }

    var loadedWeatherQuery: String? = null
    var failedWeatherQuery: String? = null

    var locationPermissionRequestAttempted: Boolean
        get() = savedStateHandle[KEY_LOCATION_PERMISSION_REQUEST_ATTEMPTED] ?: false
        set(value) {
            savedStateHandle[KEY_LOCATION_PERMISSION_REQUEST_ATTEMPTED] = value
        }

    var selectedHourlyEpoch: Long?
        get() = savedStateHandle[KEY_SELECTED_HOURLY_EPOCH]
        set(value) {
            savedStateHandle[KEY_SELECTED_HOURLY_EPOCH] = value
        }

    var hourlySelectionInitialized: Boolean
        get() = savedStateHandle[KEY_HOURLY_SELECTION_INITIALIZED] ?: false
        set(value) {
            savedStateHandle[KEY_HOURLY_SELECTION_INITIALIZED] = value
        }

    val liveDataToday = MutableLiveData<WeatherModel>()
    val liveDataCurrent = MutableLiveData<WeatherModel>()
    val liveDataList = MutableLiveData<List<WeatherModel>>()
    val liveDataWeatherBackdrop = MutableLiveData<WeatherBackdropState>()
    val liveDataTodayMetrics = MutableLiveData<TodayWeatherMetrics>()
    val liveDataWeatherIcon = MutableLiveData<WeatherIconState>()
    val weatherRequestStatus = MutableLiveData<String?>()

    private companion object {
        const val KEY_USER_SELECTED_CITY = "user_selected_city"
        const val KEY_LOCATION_PERMISSION_REQUEST_ATTEMPTED = "location_permission_request_attempted"
        const val KEY_SELECTED_HOURLY_EPOCH = "selected_hourly_epoch"
        const val KEY_HOURLY_SELECTION_INITIALIZED = "hourly_selection_initialized"
    }
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
