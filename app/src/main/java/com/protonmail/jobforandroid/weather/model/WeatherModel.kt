package com.protonmail.jobforandroid.weather.model

data class WeatherModel(

    val country: String,
    val region: String,
    val city: String,
    val time: String,
    val condition: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val imageUrl: String,
    val hours: String,
    val sunrise: String,
    val sunset: String,

    )