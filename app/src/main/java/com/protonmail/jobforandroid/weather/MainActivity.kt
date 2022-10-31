package com.protonmail.jobforandroid.weather

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

const val API_KEY = "543cbe8f60c8411f9f6133943222710"

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

}