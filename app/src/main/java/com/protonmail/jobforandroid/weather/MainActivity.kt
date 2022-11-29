package com.protonmail.jobforandroid.weather

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.protonmail.jobforandroid.weather.fragments.MainFragment


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