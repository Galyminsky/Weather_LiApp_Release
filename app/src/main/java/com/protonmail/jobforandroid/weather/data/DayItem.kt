package com.protonmail.jobforandroid.weather.data

data class DayItem (

    val country: String,
    val city: String,
    val time: String,
    val condition: String,
    val imageUrl: String,
    val currentTemp: String,
    val maxTemp: String,
    val minTemp: String,
    val hours: String

        )