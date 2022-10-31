package com.protonmail.jobforandroid.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.protonmail.jobforandroid.weather.fragments.MainFragment

const val API_KEY = "543cbe8f60c8411f9f6133943222710"
//www.weatherapi.com ---> API

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.placeHolder, MainFragment.newInstance())
            .commit()
    }

}